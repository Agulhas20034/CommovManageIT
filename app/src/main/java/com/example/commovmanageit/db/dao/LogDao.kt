package com.example.commovmanageit.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
/*
@Dao
interface LogsDao {
    @Insert
    suspend fun insert(Logs: Logs)

    @Query("SELECT * FROM Logs WHERE id = :id")
    suspend fun getById(id: String): Logs?

    @Query("DELETE FROM Logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert
    suspend fun insertAll(Logs: List<Logs>)

    @Query("DELETE FROM Logs WHERE is_synced = 1")
    suspend fun deleteSynced()

    @Query("SELECT * FROM Logs WHERE is_synced = 0")
    suspend fun getUnsynced(): List<Logs>

    @Query("UPDATE Logs SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE Logs SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("UPDATE Logs SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("SELECT * FROM Logs WHERE entity_id = :entityId AND entity_type = :entityType ORDER BY created_at DESC")
    suspend fun getByEntity(entityId: String, entityType: String): List<Logs>

    @Query("SELECT * FROM Logs WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getByUserId(userId: String): List<Logs>

    @Query("SELECT * FROM Logs WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<Logs>

    @Query("SELECT * FROM Logs ORDER BY created_at DESC")
    suspend fun getAll(): List<Logs>

    @Query("SELECT * FROM Logs ORDER BY created_at DESC")
    fun observeAll(): Flow<List<Logs>>

    @Query("SELECT * FROM Logs WHERE is_synced = 0")
    fun observeUnsynced(): Flow<List<Logs>>

    @Query("SELECT * FROM Logs WHERE entity_id = :entityId AND entity_type = :entityType ORDER BY created_at DESC")
    fun observeByEntity(entityId: String, entityType: String): Flow<List<Logs>>

    @Query("SELECT * FROM Logs WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeByUserId(userId: String): Flow<List<Logs>>

    @Query("SELECT COUNT(*) FROM Logs WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("SELECT COUNT(*) FROM Logs WHERE entity_type = :entityType")
    suspend fun countByEntityType(entityType: String): Int

    @Query("SELECT DISTINCT entity_type FROM Logs")
    suspend fun getLogsgedEntityTypes(): List<String>

    @Query("""
        SELECT * FROM Logs 
        WHERE entity_id = :entityId 
        AND entity_type = :entityType 
        AND created_at <= :timestamp
        ORDER BY created_at DESC
        LIMIT 1
    """)
    suspend fun getLastStateBefore(entityId: String, entityType: String, timestamp: Long): Logs?
}*/