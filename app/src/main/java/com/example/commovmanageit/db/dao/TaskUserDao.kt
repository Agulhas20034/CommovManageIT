package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.commovmanageit.db.entities.TaskUser
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
interface TaskUserDao {
    @Insert
    suspend fun insert(taskUser: TaskUser)

    @Insert
    suspend fun insertAll(taskUsers: List<TaskUser>)

    @Update
    suspend fun update(taskUser: TaskUser)

    @Delete
    suspend fun delete(taskUser: TaskUser)

    @Query("SELECT * FROM task_users WHERE id = :id")
    suspend fun getById(id: String): TaskUser?

    @Query("SELECT * FROM task_users WHERE deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getAllActive(): List<TaskUser>

    @Query("SELECT * FROM task_users WHERE task_id = :taskId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByTaskId(taskId: String): List<TaskUser>

    @Query("SELECT * FROM task_users WHERE user_id = :userId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByUserId(userId: String): List<TaskUser>

    @Query("SELECT * FROM task_users WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<TaskUser>

    @Query("SELECT * FROM task_users WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<TaskUser>

    @Query("UPDATE task_users SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE task_users SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("UPDATE task_users SET deleted_at = :timestamp,is_synced=0 WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Instant = Clock.System.now())

    @Query("SELECT * FROM task_users WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Instant): List<TaskUser>

    @Query("SELECT * FROM task_users WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Instant): List<TaskUser>

    @Query("SELECT * FROM task_users WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun observeAllActive(): Flow<List<TaskUser>>

    @Query("SELECT * FROM task_users WHERE id = :id")
    fun observeById(id: String): Flow<TaskUser?>

    @Query("SELECT * FROM task_users WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<TaskUser>>

    @Query("SELECT * FROM task_users WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<TaskUser>>

    @Query("UPDATE task_users SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM task_users WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM task_users WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM task_users WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM task_users")
    suspend fun deleteAll()
}