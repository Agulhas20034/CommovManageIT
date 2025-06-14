package com.example.commovmanageit
import LogRepository
import ProjectRepository
import UsersRepository
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.example.commovmanageit.db.AppDatabase
import com.example.commovmanageit.db.dao.TaskUserDao
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.db.entities.Permission
import com.example.commovmanageit.db.entities.Project
import com.example.commovmanageit.db.entities.ProjectUser
import com.example.commovmanageit.db.entities.Report
import com.example.commovmanageit.db.entities.Role
import com.example.commovmanageit.db.entities.Task
import com.example.commovmanageit.db.entities.User
import com.example.commovmanageit.db.repositories.CustomerRepository
import com.example.commovmanageit.db.repositories.MediaRepository
import com.example.commovmanageit.db.repositories.ProjectUserRepository
import com.example.commovmanageit.db.repositories.ReportRepository
import com.example.commovmanageit.db.repositories.RoleRepository
import com.example.commovmanageit.db.repositories.TaskRepository
import com.example.commovmanageit.db.repositories.TaskUserRepository
import com.example.commovmanageit.remote.dto.PermissionRemote
import com.example.commovmanageit.remote.dto.ReportRemote
import com.example.commovmanageit.remote.dto.TaskUserRemote
import com.example.commovmanageit.repository.PermissionRepository
import com.example.commovmanageit.utils.ConnectivityMonitor
import com.example.commovmanageit.utils.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class App : Application() {

    lateinit var customerRepository: CustomerRepository
    lateinit var logsRepository: LogRepository
    lateinit var permissionRepository: PermissionRepository
    lateinit var roleRepository: RoleRepository
    lateinit var userRepository: UsersRepository
    lateinit var projectRepository: ProjectRepository
    lateinit var projectusersRepository: ProjectUserRepository
    lateinit var mediaRepository: MediaRepository
    lateinit var reportRepository: ReportRepository
    lateinit var taskRepository: TaskRepository
    lateinit var taskuserRepository: TaskUserRepository
    lateinit var currentLanguage: String
    var currentUser: User? = null
    fun logout() {
        currentUser = null
    }
    fun getSavedLanguage(context: Context): String {
        val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("language", "en") ?: "en"
    }
    fun loadLanguage(context: Context) {
        val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        currentLanguage = sharedPref.getString("language", "en") ?: "en"
        updateLanguage(context, currentLanguage)
    }

    fun saveLanguage(context: Context, lang: String) {
        val editor = context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
        editor.putString("language", lang)
        editor.apply()
    }

    fun updateLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    val appCoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    lateinit var connectivityMonitor: ConnectivityMonitor
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        currentLanguage = "en"
        val db = AppDatabase.getDatabase(this)
        customerRepository = CustomerRepository(
            customerDao = db.customerDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        logsRepository = LogRepository(
            logDao = db.logsDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        permissionRepository = PermissionRepository(
            permissionDao = db.permissionDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        roleRepository = RoleRepository(
            roleDao = db.roleDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        userRepository = UsersRepository(
            UsersDao = db.userDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        projectRepository = ProjectRepository(
            ProjectDao = db.projectDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        projectusersRepository = ProjectUserRepository(
            ProjectUserDao = db.projectUserDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        mediaRepository = MediaRepository(
            MediaDao = db.mediaDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        reportRepository = ReportRepository(
            ReportDao = db.reportDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        taskRepository = TaskRepository(
            TaskDao = db.taskDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        taskuserRepository = TaskUserRepository(
            TaskUserDao = db.taskUserDao(),
            connectivityMonitor = ConnectivityMonitor(this),
            coroutineScope = appCoroutineScope
        )
        appCoroutineScope.launch{
            if(permissionRepository.getByIdRemote("1") == null) {
                permissionRepository.insertRemote(
                    Permission(
                        id = "1",
                        label = "Admin",
                        isSynced = true,
                        serverId = "1"
                    )
                )
            }
            if(roleRepository.getByIdRemote("1") == null && roleRepository.getByIdRemote("2") == null && roleRepository.getByIdRemote("3") == null) {
                roleRepository.insertRemote(
                    Role(
                        id = "1",
                        name = "Admin",
                        isSynced = true,
                        serverId = "1",
                        permissionId = "1"
                    )
                )
                roleRepository.insertRemote(
                    Role(
                        id = "2",
                        name = "UserManager",
                        isSynced = true,
                        serverId = "2",
                        permissionId = "1"
                    )
                )
                roleRepository.insertRemote(
                    Role(
                        id = "3",
                        name = "User",
                        isSynced = true,
                        serverId = "3",
                        permissionId = "1"
                    )
                )
            }
            if(userRepository.getByIdRemote("1") == null && userRepository.getByIdRemote("2") == null && userRepository.getByIdRemote("3") == null) {
                userRepository.insertRemote(
                    User(
                        id = "1",
                        email = "test1@email.com",
                        isSynced = true,
                        serverId = "1",
                        roleId = "1",
                        dailyWorkHours = 8,
                        password = "teste"
                    )
                )
                userRepository.insertRemote(
                    User(
                        id = "2",
                        email = "test2@email.com",
                        isSynced = true,
                        serverId = "2",
                        roleId = "2",
                        dailyWorkHours = 8,
                        password = "teste"
                    )
                )
                userRepository.insertRemote(
                    User(
                        id = "3",
                        email = "test3@email.com",
                        isSynced = true,
                        serverId = "3",
                        roleId = "3",
                        dailyWorkHours = 8,
                        password = "teste"
                    )
                )
            }
            if(customerRepository.getByIdRemote("1") == null) {
                customerRepository.insertRemote(
                    Customer(
                        id = "1",
                        email = "Admin",
                        isSynced = true,
                        serverId = "1",
                        name = "teste",
                        phone_Number = "teste"
                    )
                )
            }
            if(projectRepository.getByIdRemote("1") == null) {
                projectRepository.insertRemote(
                    Project(
                        id = "1",
                        isSynced = true,
                        serverId = "1",
                        name = "teste",
                        userId = "1",
                        customerId = "1",
                        hourlyRate = 1.0F,
                        dailyWorkHours = 1,
                        description = "teste"
                    )
                )
            }
            if(projectusersRepository.getByIdRemote("1") == null) {
                projectusersRepository.insertRemote(
                    ProjectUser(
                        id = "1",
                        isSynced = true,
                        serverId = "1",
                        userId = "1",
                        projectId = "1",
                        inviterId = "1",
                        speed = 1,
                        quality = 1,
                        collaboration = 1,
                        status = "active"
                    )
                )
            }
            if(reportRepository.getByIdRemote("1") == null) {
                reportRepository.insertRemote(
                    Report(
                        id = "1",
                        isSynced = true,
                        serverId = "1",
                        userId = "1",
                        projectId = "1",
                    )
                )
            }
            if(taskRepository.getByIdRemote("1") == null) {
                taskRepository.insertRemote(
                    Task(
                        id = "1",
                        isSynced = true,
                        serverId = "1",
                        projectId = "1",
                        name = "test",
                        description = "test",
                        hourlyRate = 1f,
                        status = "open",
                    )
                )
            }

        }
        connectivityMonitor = ConnectivityMonitor(this)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(5, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

    }
}
