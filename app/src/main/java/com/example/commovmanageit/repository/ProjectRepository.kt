import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.dao.ProjectDao
import com.example.commovmanageit.db.entities.Project
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.remote.dto.ProjectRemote
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.collections.map
import kotlin.text.ifEmpty

@RequiresApi(Build.VERSION_CODES.O)
class ProjectRepository(
    private val ProjectDao: ProjectDao,
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

    suspend fun insertLocal(Project: Project): Project {
        ProjectDao.insert(Project)
        syncIfConnected()
        return Project
    }

    suspend fun updateLocal(Project: Project) {
        Project.updatedAt = Clock.System.now()
        ProjectDao.update(Project)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String,type: String) {
        ProjectDao.softDelete(id)
        if(type.equals("Test"))
            return
        else
            syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): Project? = ProjectDao.getById(id)
    suspend fun getAllLocal(): List<Project> = ProjectDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(Project: Project): String {
        val remoteProject = Project.toRemote()
        val result = SupabaseManager.insertProject<ProjectRemote>(remoteProject)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(Project: Project): ProjectRemote {
        val remoteProject = Project.toRemote()
        Log.d("ProjectRepository", "Updating remote Project: ${Project.id}")
        return SupabaseManager.updateProject<ProjectRemote>(Project.serverId ?: Project.id, remoteProject)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("Projects", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(Project: Project): Project {
        val newProject = Project.copy(
            id = Project.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newProject.id
                val syncedProject = newProject.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedProject)
                insertLocal(syncedProject)
                syncedProject
            } catch (e: Exception) {
                ProjectDao.insert(newProject)
                newProject
            }
        } else {
            ProjectDao.insert(newProject)
            newProject
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(Project: Project) {
        val updatedProject = Project.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedProject.serverId != null) {
                    updateRemote(updatedProject)
                    Log.d("ProjectRepository", "Updated remote Project: ${updatedProject.id}")
                    ProjectDao.update(updatedProject.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedProject)
                    ProjectDao.update(updatedProject.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                ProjectDao.update(updatedProject)
            }
        } else {
            ProjectDao.update(updatedProject)
        }
    }

    suspend fun delete(id: String) {
        try {
            val project = ProjectDao.getById(id)
            if (project == null) {
                throw Exception("Project with ID $id not found")
            }else {
                Log.d("Delete atempt: ","Project with ID $id found")
            }
            project?.let {
                ProjectDao.softDelete(it.id)
                Log.d("ProjectRepository", "Soft deleting Project: ${it.serverId}")
                if (it.serverId != null && connectivityMonitor.isConnected) {
                    Log.d("ProjectRepository", "Deleting remote Project: ${it.id}")
                    deleteRemote(it.serverId)
                    ProjectDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("Project not found")
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
                Log.d("ProjectRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        ProjectDao.getUnsyncedCreatedOrUpdated().forEach { Project ->
            try {
                if (Project.serverId == null) {
                    val serverId = insertRemote(Project)
                    ProjectDao.markAsSynced(Project.id, serverId)
                } else {
                    updateRemote(Project)
                    ProjectDao.updateSyncStatus(Project.id, true)
                }
            } catch (e: Exception) {
                Log.d("ProjectRepository", "Erro ao sincronizar cliente ${Project.id}", e)
            }
        }

        ProjectDao.getUnsyncedDeleted().forEach { Project ->
            try {
                Log.d("ProjectRepository", "Deleting remote Project: ${Project.id}")
                Project.serverId?.let { deleteRemote(it) }
                ProjectDao.updateSyncStatus(Project.id, true)
            } catch (e: Exception) {
            }
        }

        ProjectDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<Project>> = ProjectDao.observeAllActive()
    fun observeById(id: String): Flow<Project?> = ProjectDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<Project>> = ProjectDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<Project>> = ProjectDao.observeUnsyncedDeletes()

    suspend fun getActiveCount(): Int = ProjectDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = ProjectDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<Project> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("No internet connection available")
            }

            val remoteProjects: List<ProjectRemote> = SupabaseManager.getAll<ProjectRemote>("projects")

            remoteProjects.map { remote ->
                Project(
                    id = remote.id,
                    serverId = remote.id,
                    name = remote.name,
                    createdAt = Instant.parse(remote.created_at),
                    updatedAt = Instant.parse(remote.updated_at),
                    deletedAt = remote.deleted_at?.let { Instant.parse(remote.deleted_at) },
                    isSynced = true,
                    userId = remote.user_id,
                    customerId = remote.customer_id,
                    hourlyRate = remote.hourly_rate,
                    dailyWorkHours = remote.daily_work_hours
                )
            }
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Failed to fetch remote Projects", e)
            emptyList()
        }
    }


    suspend fun getByIdRemote(id: String): ProjectRemote? {
        return try {
            Log.d("ProjectRepository", "Fetching remote Project by ID: $id")
            val remoteProject = SupabaseManager.fetchById<ProjectRemote>("projects", id)

            remoteProject?.let { project ->
                ProjectDao.update(project.toLocal())
            }

            return remoteProject
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error fetching remote Project(normal if in test)", e)
            null
        }
    }
    suspend fun clearLocalDatabase() {
        ProjectDao.deleteAll()
    }
}