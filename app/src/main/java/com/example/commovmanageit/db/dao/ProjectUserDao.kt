package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.commovmanageit.db.entities.ProjectUser
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
interface ProjectUserDao {
    @Insert
    suspend fun insert(projectUser: ProjectUser)

    @Insert
    suspend fun insertAll(projectUsers: List<ProjectUser>)

    @Update
    suspend fun update(projectUser: ProjectUser)

    @Delete
    suspend fun delete(projectUser: ProjectUser)

    @Query("SELECT * FROM project_users WHERE id = :id")
    suspend fun getById(id: String): ProjectUser?

    @Query("SELECT * FROM project_users WHERE deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getAllActive(): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByProjectId(projectId: String): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE user_id = :userId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByUserId(userId: String): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<ProjectUser>

    @Query("UPDATE project_users SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE project_users SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("UPDATE project_users SET deleted_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Instant = Clock.System.now())

    @Query("SELECT * FROM project_users WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Instant): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Instant): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun observeAllActive(): Flow<List<ProjectUser>>

    @Query("SELECT * FROM project_users WHERE id = :id")
    fun observeById(id: String): Flow<ProjectUser?>

    @Query("SELECT * FROM project_users WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<ProjectUser>>

    @Query("SELECT * FROM project_users WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<ProjectUser>>

    @Query("UPDATE project_users SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM project_users WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM project_users WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM project_users WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM project_users")
    suspend fun deleteAll()
}