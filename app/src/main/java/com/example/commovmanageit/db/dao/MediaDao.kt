package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.commovmanageit.db.entities.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
interface MediaDao {
    @Insert
    suspend fun insert(media: Media)

    @Insert
    suspend fun insertAll(mediaList: List<Media>)

    @Update
    suspend fun update(media: Media)

    @Delete
    suspend fun delete(media: Media)

    @Query("SELECT * FROM media WHERE id = :id")
    suspend fun getById(id: String): Media?

    @Query("SELECT * FROM media WHERE deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getAllActive(): List<Media>

    @Query("SELECT * FROM media WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsynced(): List<Media>

    @Query("UPDATE media SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("UPDATE media SET deleted_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Instant = Clock.System.now())

    @Query("SELECT * FROM media WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByProjectId(projectId: String): List<Media>

    @Query("SELECT * FROM media WHERE report_id = :reportId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByReportId(reportId: String): List<Media>

    @Query("SELECT * FROM media WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Instant): List<Media>

    @Query("SELECT * FROM media WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Instant): List<Media>

    @Query("SELECT * FROM media WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun observeAllActive(): Flow<List<Media>>

    @Query("SELECT * FROM media WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsynced(): Flow<List<Media>>

    @Query("DELETE FROM media WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM media WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM media WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM media")
    suspend fun deleteAll()
}