package com.example.commovmanageit
import LogRepository
import ProjectRepository
import UsersRepository
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.example.commovmanageit.db.AppDatabase
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.db.entities.Permission
import com.example.commovmanageit.db.entities.Role
import com.example.commovmanageit.db.entities.User
import com.example.commovmanageit.db.repositories.CustomerRepository
import com.example.commovmanageit.db.repositories.RoleRepository
import com.example.commovmanageit.remote.dto.PermissionRemote
import com.example.commovmanageit.repository.PermissionRepository
import com.example.commovmanageit.utils.ConnectivityMonitor
import com.example.commovmanageit.utils.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class App : Application() {

    lateinit var customerRepository: CustomerRepository
    lateinit var logsRepository: LogRepository
    lateinit var permissionRepository: PermissionRepository
    lateinit var roleRepository: RoleRepository
    lateinit var userRepository: UsersRepository
    lateinit var projectRepository: ProjectRepository




    val appCoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    lateinit var connectivityMonitor: ConnectivityMonitor
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
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
            if(roleRepository.getByIdRemote("1") == null) {
                roleRepository.insertRemote(
                    Role(
                        id = "1",
                        name = "Admin",
                        isSynced = true,
                        serverId = "1",
                        permissionId = "1"
                    )
                )
            }
            if(userRepository.getByIdRemote("1") == null) {
                userRepository.insertRemote(
                    User(
                        id = "1",
                        email = "Admin",
                        isSynced = true,
                        serverId = "1",
                        roleId = "1",
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
