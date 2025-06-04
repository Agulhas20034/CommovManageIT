package com.example.commovmanageit.db.dao

import androidx.room.*
import com.example.commovmanageit.db.entities.ProjectUser
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectUserDao {
    @Insert
    suspend fun insert(projectUser: ProjectUser)

    @Update
    suspend fun update(projectUser: ProjectUser)

    @Delete
    suspend fun delete(projectUser: ProjectUser)

    @Query("SELECT * FROM project_users WHERE id = :id")
    suspend fun getById(id: String): ProjectUser?

    @Query("SELECT * FROM project_users WHERE project_id = :projectId AND user_id = :userId")
    suspend fun getByProjectAndUser(projectId: String, userId: String): ProjectUser?

    @Query("DELETE FROM project_users WHERE project_id = :projectId AND user_id = :userId")
    suspend fun deleteByProjectAndUser(projectId: String, userId: String)

    @Query("UPDATE project_users SET deleted_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM project_users WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<ProjectUser>

    @Query("UPDATE project_users SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE project_users SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("SELECT * FROM project_users WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByProject(projectId: String): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE user_id = :userId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByUser(userId: String): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE inviter_id = :inviterId AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByInviter(inviterId: String): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE status = :status AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getByStatus(status: String): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE speed >= :minSpeed AND deleted_at IS NULL ORDER BY speed DESC")
    suspend fun getByMinSpeed(minSpeed: Int): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE quality >= :minQuality AND deleted_at IS NULL ORDER BY quality DESC")
    suspend fun getByMinQuality(minQuality: Int): List<ProjectUser>

    @Query("SELECT * FROM project_users WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY created_at DESC")
    fun observeByProject(projectId: String): Flow<List<ProjectUser>>

    @Query("SELECT * FROM project_users WHERE user_id = :userId AND deleted_at IS NULL ORDER BY created_at DESC")
    fun observeByUser(userId: String): Flow<List<ProjectUser>>

    @Query("SELECT * FROM project_users WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<ProjectUser>>

    @Query("SELECT * FROM project_users WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<ProjectUser>>

    @Insert
    suspend fun insertAll(projectUsers: List<ProjectUser>)

    @Query("UPDATE project_users SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM project_users WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("DELETE FROM project_users WHERE project_id = :projectId")
    suspend fun deleteByProject(projectId: String)

    @Query("DELETE FROM project_users WHERE user_id = :userId")
    suspend fun deleteByUser(userId: String)

    @Query("UPDATE project_users SET inviter_id = NULL WHERE inviter_id = :inviterId")
    suspend fun clearInviterReferences(inviterId: String)

    @Query("SELECT AVG(speed) FROM project_users WHERE project_id = :projectId AND deleted_at IS NULL AND speed IS NOT NULL")
    suspend fun getAverageSpeedForProject(projectId: String): Float?

    @Query("SELECT AVG(quality) FROM project_users WHERE project_id = :projectId AND deleted_at IS NULL AND quality IS NOT NULL")
    suspend fun getAverageQualityForProject(projectId: String): Float?

    @Query("SELECT COUNT(*) FROM project_users WHERE project_id = :projectId AND deleted_at IS NULL")
    suspend fun countByProject(projectId: String): Int

    @Query("SELECT COUNT(*) FROM project_users WHERE user_id = :userId AND deleted_at IS NULL")
    suspend fun countByUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM project_users WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int
}