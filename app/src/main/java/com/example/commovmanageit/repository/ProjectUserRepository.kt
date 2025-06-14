package com.example.commovmanageit.db.repositories
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.remote.dto.ProjectUserRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.db.dao.ProjectUserDao
import com.example.commovmanageit.db.dao.TaskDao
import com.example.commovmanageit.db.entities.ProjectUser
import com.example.commovmanageit.remote.dto.UserRemote
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
class ProjectUserRepository(
    private val ProjectUserDao: ProjectUserDao,
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

    suspend fun insertLocal(ProjectUser: ProjectUser): ProjectUser {
        ProjectUserDao.insert(ProjectUser)
        syncIfConnected()
        return ProjectUser
    }

    suspend fun updateLocal(ProjectUser: ProjectUser) {
        ProjectUser.updatedAt = Clock.System.now()
        ProjectUserDao.update(ProjectUser)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String,type: String) {
        ProjectUserDao.softDelete(id)
        if(type.equals("Test"))
            return
        else
            syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): ProjectUser? = ProjectUserDao.getById(id)
    suspend fun getAllLocal(): List<ProjectUser> = ProjectUserDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(ProjectUser: ProjectUser): String {
        val remoteProjectUser = ProjectUser.toRemote()
        val result = SupabaseManager.insertProjectUser<ProjectUserRemote>(remoteProjectUser)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(ProjectUser: ProjectUser): ProjectUserRemote {
        val remoteProjectUser = ProjectUser.toRemote()
        Log.d("ProjectUserRepository", "Updating remote ProjectUser: ${ProjectUser.id}")
        return SupabaseManager.updateProjectUser<ProjectUserRemote>(ProjectUser.serverId ?: ProjectUser.id, remoteProjectUser)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("project_users", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(ProjectUser: ProjectUser): ProjectUser {
        val newProjectUser = ProjectUser.copy(
            id = ProjectUser.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newProjectUser.id
                val syncedProjectUser = newProjectUser.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedProjectUser)
                insertLocal(syncedProjectUser)

                syncedProjectUser
            } catch (e: Exception) {
                ProjectUserDao.insert(newProjectUser)
                newProjectUser
            }
        } else {
            ProjectUserDao.insert(newProjectUser)
            newProjectUser
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(ProjectUser: ProjectUser) {
        val updatedProjectUser = ProjectUser.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedProjectUser.serverId != null) {
                    updateRemote(updatedProjectUser)
                    Log.d("ProjectUserRepository", "Updated remote ProjectUser: ${updatedProjectUser.id}")
                    ProjectUserDao.update(updatedProjectUser.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedProjectUser)
                    ProjectUserDao.update(updatedProjectUser.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                ProjectUserDao.update(updatedProjectUser)
            }
        } else {
            ProjectUserDao.update(updatedProjectUser)
        }
    }

    suspend fun delete(id: String) {
        try {
            val ProjectUser = ProjectUserDao.getById(id)
            ProjectUser?.let {
                ProjectUserDao.softDelete(id)

                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId!!)
                    ProjectUserDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("ProjectUser not found")
        } catch (e: Exception) {
            println("Delete operation partially failed: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncIfConnected() {
        if (connectivityMonitor.isConnected) {
            try {
                syncChanges()
            } catch (e: Exception) {
                Log.d("ProjectUserRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        ProjectUserDao.getUnsyncedCreatedOrUpdated().forEach { ProjectUser ->
            try {
                if (ProjectUser.serverId == null) {
                    val serverId = insertRemote(ProjectUser)
                    ProjectUserDao.markAsSynced(ProjectUser.id, serverId)
                } else {
                    updateRemote(ProjectUser)
                    ProjectUserDao.updateSyncStatus(ProjectUser.id, true)
                }
            } catch (e: Exception) {
                Log.d("ProjectUserRepository", "Erro ao sincronizar cliente ${ProjectUser.id}", e)
            }
        }

        ProjectUserDao.getUnsyncedDeleted().forEach { ProjectUser ->
            try {
                Log.d("ProjectUserRepository", "Deleting remote ProjectUser: ${ProjectUser.id}")
                ProjectUser.serverId?.let { deleteRemote(it) }
                ProjectUserDao.updateSyncStatus(ProjectUser.id, true)
            } catch (e: Exception) {
            }
        }

        ProjectUserDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<ProjectUser>> = ProjectUserDao.observeAllActive()
    fun observeById(id: String): Flow<ProjectUser?> = ProjectUserDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<ProjectUser>> = ProjectUserDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<ProjectUser>> = ProjectUserDao.observeUnsyncedDeletes()

    suspend fun getActiveCount(): Int = ProjectUserDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = ProjectUserDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<ProjectUser> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("No internet connection available")
            }

            val remoteProjectUsers: List<ProjectUserRemote> = SupabaseManager.getAll<ProjectUserRemote>("project_users")

            remoteProjectUsers.map { remote ->
                ProjectUser(
                    id = remote.id,
                    serverId = remote.id,
                    createdAt = Instant.parse(remote.created_at),
                    updatedAt = Instant.parse(remote.updated_at),
                    deletedAt = remote.deleted_at?.let { Instant.parse(remote.deleted_at) },
                    isSynced = true,
                    projectId = remote.project_id,
                    userId = remote.project_id,
                    inviterId = remote.inviter_id,
                    speed = remote.speed,
                    quality = remote.quality,
                    collaboration = remote.collaboration,
                    status = remote.status
                )
            }
        } catch (e: Exception) {
            Log.e("ProjectUserRepository", "Failed to fetch remote ProjectUsers", e)
            emptyList()
        }
    }


    suspend fun getByIdRemote(id: String): ProjectUserRemote? {
        return try {
            val remoteProjectUser = SupabaseManager.fetchById<ProjectUserRemote>("project_users", id)

            remoteProjectUser?.let { ProjectUser ->
                ProjectUserDao.update(ProjectUser.toLocal())
            }

            return remoteProjectUser
        } catch (e: Exception) {
            Log.e("ProjectUserRepository", "Error fetching remote ProjectUser(normal if in test)", e)
            null
        }
    }

    suspend fun getByUserIdRemote(id: String): List<ProjectUserRemote>? {
        return try {
            val remoteProjectUser = SupabaseManager.fetchByUserId<ProjectUserRemote>("project_users", id, "user_id")

            remoteProjectUser?.let { projectUser ->
                projectUser.forEach { ProjectUserDao.update(it.toLocal()) }
            }

            return remoteProjectUser
        } catch (e: Exception) {
            Log.e("ProjectUserRepository", "Error fetching remote ProjectUser(normal if in test)", e)
            null
        }
    }
    suspend fun getByProjectIdRemote(id: String): ProjectUserRemote? {
        return try {
            val remoteUsers = SupabaseManager.fetchByProjectId<ProjectUserRemote>("project_users", id)

            remoteUsers?.let { Users ->
                ProjectUserDao.update(Users.toLocal())
            }

            return remoteUsers
        } catch (e: Exception) {
            Log.e("ProjectUsersRepository", "Error fetching remote ProjectUsers by project(normal if in test)", e)
            null
        }
    }
    suspend fun clearLocalDatabase() {
        ProjectUserDao.deleteAll()
    }
}