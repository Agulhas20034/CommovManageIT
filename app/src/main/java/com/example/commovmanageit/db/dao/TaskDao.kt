package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.commovmanageit.db.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: String): Int

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): Task?

    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getAll(): Flow<List<Task>>

    @Query("UPDATE tasks SET deleted_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM tasks WHERE deleted_at IS NULL")
    fun getAllActive(): Flow<List<Task>>

    @Query("UPDATE tasks SET deleted_at = NULL, updated_at = :timestamp WHERE id = :id")
    suspend fun restore(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM tasks WHERE is_synced = 0")
    suspend fun getUnsyncedTasks(): List<Task>

    @Query("UPDATE tasks SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String): Int

    @Query("SELECT * FROM tasks WHERE server_id = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: String): Task?

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND deleted_at IS NULL")
    fun getTasksByUser(userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE project_id = :projectId AND deleted_at IS NULL")
    fun getTasksByProject(projectId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE project_id = :projectId AND user_id = :userId AND deleted_at IS NULL")
    fun getTasksByProjectAndUser(projectId: String, userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status AND deleted_at IS NULL")
    fun getTasksByStatus(status: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE start_date BETWEEN :start AND :end AND deleted_at IS NULL")
    fun getTasksStartingBetween(start: Long, end: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE end_date BETWEEN :start AND :end AND deleted_at IS NULL")
    fun getTasksEndingBetween(start: Long, end: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE created_at BETWEEN :start AND :end")
    fun getTasksCreatedBetween(start: Long, end: Long): Flow<List<Task>>

    // ========== Bulk Operations ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>): List<Long>

    @Query("DELETE FROM tasks WHERE id IN (:ids)")
    suspend fun deleteAllById(ids: List<String>): Int

    @Transaction
    suspend fun upsert(task: Task) {
        if (exists(task.id)) {
            task.updatedAt = System.currentTimeMillis()
            update(task)
        } else {
            insert(task)
        }
    }

    @Query("SELECT EXISTS(SELECT 1 FROM tasks WHERE id = :id LIMIT 1)")
    suspend fun exists(id: String): Boolean


    @Query("SELECT SUM(hourly_rate) FROM tasks WHERE project_id = :projectId AND deleted_at IS NULL")
    suspend fun getTotalHourlyRateForProject(projectId: String): Float?

    @Query("SELECT AVG(hourly_rate) FROM tasks WHERE user_id = :userId AND deleted_at IS NULL")
    suspend fun getAverageHourlyRateForUser(userId: String): Float?
}