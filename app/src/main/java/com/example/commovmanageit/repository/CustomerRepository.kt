package com.example.commovmanageit.db.repositories
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.remote.dto.CustomerRemote
import com.example.commovmanageit.remote.dto.toRemote
import com.example.commovmanageit.remote.SupabaseManager
import com.example.commovmanageit.db.dao.CustomerDao
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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
        customer.updatedAt = Clock.System.now()
        customerDao.update(customer)
        syncIfConnected()
    }

    suspend fun deleteLocal(id: String,type: String) {
        customerDao.softDelete(id)
        if(type.equals("Test"))
            return
        else
            syncIfConnected()
    }

    suspend fun getByIdLocal(id: String): Customer? = customerDao.getById(id)
    suspend fun getAllLocal(): List<Customer> = customerDao.getAllActive()

    @RequiresApi(Build.VERSION_CODES.O)
    public suspend fun insertRemote(customer: Customer): String {
        val remoteCustomer = customer.toRemote()
        val result = SupabaseManager.insertCustomer<CustomerRemote>(remoteCustomer)
        return result.id
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRemote(customer: Customer): CustomerRemote {
        val remoteCustomer = customer.toRemote()
        Log.d("CustomerRepository", "Updating remote customer: ${customer.id}")
        return SupabaseManager.updateCustomer<CustomerRemote>(customer.serverId ?: customer.id, remoteCustomer)
    }

    suspend fun deleteRemote(id: String) {
        SupabaseManager.delete("customers", id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(customer: Customer): Customer {
        val newCustomer = customer.copy(
            id = customer.id.ifEmpty { UUID.randomUUID().toString() },
            updatedAt = Clock.System.now()
        )

        return if (connectivityMonitor.isConnected) {
            try {
                val serverId = newCustomer.id
                val syncedCustomer = newCustomer.copy(isSynced = true, serverId = serverId)
                insertRemote(syncedCustomer)
                insertLocal(syncedCustomer)
                syncedCustomer
            } catch (e: Exception) {
                customerDao.insert(newCustomer)
                newCustomer
            }
        } else {
            customerDao.insert(newCustomer)
            newCustomer
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(customer: Customer) {
        val updatedCustomer = customer.copy(updatedAt = Clock.System.now())

        if (connectivityMonitor.isConnected) {
            try {
                if (updatedCustomer.serverId != null) {
                    updateRemote(updatedCustomer)
                    Log.d("CustomerRepository", "Updated remote customer: ${updatedCustomer.id}")
                    customerDao.update(updatedCustomer.copy(isSynced = true))
                } else {
                    val serverId = insertRemote(updatedCustomer)
                    customerDao.update(updatedCustomer.copy(isSynced = true, serverId = serverId))
                }
            } catch (e: Exception) {
                customerDao.update(updatedCustomer)
            }
        } else {
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
        if (connectivityMonitor.isConnected) {
            try {
                syncChanges()
            } catch (e: Exception) {
                Log.d("CustomerRepository", "Sync failed: ${e.message}", e)
            }
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
                Log.d("CustomerRepository", "Erro ao sincronizar cliente ${customer.id}", e)
            }
        }

        customerDao.getUnsyncedDeleted().forEach { customer ->
            try {
                Log.d("CustomerRepository", "Deleting remote customer: ${customer.id}")
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
                    phone_Number = remote.phone_number,
                    createdAt = Instant.parse(remote.created_at),
                    updatedAt = Instant.parse(remote.updated_at),
                    deletedAt = remote.deleted_at?.let { Instant.parse(remote.deleted_at) },
                    isSynced = true
                )
            }
        } catch (e: Exception) {
            Log.e("CustomerRepository", "Failed to fetch remote customers", e)
            emptyList()
        }
    }


    suspend fun getByIdRemote(id: String): CustomerRemote? {
        return try {
            val remoteCustomer = SupabaseManager.fetchById<CustomerRemote>("customers", id)

            remoteCustomer?.let { customer ->
                customerDao.update(customer.toLocal())
            }

            return remoteCustomer
        } catch (e: Exception) {
            Log.e("CustomerRepository", "Error fetching remote customer(normal if in test)", e)
            null
        }
    }
    suspend fun clearLocalDatabase() {
        customerDao.deleteAll()
    }
}