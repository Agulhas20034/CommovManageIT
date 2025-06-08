package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.commovmanageit.db.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Insert
    suspend fun insertAll(users: List<User>)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): User?

    @Query("SELECT * FROM users WHERE deleted_at IS NULL ORDER BY email ASC")
    suspend fun getAllActive(): List<User>

    @Query("SELECT * FROM users WHERE role_id = :roleId AND deleted_at IS NULL ORDER BY email ASC")
    suspend fun getByRoleId(roleId: String): List<User>

    @Query("SELECT * FROM users WHERE email = :email AND deleted_at IS NULL LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<User>

    @Query("SELECT * FROM users WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<User>

    @Query("UPDATE users SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE users SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("UPDATE users SET deleted_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Instant = Clock.System.now())

    @Query("SELECT * FROM users WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Instant): List<User>

    @Query("SELECT * FROM users WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Instant): List<User>

    @Query("SELECT * FROM users WHERE deleted_at IS NULL ORDER BY email ASC")
    fun observeAllActive(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<User>>

    @Query("UPDATE users SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM users WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM users WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM users WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
