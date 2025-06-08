package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.commovmanageit.db.entities.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
interface ReportDao {
    @Insert
    suspend fun insert(report: Report)

    @Insert
    suspend fun insertAll(reports: List<Report>)

    @Update
    suspend fun update(report: Report)

    @Delete
    suspend fun delete(report: Report)

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getById(id: String): Report?

    @Query("SELECT * FROM reports WHERE deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getAllActive(): List<Report>

    @Query("SELECT * FROM reports WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByProjectId(projectId: String): List<Report>

    @Query("SELECT * FROM reports WHERE user_id = :userId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByUserId(userId: String): List<Report>

    @Query("SELECT * FROM reports WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<Report>

    @Query("SELECT * FROM reports WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<Report>

    @Query("UPDATE reports SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE reports SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("UPDATE reports SET deleted_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Instant = Clock.System.now())

    @Query("SELECT * FROM reports WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Instant): List<Report>

    @Query("SELECT * FROM reports WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Instant): List<Report>

    @Query("SELECT * FROM reports WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun observeAllActive(): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE id = :id")
    fun observeById(id: String): Flow<Report?>

    @Query("SELECT * FROM reports WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<Report>>

    @Query("UPDATE reports SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM reports WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM reports WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM reports WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM reports")
    suspend fun deleteAll()
}