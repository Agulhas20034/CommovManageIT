package com.example.commovmanageit.db.repositories
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.dao.RoleDao
import com.example.commovmanageit.db.entities.Role
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.remote.dto.PermissionRemote
import com.example.commovmanageit.remote.dto.RoleRemote
import com.example.commovmanageit.remote.dto.toRemote
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
class RoleRepository(
    private val roleDao: RoleDao,
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

    suspend fun insertLocal(role: Role): Role {
        roleDao.insert(role)
        syncIfConnected()
        return role
    }

    suspend fun updateLocal(role: Role) {
        roleDao.update(role.copy(updatedAt = Clock.System.now()))
        syncIfConnected()
    }

    suspend fun deleteLocal(role: Role) {
        roleDao.softDelete(role.id)
        syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): Role? = roleDao.getById(id)
    suspend fun getAllLocal(): List<Role> = roleDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertRemote(role: Role): String {
        val remoteRole = role.toRemote()
        val result = SupabaseManager.insertRole(remoteRole)
        Log.d("RoleRepository", "Inserted role remotely with ID: ${result.id}")
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(role: Role): RoleRemote {
        val remoteRole = role.toRemote()
        return SupabaseManager.updateRole<RoleRemote>(role.serverId ?: role.id, remoteRole)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("roles", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(role: Role): Role {
        val newRole = role.copy(
            id = role.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now(),
        )
        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newRole.id
                val syncedRole = newRole.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedRole)
                insertLocal(syncedRole)
                syncedRole
            } catch (e: Exception) {
                roleDao.insert(newRole)
                newRole
            }
        } else {
            roleDao.insert(newRole)
            newRole
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(role: Role) {
        val updatedRole = role.copy(updatedAt = Clock.System.now())
        if (connectivityMonitor.isConnected) {
            try {
                if (updatedRole.serverId != null) {
                    updateRemote(updatedRole)
                    roleDao.update(updatedRole.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedRole)
                    roleDao.update(updatedRole.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                roleDao.update(updatedRole)
            }
        } else {
            roleDao.update(updatedRole)
        }
    }

    suspend fun delete(id: String) {
        try {
            val role = roleDao.getById(id)
            role?.let {
                roleDao.softDelete(id)
                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId)
                    roleDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("Role not found")
        } catch (e: Exception) {
            Log.d("RoleRepository", "Delete operation failed: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncIfConnected() {
        if (connectivityMonitor.isConnected) {
            try {
                syncChanges()
            } catch (e: Exception) {
                Log.d("RoleRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        roleDao.getUnsyncedCreatedOrUpdated().forEach { customer ->
            try {
                if (customer.serverId == null) {
                    Log.d("RoleRepository: ","tried to sync role ${customer.name} with server id ${customer.serverId}")
                    val serverId = insertRemote(customer)
                    roleDao.markAsSynced(customer.id, serverId)
                } else {
                    updateRemote(customer)
                    roleDao.updateSyncStatus(customer.id, true)
                }
            } catch (e: Exception) {
                Log.d("CustomerRepository", "Erro ao sincronizar cliente ${customer.id}", e)
            }
        }

        roleDao.getUnsyncedDeleted().forEach { customer ->
            try {
                Log.d("CustomerRepository", "Deleting remote customer: ${customer.id}")
                customer.serverId?.let { deleteRemote(it) }
                roleDao.updateSyncStatus(customer.id, true)
            } catch (e: Exception) {
            }
        }

        roleDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<Role>> = roleDao.observeAllActive()
    fun observeById(id: String): Flow<Role?> = roleDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<Role>> = roleDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<Role>> = roleDao.observeUnsyncedDeletes()

    suspend fun getActiveCount(): Int = roleDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = roleDao.getUnsyncedCount()
    suspend fun clearLocalDatabase() = roleDao.deleteAll()

    suspend fun getByIdRemote(id: String): RoleRemote? {
        return try {
            val remoteRole = SupabaseManager.fetchById<RoleRemote>("roles", id)

            remoteRole?.let { role ->
                roleDao.update(role.toLocal())
            }

            return remoteRole
        } catch (e: Exception) {
            Log.e("RoleRepository", "Erro ao buscar role remota", e)
            null
        }
    }
}