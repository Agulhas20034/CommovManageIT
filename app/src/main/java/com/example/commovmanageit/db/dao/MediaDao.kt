package com.example.commovmanageit.db.dao

import androidx.room.*
import com.example.commovmanageit.db.entities.Media
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert
    suspend fun insert(media: Media)

    @Update
    suspend fun update(media: Media)

    @Delete
    suspend fun delete(media: Media)

    @Query("SELECT * FROM media WHERE id = :id")
    suspend fun getById(id: String): Media?

    @Query("UPDATE media SET deleted_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM media WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<Media>

    @Query("SELECT * FROM media WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<Media>

    @Query("UPDATE media SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE media SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("SELECT * FROM media WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByProject(projectId: String): List<Media>

    @Query("SELECT * FROM media WHERE report_id = :reportId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByReport(reportId: String): List<Media>

    @Query("SELECT * FROM media WHERE type = :type AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByType(type: String): List<Media>

    @Query("SELECT DISTINCT type FROM media WHERE deleted_at IS NULL")
    suspend fun getAvailableTypes(): List<String>

    @Query("SELECT * FROM media WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Long): List<Media>

    @Query("SELECT * FROM media WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Long): List<Media>

    @Query("SELECT * FROM media WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun observeAllActive(): Flow<List<Media>>

    @Query("SELECT * FROM media WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    fun observeByProject(projectId: String): Flow<List<Media>>

    @Query("SELECT * FROM media WHERE report_id = :reportId AND deleted_at IS NULL ORDER BY created_at DESC")
    fun observeByReport(reportId: String): Flow<List<Media>>

    @Query("SELECT * FROM media WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<Media>>

    @Query("SELECT * FROM media WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<Media>>

    @Insert
    suspend fun insertAll(mediaList: List<Media>)

    @Query("UPDATE media SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM media WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("DELETE FROM media WHERE project_id = :projectId")
    suspend fun deleteByProject(projectId: String)

    @Query("UPDATE media SET report_id = NULL WHERE report_id = :reportId")
    suspend fun detachFromReport(reportId: String)

    @Query("SELECT COUNT(*) FROM media WHERE project_id = :projectId AND deleted_at IS NULL")
    suspend fun countByProject(projectId: String): Int

    @Query("SELECT COUNT(*) FROM media WHERE report_id = :reportId AND deleted_at IS NULL")
    suspend fun countByReport(reportId: String): Int

    @Query("SELECT COUNT(*) FROM media WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int
}