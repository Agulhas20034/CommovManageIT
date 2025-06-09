package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.commovmanageit.db.entities.Permission
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
interface PermissionDao {
    @Insert
    suspend fun insert(permission: Permission)

    @Insert
    suspend fun insertAll(permissions: List<Permission>)

    @Update
    suspend fun update(permission: Permission)

    @Delete
    suspend fun delete(permission: Permission)

    @Query("SELECT * FROM permissions WHERE id = :id")
    suspend fun getById(id: String): Permission?

    @Query("SELECT * FROM permissions WHERE deleted_at IS NULL ORDER BY label ASC")
    suspend fun getAllActive(): List<Permission>

    @Query("SELECT * FROM permissions WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<Permission>

    @Query("SELECT * FROM permissions WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<Permission>

    @Query("UPDATE permissions SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE permissions SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("UPDATE permissions SET deleted_at = :timestamp,is_synced=0 WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Instant = Clock.System.now())

    @Query("SELECT * FROM permissions WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Instant): List<Permission>

    @Query("SELECT * FROM permissions WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Instant): List<Permission>

    @Query("SELECT * FROM permissions WHERE deleted_at IS NULL ORDER BY label ASC")
    fun observeAllActive(): Flow<List<Permission>>

    @Query("SELECT * FROM permissions WHERE id = :id")
    fun observeById(id: String): Flow<Permission?>

    @Query("SELECT * FROM permissions WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<Permission>>

    @Query("SELECT * FROM permissions WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<Permission>>

    @Query("UPDATE permissions SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM permissions WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM permissions WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM permissions WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM permissions")
    suspend fun deleteAll()
}