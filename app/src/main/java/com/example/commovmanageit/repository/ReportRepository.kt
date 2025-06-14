package com.example.commovmanageit.db.repositories
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.remote.dto.ReportRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.db.dao.ReportDao
import com.example.commovmanageit.db.entities.Report
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
class ReportRepository(
    private val ReportDao: ReportDao,
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

    suspend fun insertLocal(Report: Report): Report {
        ReportDao.insert(Report)
        syncIfConnected()
        return Report
    }

    suspend fun updateLocal(Report: Report) {
        Report.updatedAt = Clock.System.now()
        ReportDao.update(Report)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String,type: String) {
        ReportDao.softDelete(id)
        if(type.equals("Test"))
            return
        else
            syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): Report? = ReportDao.getById(id)
    suspend fun getAllLocal(): List<Report> = ReportDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(Report: Report): String {
        val remoteReport = Report.toRemote()
        val result = SupabaseManager.insertReport<ReportRemote>(remoteReport)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(Report: Report): ReportRemote {
        val remoteReport = Report.toRemote()
        Log.d("ReportRepository", "Updating remote Report: ${Report.id}")
        return SupabaseManager.updateReport<ReportRemote>(Report.serverId ?: Report.id, remoteReport)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("reports", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(Report: Report): Report {
        val newReport = Report.copy(
            id = Report.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newReport.id
                val syncedReport = newReport.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedReport)
                insertLocal(syncedReport)

                syncedReport
            } catch (e: Exception) {
                ReportDao.insert(newReport)
                newReport
            }
        } else {
            ReportDao.insert(newReport)
            newReport
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(Report: Report) {
        val updatedReport = Report.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedReport.serverId != null) {
                    updateRemote(updatedReport)
                    Log.d("ReportRepository", "Updated remote Report: ${updatedReport.id}")
                    ReportDao.update(updatedReport.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedReport)
                    ReportDao.update(updatedReport.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                ReportDao.update(updatedReport)
            }
        } else {
            ReportDao.update(updatedReport)
        }
    }

    suspend fun delete(id: String) {
        try {
            val Report = ReportDao.getById(id)
            Report?.let {
                ReportDao.softDelete(id)

                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId!!)
                    ReportDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("Report not found")
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
                Log.d("ReportRepository", "Sync failed: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        ReportDao.getUnsyncedCreatedOrUpdated().forEach { Report ->
            try {
                if (Report.serverId == null) {
                    val serverId = insertRemote(Report)
                    ReportDao.markAsSynced(Report.id, serverId)
                } else {
                    updateRemote(Report)
                    ReportDao.updateSyncStatus(Report.id, true)
                }
            } catch (e: Exception) {
                Log.d("ReportRepository", "Erro ao sincronizar cliente ${Report.id}", e)
            }
        }

        ReportDao.getUnsyncedDeleted().forEach { Report ->
            try {
                Log.d("ReportRepository", "Deleting remote Report: ${Report.id}")
                Report.serverId?.let { deleteRemote(it) }
                ReportDao.updateSyncStatus(Report.id, true)
            } catch (e: Exception) {
            }
        }

        ReportDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<Report>> = ReportDao.observeAllActive()
    fun observeById(id: String): Flow<Report?> = ReportDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<Report>> = ReportDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<Report>> = ReportDao.observeUnsyncedDeletes()

    suspend fun getActiveCount(): Int = ReportDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = ReportDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<Report> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("No internet connection available")
            }

            val remotereports: List<ReportRemote> = SupabaseManager.getAll<ReportRemote>("reports")

            remotereports.map { remote ->
                Report(
                    id = remote.id,
                    serverId = remote.id,
                    createdAt = Instant.parse(remote.created_at),
                    updatedAt = Instant.parse(remote.updated_at),
                    deletedAt = remote.deleted_at?.let { Instant.parse(remote.deleted_at) },
                    isSynced = true,
                    userId = remote.user_id,
                    projectId = remote.project_id
                )
            }
        } catch (e: Exception) {
            Log.e("ReportRepository", "Failed to fetch remote reports", e)
            emptyList()
        }
    }


    suspend fun getByIdRemote(id: String): ReportRemote? {
        return try {
            val remoteReport = SupabaseManager.fetchById<ReportRemote>("reports", id)

            remoteReport?.let { Report ->
                ReportDao.update(Report.toLocal())
            }

            return remoteReport
        } catch (e: Exception) {
            Log.e("ReportRepository", "Error fetching remote Report(normal if in test)", e)
            null
        }
    }
    suspend fun clearLocalDatabase() {
        ReportDao.deleteAll()
    }
}