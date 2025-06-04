package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.commovmanageit.db.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User): Int

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: String): Int

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): User?

    @Query("SELECT * FROM users ORDER BY email ASC")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE email = :email AND deleted_at IS NULL LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password AND deleted_at IS NULL LIMIT 1")
    suspend fun authenticate(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE role_id = :roleId AND deleted_at IS NULL")
    fun getUsersByRole(roleId: String): Flow<List<User>>

    @Query("UPDATE users SET role_id = :roleId, updated_at = :timestamp WHERE id = :userId")
    suspend fun updateUserRole(userId: String, roleId: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("UPDATE users SET deleted_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM users WHERE deleted_at IS NULL")
    fun getAllActive(): Flow<List<User>>

    @Query("UPDATE users SET deleted_at = NULL, updated_at = :timestamp WHERE id = :id")
    suspend fun restore(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM users WHERE is_synced = 0")
    suspend fun getUnsyncedUsers(): List<User>

    @Query("UPDATE users SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String): Int

    @Query("SELECT * FROM users WHERE server_id = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: String): User?

    @Query("UPDATE users SET daily_work_hours = :hours, updated_at = :timestamp WHERE id = :userId")
    suspend fun updateDailyWorkHours(userId: String, hours: Int, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT AVG(daily_work_hours) FROM users WHERE deleted_at IS NULL")
    suspend fun getAverageDailyWorkHours(): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>): List<Long>

    @Query("DELETE FROM users WHERE id IN (:ids)")
    suspend fun deleteAllById(ids: List<String>): Int

    @Transaction
    suspend fun upsert(user: User) {
        if (exists(user.id)) {
            user.updatedAt = System.currentTimeMillis()
            update(user)
        } else {
            insert(user)
        }
    }

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :id LIMIT 1)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email LIMIT 1)")
    suspend fun emailExists(email: String): Boolean
}