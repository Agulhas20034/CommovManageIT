package com.example.commovmanageit.db.dao

import androidx.room.*
import com.example.commovmanageit.db.entities.Report
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert
    suspend fun insert(report: Report): Long

    @Update
    suspend fun update(report: Report)

    @Delete
    suspend fun delete(report: Report)

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getById(id: String): Report?

    @Query("SELECT * FROM reports WHERE user_id = :userId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByUser(userId: String): List<Report>

    @Query("SELECT * FROM reports WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByProject(projectId: String): List<Report>

    @Query("SELECT * FROM reports WHERE created_at BETWEEN :start AND :end AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<Report>

    @Query("SELECT * FROM reports WHERE created_at >= :timestamp AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedAfter(timestamp: Long): List<Report>

    @Query("SELECT * FROM reports WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<Report>

    @Query("SELECT * FROM reports WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<Report>

    @Query("UPDATE reports SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE reports SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("SELECT * FROM reports WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun observeAll(): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    fun observeByProject(projectId: String): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE user_id = :userId AND deleted_at IS NULL ORDER BY created_at DESC")
    fun observeByUser(userId: String): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<Report>>

    @Insert
    suspend fun insertAll(reports: List<Report>)

    @Query("UPDATE reports SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM reports WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("UPDATE reports SET user_id = NULL WHERE user_id = :userId")
    suspend fun detachFromUser(userId: String)

    @Query("DELETE FROM reports WHERE project_id = :projectId")
    suspend fun deleteByProject(projectId: String)

    @Query("SELECT COUNT(*) FROM reports WHERE project_id = :projectId AND deleted_at IS NULL")
    suspend fun countByProject(projectId: String): Int

    @Query("SELECT COUNT(*) FROM reports WHERE user_id = :userId AND deleted_at IS NULL")
    suspend fun countByUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM reports WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("SELECT * FROM reports WHERE project_id = :projectId AND user_id = :userId AND deleted_at IS NULL ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestByProjectAndUser(projectId: String, userId: String): Report?
}