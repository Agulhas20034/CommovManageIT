import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.dao.TaskDao
import com.example.commovmanageit.db.dao.UserDao
import com.example.commovmanageit.db.entities.User
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.remote.dto.ProjectUserRemote
import com.example.commovmanageit.remote.dto.TaskRemote
import com.example.commovmanageit.remote.dto.UserRemote
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
class UsersRepository(
    private val UsersDao: UserDao,
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

    suspend fun insertLocal(Users: User): User {
        UsersDao.insert(Users)
        syncIfConnected()
        return Users
    }

    suspend fun updateLocal(Users: User) {
        Users.updatedAt = Clock.System.now()
        UsersDao.update(Users)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String,type: String) {
        UsersDao.softDelete(id)
        if(type.equals("Test"))
            return
        else
            syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): User? = UsersDao.getById(id)
    suspend fun getAllLocal(): List<User> = UsersDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(Users: User): String {
        val remoteUsers = Users.toRemote()
        val result = SupabaseManager.insertUser<UserRemote>(remoteUsers)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(Users: User): UserRemote {
        val remoteUsers = Users.toRemote()
        Log.d("UsersRepository", "Updating remote Users: ${Users.id}")
        return SupabaseManager.updateUser<UserRemote>(Users.serverId ?: Users.id, remoteUsers)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("users", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(Users: User): User {
        val newUsers = Users.copy(
            id = Users.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newUsers.id
                val syncedUsers = newUsers.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedUsers)
                insertLocal(syncedUsers)
                syncedUsers
            } catch (e: Exception) {
                UsersDao.insert(newUsers)
                newUsers
            }
        } else {
            UsersDao.insert(newUsers)
            newUsers
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(Users: User) {
        val updatedUsers = Users.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedUsers.serverId != null) {
                    updateRemote(updatedUsers)
                    Log.d("UsersRepository", "Updated remote Users: ${updatedUsers.id}")
                    UsersDao.update(updatedUsers.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedUsers)
                    UsersDao.update(updatedUsers.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                UsersDao.update(updatedUsers)
            }
        } else {
            UsersDao.update(updatedUsers)
        }
    }

    suspend fun delete(id: String) {
        try {
            val Users = UsersDao.getById(id)
            Users?.let {
                UsersDao.softDelete(id)

                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId!!)
                    UsersDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("Users not found")
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
                Log.d("UsersRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        UsersDao.getUnsyncedCreatedOrUpdated().forEach { Users ->
            try {
                if (Users.serverId == null) {
                    val serverId = insertRemote(Users)
                    UsersDao.markAsSynced(Users.id, serverId)
                } else {
                    updateRemote(Users)
                    UsersDao.updateSyncStatus(Users.id, true)
                }
            } catch (e: Exception) {
                Log.d("UsersRepository", "Erro ao sincronizar cliente ${Users.id}", e)
            }
        }

        UsersDao.getUnsyncedDeleted().forEach { Users ->
            try {
                Log.d("UsersRepository", "Deleting remote Users: ${Users.id}")
                Users.serverId?.let { deleteRemote(it) }
                UsersDao.updateSyncStatus(Users.id, true)
            } catch (e: Exception) {
            }
        }

        UsersDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<User>> = UsersDao.observeAllActive()
    fun observeById(id: String): Flow<User?> = UsersDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<User>> = UsersDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<User>> = UsersDao.observeUnsyncedDeletes()

    suspend fun getByEmail(email: String): User? = UsersDao.getByEmail(email)
    suspend fun getActiveCount(): Int = UsersDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = UsersDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<User> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("No internet connection available")
            }

            val remoteUserss: List<UserRemote> = SupabaseManager.getAll<UserRemote>("users")

            remoteUserss.map { remote ->
                User(
                    id = remote.id,
                    serverId = remote.id,
                    email = remote.email,
                    createdAt = Instant.parse(if (remote.created_at.endsWith("Z") || remote.created_at.contains("+")) remote.created_at else remote.created_at + "Z"),
                    updatedAt = Instant.parse(if (remote.updated_at.endsWith("Z") || remote.updated_at.contains("+")) remote.updated_at else remote.updated_at + "Z"),
                    deletedAt = remote.deleted_at?.let { Instant.parse(if (it.endsWith("Z") || it.contains("+")) it else it + "Z") },
                    isSynced = true,
                    roleId = remote.role_id,
                    password = remote.password.toString(),
                    dailyWorkHours = remote.daily_work_hours
                )
            }
        } catch (e: Exception) {
            Log.e("UsersRepository", "Failed to fetch remote Users", e)
            emptyList()
        }
    }


    suspend fun getByIdRemote(id: String): UserRemote? {
        return try {
            val remoteUsers = SupabaseManager.fetchById<UserRemote>("users", id)

            remoteUsers?.let { Users ->
                UsersDao.update(Users.toLocal())
            }

            return remoteUsers
        } catch (e: Exception) {
            Log.e("UsersRepository", "Error fetching remote Users(normal if in test)", e)
            null
        }
    }

    suspend fun getByProjectIdRemote(projectId: String): List<UserRemote>? {
        return try {
            // Busca os user_ids na tabela projectusers
            val projectUsers = SupabaseManager.fetchByUserId<ProjectUserRemote>("project_users", projectId, "project_id")
            val userIds = projectUsers.map { it.user_id }
            // Busca os usuários na tabela users usando os ids encontrados
            val users = userIds.map { userId ->
                Log.d("UsersRepository", "Fetching remote Users by ID: $userId")
                SupabaseManager.fetchById<UserRemote>("users", userId)
            }
            users.forEach { UsersDao.update(it.toLocal()) }
            users
        } catch (e: Exception) {
            Log.e("UsersRepository", "Erro ao buscar usuários do projeto (normal se em teste)", e)
            null
        }
    }

    suspend fun getByemailRemote(email: String): UserRemote? {
        return try {
            val remoteUsers = SupabaseManager.fetchByEmail<UserRemote>("users", email)

            remoteUsers?.let { Users ->
                UsersDao.update(Users.toLocal())
            }

            return remoteUsers
        } catch (e: Exception) {
            Log.e("UsersRepository", "Error fetching remote Users(normal if in test)", e)
            null
        }
    }
    suspend fun clearLocalDatabase() {
        UsersDao.deleteAll()
    }
}