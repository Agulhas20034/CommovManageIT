package com.example.commovmanageit.db.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.remote.dto.CustomerRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.db.dao.CustomerDao
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimePeriod
import java.util.Date
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class CustomerRepository(
    private val customerDao: CustomerDao,
    private val connectivityMonitor: ConnectivityMonitor,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    init {
        connectivityMonitor.registerListener { online ->
            if (online) {
                coroutineScope.launch {
                    syncChanges()
                }
            }
        }
    }

    suspend fun insertLocal(customer: Customer): Customer {
        customerDao.insert(customer)
        syncIfConnected()
        return customer
    }

    suspend fun updateLocal(customer: Customer) {
        customer.updatedAt = Date()
        customerDao.update(customer)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String) {
        customerDao.softDelete(id)
        syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): Customer? = customerDao.getById(id)
    suspend fun getAllLocal(): List<Customer> = customerDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(customer: Customer): String {
        val remoteCustomer = customer.toRemote()
        val result = SupabaseManager.insert<CustomerRemote>("customers", remoteCustomer)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateRemote(customer: Customer) {
        val remoteCustomer = customer.toRemote()
        SupabaseManager.update<CustomerRemote>("customers", customer.serverId ?: customer.id, remoteCustomer)
    }

    private suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("customers", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(customer: Customer): Customer {
        val newCustomer = customer.copy(
            id = customer.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Date()
        )

        return try {
            val serverId = insertRemote(newCustomer)
            val syncedCustomer = newCustomer.copy(isSynced = true, serverId = serverId)
            customerDao.insert(syncedCustomer)
            syncedCustomer
        } catch (e: Exception) {
            customerDao.insert(newCustomer)
            newCustomer
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(customer: Customer) {
        val updatedCustomer = customer.copy(updatedAt = Date())

        try {
            if (updatedCustomer.serverId != null) {
                updateRemote(updatedCustomer)
                customerDao.update(updatedCustomer.copy(isSynced = true))
            } else {
                val serverId = insertRemote(updatedCustomer)
                customerDao.update(updatedCustomer.copy(isSynced = true, serverId = serverId))
            }
        } catch (e: Exception) {
            customerDao.update(updatedCustomer)
        }
    }

    suspend fun delete(id: String) {
        try {
            val customer = customerDao.getById(id)
            customer?.let {
                customerDao.softDelete(id)

                if (it.serverId != null && connectivityMonitor.isConnected) {
                    deleteRemote(it.serverId)
                    customerDao.updateSyncStatus(id, true)
                }
            } ?: throw Exception("Customer not found")
        } catch (e: Exception) {
            println("Delete operation partially failed: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncIfConnected() {
        try {
            syncChanges()
        } catch (e: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncChanges() {
        customerDao.getUnsyncedCreatedOrUpdated().forEach { customer ->
            try {
                if (customer.serverId == null) {
                    val serverId = insertRemote(customer)
                    customerDao.markAsSynced(customer.id, serverId)
                } else {
                    updateRemote(customer)
                    customerDao.updateSyncStatus(customer.id, true)
                }
            } catch (e: Exception) {
            }
        }

        customerDao.getUnsyncedDeleted().forEach { customer ->
            try {
                customer.serverId?.let { deleteRemote(it) }
                customerDao.updateSyncStatus(customer.id, true)
            } catch (e: Exception) {
            }
        }

        customerDao.purgeDeleted()
    }

    fun observeAllActive(): Flow<List<Customer>> = customerDao.observeAllActive()
    fun observeById(id: String): Flow<Customer?> = customerDao.observeById(id)
    fun observeUnsyncedChanges(): Flow<List<Customer>> = customerDao.observeUnsyncedChanges()
    fun observeUnsyncedDeletes(): Flow<List<Customer>> = customerDao.observeUnsyncedDeletes()

    suspend fun searchByName(query: String): List<Customer> = customerDao.searchByName("%$query%")
    suspend fun getByEmail(email: String): Customer? = customerDao.getByEmail(email)
    suspend fun getByPhoneNumber(phoneNumber: String): Customer? = customerDao.getByPhoneNumber(phoneNumber)
    suspend fun getActiveCount(): Int = customerDao.getActiveCount()
    suspend fun getUnsyncedCount(): Int = customerDao.getUnsyncedCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllRemote(): List<Customer> {
        return try {
            if (!connectivityMonitor.isConnected) {
                throw IllegalStateException("No internet connection available")
            }

            val remoteCustomers: List<CustomerRemote> = SupabaseManager.getAll<CustomerRemote>("customers")

            remoteCustomers.map { remote ->
                Customer(
                    id = remote.id,
                    serverId = remote.id,
                    name = remote.name,
                    email = remote.email,
                    phoneNumber = remote.phone_number,
                    createdAt = Date(remote.created_at),
                    updatedAt = Date(remote.updated_at),
                    deletedAt = remote.deleted_at?.let { Date(it) },
                    isSynced = true
                )
            }
        } catch (e: Exception) {
            Log.e("CustomerRepository", "Failed to fetch remote customers", e)
            emptyList()
        }
    }
}