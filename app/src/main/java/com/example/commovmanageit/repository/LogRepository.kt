import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.dao.LogsDao
import com.example.commovmanageit.db.entities.Logs
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.remote.dto.LogsRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.O)
class LogRepository(
    private val logDao: LogsDao,
    private val connectivityMonitor: ConnectivityMonitor,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    init {
        connectivityMonitor.registerListener { online ->
            if (online) {
                coroutineScope.launch {
                    syncIfConnected()
                }
            }
        }
    }

    suspend fun insertLocal(logs: Logs): Logs {
        logDao.insert(logs)
        syncIfConnected()
        return logs
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertRemote(logs: Logs): String {
        val remoteLog = logs.toRemote()
        val result = SupabaseManager.insertLog<LogsRemote>(remoteLog)
        return result.id
    }

    suspend fun deleteLocal(logs:Logs) {
        logDao.delete(logs)
        syncIfConnected()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(logs: Logs): Logs {
        val newLog = logs.copy(
            id = logs.id.ifEmpty { UUID.randomUUID().toString() }
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = insertRemote(newLog)
                val syncedLog = newLog.copy(isSynced = true, serverId = serverId)
                insertLocal(syncedLog)
                syncedLog
            } catch (e: Exception) {
                logDao.insert(newLog)
                newLog
            }
        } else {
            logDao.insert(newLog)
            newLog
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncIfConnected() {
        if (connectivityMonitor.isConnected) {
            try {
                syncChanges()
            } catch (e: Exception) {
                Log.d("LogRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        logDao.getUnsyncedCreated().forEach { logs ->
            try {
                if (logs.serverId == null) {
                    val serverId = insertRemote(logs)
                    logDao.markAsSynced(logs.id, serverId)
                } else {
                    deleteLocal(logs)
                }
            } catch (e: Exception) {
                Log.d("LogRepository", "Erro ao sincronizar cliente ${logs.id}", e)
            }
        }
    }

    suspend fun clearLocalDatabase() {
        logDao.deleteAll()
    }
}
