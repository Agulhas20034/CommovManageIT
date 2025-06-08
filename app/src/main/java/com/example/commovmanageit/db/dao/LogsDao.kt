package com.example.commovmanageit.db.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.db.entities.Logs
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface LogsDao {
    @Insert
    suspend fun insert(log: Logs)

    @Insert
    suspend fun insertAll(logs: List<Logs>)

    @Update
    suspend fun update(log: Logs)

    @Delete
    suspend fun delete(log: Logs)

    @Query("SELECT * FROM logs WHERE id = :id")
    suspend fun getById(id: String): Logs?

    @Query("SELECT * FROM logs ORDER BY created_at DESC")
    suspend fun getAll(): List<Logs>

    @Query("SELECT * FROM logs WHERE is_synced = 0")
    suspend fun getUnsynced(): List<Logs>

    @Query("UPDATE logs SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("SELECT * FROM logs WHERE entity_id = :entityId AND entity_type = :entityType ORDER BY created_at DESC")
    suspend fun getByEntity(entityId: String, entityType: String): List<Logs>

    @Query("SELECT * FROM logs WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getByUser(userId: String): List<Logs>

    @Query("SELECT * FROM logs WHERE created_at >= :since ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Instant): List<Logs>

    @Query("SELECT * FROM logs ORDER BY created_at DESC")
    fun observeAll(): Flow<List<Logs>>

    @Query("SELECT * FROM logs WHERE is_synced = 0")
    fun observeUnsynced(): Flow<List<Logs>>

    @Query("DELETE FROM logs")
    suspend fun deleteAll()

    @Query("UPDATE logs SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("SELECT * FROM logs WHERE is_synced = 0")
    suspend fun getUnsyncedCreated(): List<Logs>
}