package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.commovmanageit.db.entities.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
interface RoleDao {
    @Insert
    suspend fun insert(role: Role)

    @Insert
    suspend fun insertAll(roles: List<Role>)

    @Update
    suspend fun update(role: Role)

    @Delete
    suspend fun delete(role: Role)

    @Query("SELECT * FROM roles WHERE id = :id")
    suspend fun getById(id: String): Role?

    @Query("SELECT * FROM roles WHERE deleted_at IS NULL ORDER BY name ASC")
    suspend fun getAllActive(): List<Role>

    @Query("SELECT * FROM roles WHERE permission_id = :permissionId AND deleted_at IS NULL ORDER BY name ASC")
    suspend fun getByPermissionId(permissionId: String): List<Role>

    @Query("SELECT * FROM roles WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<Role>

    @Query("SELECT * FROM roles WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<Role>

    @Query("UPDATE roles SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE roles SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("UPDATE roles SET deleted_at = :timestamp,is_synced=0 WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Instant = Clock.System.now())

    @Query("SELECT * FROM roles WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Instant): List<Role>

    @Query("SELECT * FROM roles WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Instant): List<Role>

    @Query("SELECT * FROM roles WHERE deleted_at IS NULL ORDER BY name ASC")
    fun observeAllActive(): Flow<List<Role>>

    @Query("SELECT * FROM roles WHERE id = :id")
    fun observeById(id: String): Flow<Role?>

    @Query("SELECT * FROM roles WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<Role>>

    @Query("SELECT * FROM roles WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<Role>>

    @Query("UPDATE roles SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM roles WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM roles WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM roles WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM roles")
    suspend fun deleteAll()
}