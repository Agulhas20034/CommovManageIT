package com.example.commovmanageit.db.repositories
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.dao.ProjectUserDao
import com.example.commovmanageit.remote.dto.TaskRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.db.dao.TaskDao
import com.example.commovmanageit.db.dao.TaskUserDao
import com.example.commovmanageit.db.entities.Task
import com.example.commovmanageit.remote.dto.ProjectUserRemote
import com.example.commovmanageit.remote.dto.TaskUserRemote
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
class TaskRepository(
    private val TaskDao: TaskDao,
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

    suspend fun insertLocal(Task: Task): Task {
        TaskDao.insert(Task)
        syncIfConnected()
        return Task
    }

    suspend fun updateLocal(Task: Task) {
        Task.updatedAt = Clock.System.now()
        TaskDao.update(Task)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String, type: String) {
        TaskDao.softDelete(id)
        if (type.equals("Test"))
            return
        else
            syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): Task? = TaskDao.getById(id)
    suspend fun getAllLocal(): List<Task> = TaskDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(Task: Task): String {
        val remoteTask = Task.toRemote()
        val result = SupabaseManager.insertTask<TaskRemote>(remoteTask)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(Task: Task): TaskRemote {
        val remoteTask = Task.toRemote()
        Log.d("TaskRepository", "Updating remote Task: ${Task.id}")
        return SupabaseManager.updateTask<TaskRemote>(Task.serverId ?: Task.id, remoteTask)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("tasks", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(Task: Task): Task {
        val newTask = Task.copy(
            id = Task.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newTask.id
                val syncedTask = newTask.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedTask)
                insertLocal(syncedTask)

                syncedTask
            } catch (e: Exception) {
                TaskDao.insert(newTask)
                newTask
            }
        } else {
            TaskDao.insert(newTask)
            newTask
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(Task: Task) {
        val updatedTask = Task.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedTask.serverId != null) {
                    updateRemote(updatedTask)
                    Log.d("TaskRepository", "Updated remote Task: ${updatedTask.id}")
                    TaskDao.update(updatedTask.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedTask)
                    TaskDao.update(updatedTask.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                TaskDao.update(updatedTask)
            }
        } else {
            TaskDao.update(updatedTask)
        }
    }

    suspend fun delete(id: String) {
        try {
            val Task = TaskDao.getById(id)
            Task?.let {
                TaskDao.softDelete(id)

                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId!!)
                    TaskDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("Task not found")
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
                Log.d("TaskRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        TaskDao.getUnsyncedCreatedOrUpdated().forEach { Task ->
            try {
                if (Task.serverId == null) {
                    val serverId = insertRemote(Task)
                    TaskDao.markAsSynced(Task.id, serverId)
                } else {
                    updateRemote(Task)
                    TaskDao.updateSyncStatus(Task.id, true)
                }
            } catch (e: Exception) {
                Log.d("TaskRepository", "Erro ao sincronizar cliente ${Task.id}", e)
            }
        }

        TaskDao.getUnsyncedDeleted().forEach { Task ->
            try {
                Log.d("TaskRepository", "Deleting remote Task: ${Task.id}")
                Task.serverId?.let { deleteRemote(it) }
                TaskDao.updateSyncStatus(Task.id, true)
            } catch (e: Exception) {
            }
        }

        TaskDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<Task>> = TaskDao.observeAllActive()
    fun observeById(id: String): Flow<Task?> = TaskDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<Task>> = TaskDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<Task>> = TaskDao.observeUnsyncedDeletes()

    suspend fun getActiveCount(): Int = TaskDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = TaskDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<Task> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("No internet connection available")
            }

            val remotetasks: List<TaskRemote> = SupabaseManager.getAll<TaskRemote>("tasks")

            remotetasks.map { remote ->
                Task(
                    id = remote.id,
                    serverId = remote.id,
                    name = remote.name,
                    createdAt = Instant.parse(if (remote.created_at.endsWith("Z")) remote.created_at else remote.created_at + "Z"),
                    updatedAt = Instant.parse(if (remote.updated_at.endsWith("Z")) remote.updated_at else remote.updated_at + "Z"),
                    deletedAt = remote.deleted_at?.let { Instant.parse(if (it.endsWith("Z")) it else it + "Z") },
                    isSynced = true,
                    projectId = remote.project_id,
                    description = remote.description,
                    hourlyRate = remote.hourly_rate,
                    status = remote.status
                )
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to fetch remote tasks", e)
            emptyList()
        }
    }


    suspend fun getByIdRemote(id: String): TaskRemote? {
        return try {
            val remoteTask = SupabaseManager.fetchById<TaskRemote>("tasks", id)

            remoteTask?.let { Task ->
                TaskDao.update(Task.toLocal())
            }

            return remoteTask
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error fetching remote Task(normal if in test)", e)
            null
        }
    }

    suspend fun getByProjectIdRemote(id: String): List<TaskRemote>? {
        return try {
            val remoteProjectUser = SupabaseManager.fetchByUserId<TaskRemote>("tasks", id, "project_id")

            remoteProjectUser?.let { projectUser ->
                projectUser.forEach { TaskDao.update(it.toLocal()) }
            }

            return remoteProjectUser
        } catch (e: Exception) {
            Log.e("ProjectUserRepository", "Error fetching remote ProjectUser(normal if in test)", e)
            null
        }
    }
    suspend fun clearLocalDatabase() {
        TaskDao.deleteAll()
    }
}