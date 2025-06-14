package com.example.commovmanageit.repository
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.remote.dto.PermissionRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.db.dao.PermissionDao
import com.example.commovmanageit.db.entities.Permission
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class PermissionRepository(
    private val permissionDao: PermissionDao,
    private val connectivityMonitor: ConnectivityMonitor,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    init {
        connectivityMonitor.registerListener { online ->
            if (online) {
                coroutineScope.launch {
                    syncChanges()
                }
            }
        }
    }

    suspend fun insertLocal(permission: Permission): Permission {
        permissionDao.insert(permission)
        syncIfConnected()
        return permission
    }

    suspend fun updateLocal(permission: Permission) {
        permission.updatedAt = Clock.System.now()
        permissionDao.update(permission)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String) {
        permissionDao.softDelete(id)
        syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): Permission? = permissionDao.getById(id)
    suspend fun getAllLocal(): List<Permission> = permissionDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertRemote(permission: Permission): String {
        val remotePermission = permission.toRemote()
        val result = SupabaseManager.insertPermission<PermissionRemote>(remotePermission)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(permission: Permission): PermissionRemote {
        val remotePermission = permission.toRemote()
        Log.d("PermissionRepository", "Atualizando permissão remota: ${permission.id}")
        return SupabaseManager.updatePermission<PermissionRemote>(permission.serverId ?: permission.id, remotePermission)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("permissions", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(permission: Permission): Permission {
        val newPermission = permission.copy(
            id = permission.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newPermission.id
                val syncedPermission = newPermission.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedPermission)
                insertLocal(syncedPermission)
                syncedPermission
            } catch (e: Exception) {
                permissionDao.insert(newPermission)
                newPermission
            }
        } else {
            permissionDao.insert(newPermission)
            newPermission
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(permission: Permission) {
        val updatedPermission = permission.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedPermission.serverId != null) {
                    updateRemote(updatedPermission)
                    Log.d("PermissionRepository", "Permissão remota atualizada: ${updatedPermission.id}")
                    permissionDao.update(updatedPermission.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedPermission)
                    permissionDao.update(updatedPermission.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                permissionDao.update(updatedPermission)
            }
        } else {
            permissionDao.update(updatedPermission)
        }
    }

    suspend fun delete(id: String) {
        try {
            val permission = permissionDao.getById(id)
            permission?.let {
                permissionDao.softDelete(id)

                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId!!)
                    permissionDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("Permissão não encontrada")
        } catch (e: Exception) {
            println("Falha parcial ao deletar: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncIfConnected() {
        if (connectivityMonitor.isConnected) {
            try {
                syncChanges()
            } catch (e: Exception) {
                Log.d("PermissionRepository", "Falha ao sincronizar: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        permissionDao.getUnsyncedCreatedOrUpdated().forEach { permission ->
            try {
                if (permission.serverId == null) {
                    val serverId = insertRemote(permission)
                    permissionDao.markAsSynced(permission.id, serverId)
                } else {
                    updateRemote(permission)
                    permissionDao.updateSyncStatus(permission.id, true)
                }
            } catch (e: Exception) {
                Log.d("PermissionRepository", "Erro ao sincronizar permissão ${permission.id}", e)
            }
        }

        permissionDao.getUnsyncedDeleted().forEach { permission ->
            try {
                Log.d("PermissionRepository", "Deletando permissão remota: ${permission.id}")
                permission.serverId?.let { deleteRemote(it) }
                permissionDao.updateSyncStatus(permission.id, true)
            } catch (e: Exception) {
            }
        }

        permissionDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<Permission>> = permissionDao.observeAllActive()
    fun observeById(id: String): Flow<Permission?> = permissionDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<Permission>> = permissionDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<Permission>> = permissionDao.observeUnsyncedDeletes()

    suspend fun getActiveCount(): Int = permissionDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = permissionDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<Permission> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("Sem conexão com a internet")
            }

            val remotePermissions: List<PermissionRemote> = SupabaseManager.getAll<PermissionRemote>("permissions")

            remotePermissions.map { remote ->
                Permission(
                    id = remote.id,
                    serverId = remote.id,
                    label = remote.label,
                    createdAt = Instant.parse(remote.created_at),
                    updatedAt = Instant.parse(remote.updated_at),
                    deletedAt = remote.deleted_at?.let { Instant.parse(remote.deleted_at) },
                    isSynced = true
                )
            }
        } catch (e: Exception) {
            Log.e("PermissionRepository", "Falha ao buscar permissões remotas", e)
            emptyList()
        }
    }

    suspend fun getByIdRemote(id: String): PermissionRemote? {
        return try {
            val remotePermission = SupabaseManager.fetchById<PermissionRemote>("permissions", id)

            remotePermission?.let { permission ->
                permissionDao.update(permission.toLocal())
            }

            return remotePermission
        } catch (e: Exception) {
            Log.e("PermissionRepository", "Erro ao buscar permissão remota", e)
            null
        }
    }

    suspend fun clearLocalDatabase() {
        permissionDao.deleteAll()
    }
}