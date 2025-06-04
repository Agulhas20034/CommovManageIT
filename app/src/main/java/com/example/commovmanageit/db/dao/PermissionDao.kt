package com.example.commovmanageit.db.dao

import androidx.room.*
import com.example.commovmanageit.db.entities.Permission
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionDao {
    @Insert
    suspend fun insert(permission: Permission)

    @Update
    suspend fun update(permission: Permission)

    @Delete
    suspend fun delete(permission: Permission)

    @Query("SELECT * FROM permissions WHERE id = :id")
    suspend fun getById(id: String): Permission?

    @Insert
    suspend fun insertAll(permissions: List<Permission>)

    @Query("DELETE FROM permissions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("UPDATE permissions SET deleted_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM permissions WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<Permission>

    @Query("SELECT * FROM permissions WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<Permission>

    @Query("UPDATE permissions SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE permissions SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("SELECT * FROM permissions WHERE deleted_at IS NULL ORDER BY label ASC")
    suspend fun getAllActive(): List<Permission>

    @Query("SELECT * FROM permissions WHERE label LIKE :query AND deleted_at IS NULL ORDER BY label ASC")
    suspend fun searchByLabel(query: String): List<Permission>

    @Query("SELECT * FROM permissions WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Long): List<Permission>

    @Query("SELECT * FROM permissions WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Long): List<Permission>

    @Query("SELECT * FROM permissions WHERE deleted_at IS NULL ORDER BY label ASC")
    fun observeAllActive(): Flow<List<Permission>>

    @Query("SELECT * FROM permissions WHERE id = :id")
    fun observeById(id: String): Flow<Permission?>

    @Query("SELECT * FROM permissions WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<Permission>>

    @Query("SELECT * FROM permissions WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<Permission>>

    @Query("DELETE FROM permissions WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM permissions WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM permissions WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("UPDATE permissions SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("UPDATE permissions SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markMultipleAsSynced(ids: List<String>)
}