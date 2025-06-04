package com.example.commovmanageit.db.dao

import androidx.room.*
import com.example.commovmanageit.db.entities.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert
    suspend fun insert(project: Project)

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: String): Project?

    @Query("UPDATE projects SET deleted_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM projects WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<Project>

    @Query("SELECT * FROM projects WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<Project>

    @Query("UPDATE projects SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE projects SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("SELECT * FROM projects WHERE user_id = :userId AND deleted_at IS NULL ORDER BY name ASC")
    suspend fun getByUser(userId: String): List<Project>

    @Query("SELECT * FROM projects WHERE customer_id = :customerId AND deleted_at IS NULL ORDER BY name ASC")
    suspend fun getByCustomer(customerId: String): List<Project>

    @Query("SELECT * FROM projects WHERE name LIKE :query AND deleted_at IS NULL ORDER BY name ASC")
    suspend fun searchByName(query: String): List<Project>

    @Query("SELECT * FROM projects WHERE hourly_rate BETWEEN :minRate AND :maxRate AND deleted_at IS NULL ORDER BY hourly_rate ASC")
    suspend fun filterByRateRange(minRate: Float, maxRate: Float): List<Project>

    @Query("SELECT * FROM projects WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Long): List<Project>

    @Query("SELECT * FROM projects WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Long): List<Project>

    @Query("SELECT * FROM projects WHERE deleted_at IS NULL ORDER BY name ASC")
    fun observeAllActive(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE user_id = :userId AND deleted_at IS NULL ORDER BY name ASC")
    fun observeByUser(userId: String): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE customer_id = :customerId AND deleted_at IS NULL ORDER BY name ASC")
    fun observeByCustomer(customerId: String): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<Project>>

    @Insert
    suspend fun insertAll(projects: List<Project>)

    @Query("UPDATE projects SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM projects WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("UPDATE projects SET user_id = NULL WHERE user_id = :userId")
    suspend fun detachFromUser(userId: String)

    @Query("UPDATE projects SET customer_id = NULL WHERE customer_id = :customerId")
    suspend fun detachFromCustomer(customerId: String)

    @Query("SELECT COUNT(*) FROM projects WHERE user_id = :userId AND deleted_at IS NULL")
    suspend fun countByUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM projects WHERE customer_id = :customerId AND deleted_at IS NULL")
    suspend fun countByCustomer(customerId: String): Int

    @Query("SELECT COUNT(*) FROM projects WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("SELECT AVG(hourly_rate) FROM projects WHERE deleted_at IS NULL AND hourly_rate IS NOT NULL")
    suspend fun getAverageHourlyRate(): Float?

    @Query("SELECT SUM(daily_work_hours) FROM projects WHERE deleted_at IS NULL AND daily_work_hours IS NOT NULL")
    suspend fun getTotalDailyWorkHours(): Int?
}