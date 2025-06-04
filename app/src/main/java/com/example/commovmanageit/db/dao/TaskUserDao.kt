package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.commovmanageit.db.entities.TaskUser
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TaskUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskUser: TaskUser): Long

    @Update
    suspend fun update(taskUser: TaskUser): Int

    @Query("DELETE FROM task_users WHERE id = :id")
    suspend fun delete(id: String): Int

    @Query("SELECT * FROM task_users WHERE id = :id")
    suspend fun getById(id: String): TaskUser?


    @Query("SELECT * FROM task_users WHERE task_id = :taskId AND deleted_at IS NULL")
    fun getUsersForTask(taskId: String): Flow<List<TaskUser>>

    @Query("SELECT * FROM task_users WHERE user_id = :userId AND deleted_at IS NULL")
    fun getTasksForUser(userId: String): Flow<List<TaskUser>>

    @Query("SELECT * FROM task_users WHERE task_id = :taskId AND user_id = :userId AND deleted_at IS NULL LIMIT 1")
    suspend fun getTaskUserRelation(taskId: String, userId: String): TaskUser?


    @Query("SELECT * FROM task_users WHERE start_date BETWEEN :start AND :end AND deleted_at IS NULL")
    fun getActiveAssignmentsBetween(start: Long, end: Long): Flow<List<TaskUser>>

    @Query("SELECT * FROM task_users WHERE end_date BETWEEN :start AND :end AND deleted_at IS NULL")
    fun getEndingAssignmentsBetween(start: Long, end: Long): Flow<List<TaskUser>>


    @Query("UPDATE task_users SET deleted_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM task_users WHERE deleted_at IS NULL")
    fun getAllActive(): Flow<List<TaskUser>>

    @Query("UPDATE task_users SET deleted_at = NULL, updated_at = :timestamp WHERE id = :id")
    suspend fun restore(id: String, timestamp: Long = System.currentTimeMillis()): Int


    @Query("SELECT * FROM task_users WHERE is_synced = 0")
    suspend fun getUnsyncedRelations(): List<TaskUser>

    @Query("UPDATE task_users SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String): Int

    @Query("SELECT * FROM task_users WHERE server_id = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: String): TaskUser?


    @Query("SELECT AVG(conclusion_rate) FROM task_users WHERE user_id = :userId AND deleted_at IS NULL")
    suspend fun getAverageConclusionRateForUser(userId: String): Float?

    @Query("SELECT SUM(time_used) FROM task_users WHERE task_id = :taskId AND deleted_at IS NULL")
    suspend fun getTotalTimeUsedForTask(taskId: String): Float?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(taskUsers: List<TaskUser>): List<Long>

    @Query("DELETE FROM task_users WHERE id IN (:ids)")
    suspend fun deleteAllById(ids: List<String>): Int


    @Transaction
    suspend fun assignUserToTask(taskId: String, userId: String, startDate: Long? = null, endDate: Long? = null): TaskUser {
        val existing = getTaskUserRelation(taskId, userId)
        if (existing != null) {
            existing.apply {
                this.startDate = startDate ?: this.startDate
                this.endDate = endDate ?: this.endDate
                updatedAt = System.currentTimeMillis()
                deletedAt = null
            }
            update(existing)
            return existing
        }

        val newAssignment = TaskUser(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            userId = userId,
            startDate = startDate,
            endDate = endDate,
            location = null,
            conclusionRate = null,
            timeUsed = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
        insert(newAssignment)
        return getById(newAssignment.id) ?: throw Exception("Failed to create task-user assignment")
    }

    @Query("SELECT EXISTS(SELECT 1 FROM task_users WHERE id = :id LIMIT 1)")
    suspend fun exists(id: String): Boolean
}