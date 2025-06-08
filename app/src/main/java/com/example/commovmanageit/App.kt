package com.example.commovmanageit
import LogRepository
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.example.commovmanageit.db.AppDatabase
import com.example.commovmanageit.db.repositories.CustomerRepository
import com.example.commovmanageit.repository.PermissionRepository
import com.example.commovmanageit.utils.ConnectivityMonitor
import com.example.commovmanageit.utils.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    lateinit var customerRepository: CustomerRepository
    lateinit var logsRepository: LogRepository
    lateinit var permissionRepository: PermissionRepository


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
        connectivityMonitor = ConnectivityMonitor(this)
        // Inicialize banco de dados, monitor de conectividade, etc.

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(5, java.util.concurrent.TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

    }
}
