package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.commovmanageit.db.entities.Role
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(role: Role): Long

    @Update
    suspend fun update(role: Role): Int

    @Query("DELETE FROM roles WHERE id = :id")
    suspend fun delete(id: String): Int

    @Query("SELECT * FROM roles WHERE id = :id")
    suspend fun getById(id: String): Role?

    @Query("SELECT * FROM roles ORDER BY name ASC")
    fun getAll(): Flow<List<Role>>

    @Query("UPDATE roles SET deleted_at = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM roles WHERE deleted_at IS NULL")
    fun getAllActive(): Flow<List<Role>>

    @Query("UPDATE roles SET deleted_at = NULL, updated_at = :timestamp WHERE id = :id")
    suspend fun restore(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM roles WHERE is_synced = 0")
    suspend fun getUnsyncedRoles(): List<Role>

    @Query("UPDATE roles SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String): Int

    @Query("SELECT * FROM roles WHERE server_id = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: String): Role?

    @Query("SELECT * FROM roles WHERE permission_id = :permissionId AND deleted_at IS NULL")
    fun getRolesByPermission(permissionId: String): Flow<List<Role>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(roles: List<Role>): List<Long>

    @Query("DELETE FROM roles WHERE id IN (:ids)")
    suspend fun deleteAllById(ids: List<String>): Int

    @Query("SELECT * FROM roles WHERE created_at BETWEEN :start AND :end")
    fun getRolesCreatedBetween(start: Long, end: Long): Flow<List<Role>>

    @Query("SELECT * FROM roles WHERE updated_at BETWEEN :start AND :end")
    fun getRolesUpdatedBetween(start: Long, end: Long): Flow<List<Role>>

    @Query("UPDATE roles SET is_synced = :isSynced WHERE id = :id")
    suspend fun setSyncStatus(id: String, isSynced: Boolean): Int

    @Query("UPDATE roles SET is_synced = 0 WHERE server_id = :serverId")
    suspend fun markServerRecordAsUnsynced(serverId: String): Int

    @Transaction
    suspend fun upsert(role: Role) {
        if (exists(role.id)) {
            role.updatedAt = System.currentTimeMillis()
            update(role)
        } else {
            insert(role)
        }
    }

    @Query("SELECT EXISTS(SELECT 1 FROM roles WHERE id = :id LIMIT 1)")
    suspend fun exists(id: String): Boolean
}