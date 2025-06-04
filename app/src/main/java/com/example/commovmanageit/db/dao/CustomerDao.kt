package com.example.commovmanageit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.commovmanageit.db.entities.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert
    suspend fun insert(customer: Customer)

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: String): Customer?

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAll(): List<Customer>


    @Query("UPDATE customers SET deleted_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM customers WHERE is_synced = 0 AND deleted_at IS NULL")
    suspend fun getUnsyncedCreatedOrUpdated(): List<Customer>

    @Query("SELECT * FROM customers WHERE deleted_at IS NOT NULL AND is_synced = 0")
    suspend fun getUnsyncedDeleted(): List<Customer>

    @Query("UPDATE customers SET is_synced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE customers SET is_synced = :isSynced, server_id = :serverId WHERE id = :id")
    suspend fun updateSyncInfo(id: String, isSynced: Boolean, serverId: String?)

    @Query("SELECT * FROM customers WHERE deleted_at IS NULL ORDER BY name ASC")
    suspend fun getAllActive(): List<Customer>

    @Query("SELECT * FROM customers WHERE name LIKE :query AND deleted_at IS NULL ORDER BY name ASC")
    suspend fun searchByName(query: String): List<Customer>

    @Query("SELECT * FROM customers WHERE email = :email AND deleted_at IS NULL LIMIT 1")
    suspend fun getByEmail(email: String): Customer?

    @Query("SELECT * FROM customers WHERE phone_number = :phoneNumber AND deleted_at IS NULL LIMIT 1")
    suspend fun getByPhoneNumber(phoneNumber: String): Customer?

    @Query("SELECT * FROM customers WHERE created_at >= :since AND deleted_at IS NULL ORDER BY created_at DESC")
    suspend fun getCreatedSince(since: Long): List<Customer>

    @Query("SELECT * FROM customers WHERE updated_at >= :since AND deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getUpdatedSince(since: Long): List<Customer>

    @Query("SELECT * FROM customers WHERE deleted_at IS NULL ORDER BY name ASC")
    fun observeAllActive(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeById(id: String): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE is_synced = 0 AND deleted_at IS NULL")
    fun observeUnsyncedChanges(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE deleted_at IS NOT NULL AND is_synced = 0")
    fun observeUnsyncedDeletes(): Flow<List<Customer>>

    @Insert
    suspend fun insertAll(customers: List<Customer>)

    @Query("UPDATE customers SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)

    @Query("DELETE FROM customers WHERE deleted_at IS NOT NULL AND is_synced = 1")
    suspend fun purgeDeleted()

    @Query("SELECT COUNT(*) FROM customers WHERE deleted_at IS NULL")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM customers WHERE is_synced = 0")
    suspend fun getUnsyncedCount(): Int

}