package com.example.commovmanageit.db.repositories
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.remote.dto.TaskUserRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.db.dao.TaskUserDao
import com.example.commovmanageit.db.entities.TaskUser
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
class TaskUserRepository(
    private val TaskUserDao: TaskUserDao,
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

    suspend fun insertLocal(TaskUser: TaskUser): TaskUser {
        TaskUserDao.insert(TaskUser)
        syncIfConnected()
        return TaskUser
    }

    suspend fun updateLocal(TaskUser: TaskUser) {
        TaskUser.updatedAt = Clock.System.now()
        TaskUserDao.update(TaskUser)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String,type: String) {
        TaskUserDao.softDelete(id)
        if(type.equals("Test"))
            return
        else
            syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): TaskUser? = TaskUserDao.getById(id)
    suspend fun getAllLocal(): List<TaskUser> = TaskUserDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(TaskUser: TaskUser): String {
        val remoteTaskUser = TaskUser.toRemote()
        val result = SupabaseManager.insertTaskUser<TaskUserRemote>(remoteTaskUser)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(TaskUser: TaskUser): TaskUserRemote {
        val remoteTaskUser = TaskUser.toRemote()
        Log.d("TaskUserRepository", "Updating remote TaskUser: ${TaskUser.id}")
        return SupabaseManager.updateTaskUser<TaskUserRemote>(TaskUser.serverId ?: TaskUser.id, remoteTaskUser)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("task_users", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(TaskUser: TaskUser): TaskUser {
        val newTaskUser = TaskUser.copy(
            id = TaskUser.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newTaskUser.id
                val syncedTaskUser = newTaskUser.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedTaskUser)
                insertLocal(syncedTaskUser)

                syncedTaskUser
            } catch (e: Exception) {
                TaskUserDao.insert(newTaskUser)
                newTaskUser
            }
        } else {
            TaskUserDao.insert(newTaskUser)
            newTaskUser
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(TaskUser: TaskUser) {
        val updatedTaskUser = TaskUser.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedTaskUser.serverId != null) {
                    updateRemote(updatedTaskUser)
                    Log.d("TaskUserRepository", "Updated remote TaskUser: ${updatedTaskUser.id}")
                    TaskUserDao.update(updatedTaskUser.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedTaskUser)
                    TaskUserDao.update(updatedTaskUser.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                TaskUserDao.update(updatedTaskUser)
            }
        } else {
            TaskUserDao.update(updatedTaskUser)
        }
    }

    suspend fun delete(id: String) {
        try {
            val TaskUser = TaskUserDao.getById(id)
            TaskUser?.let {
                TaskUserDao.softDelete(id)

                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId)
                    TaskUserDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("TaskUser not found")
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
                Log.d("TaskUserRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        TaskUserDao.getUnsyncedCreatedOrUpdated().forEach { TaskUser ->
            try {
                if (TaskUser.serverId == null) {
                    val serverId = insertRemote(TaskUser)
                    TaskUserDao.markAsSynced(TaskUser.id, serverId)
                } else {
                    updateRemote(TaskUser)
                    TaskUserDao.updateSyncStatus(TaskUser.id, true)
                }
            } catch (e: Exception) {
                Log.d("TaskUserRepository", "Erro ao sincronizar cliente ${TaskUser.id}", e)
            }
        }

        TaskUserDao.getUnsyncedDeleted().forEach { TaskUser ->
            try {
                Log.d("TaskUserRepository", "Deleting remote TaskUser: ${TaskUser.id}")
                TaskUser.serverId?.let { deleteRemote(it) }
                TaskUserDao.updateSyncStatus(TaskUser.id, true)
            } catch (e: Exception) {
            }
        }

        TaskUserDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<TaskUser>> = TaskUserDao.observeAllActive()
    fun observeById(id: String): Flow<TaskUser?> = TaskUserDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<TaskUser>> = TaskUserDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<TaskUser>> = TaskUserDao.observeUnsyncedDeletes()

    suspend fun getActiveCount(): Int = TaskUserDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = TaskUserDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<TaskUser> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("No internet connection available")
            }

            val remotetask_users: List<TaskUserRemote> = SupabaseManager.getAll<TaskUserRemote>("task_users")

            remotetask_users.map { remote ->
                TaskUser(
                    id = remote.id,
                    serverId = remote.id,
                    createdAt = Instant.parse(remote.created_at),
                    updatedAt = Instant.parse(remote.updated_at),
                    deletedAt = remote.deleted_at?.let { Instant.parse(remote.deleted_at) },
                    isSynced = true,
                    taskId = remote.task_id,
                    userId = remote.user_id,
                    startDate = remote.start_date as Instant?,
                    endDate = remote.end_date as Instant?,
                    location = remote.location,
                    conclusionRate = remote.conclusion_rate,
                    timeUsed = remote.time_used
                )
            }
        } catch (e: Exception) {
            Log.e("TaskUserRepository", "Failed to fetch remote task_users", e)
            emptyList()
        }
    }


    suspend fun getByIdRemote(id: String): TaskUserRemote? {
        return try {
            val remoteTaskUser = SupabaseManager.fetchById<TaskUserRemote>("task_users", id)

            remoteTaskUser?.let { TaskUser ->
                TaskUserDao.update(TaskUser.toLocal())
            }

            return remoteTaskUser
        } catch (e: Exception) {
            Log.e("TaskUserRepository", "Error fetching remote TaskUser(normal if in test)", e)
            null
        }
    }
    suspend fun clearLocalDatabase() {
        TaskUserDao.deleteAll()
    }
}