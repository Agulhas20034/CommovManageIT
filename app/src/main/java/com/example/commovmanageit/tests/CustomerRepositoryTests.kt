package com.example.commovmanageit.db.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.utils.CustomerTestUtils
import com.example.commovmanageit.utils.ConnectivityMonitor
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
@RequiresApi(Build.VERSION_CODES.O)
class CustomerRepositoryTest(
    private val repository: CustomerRepository,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val testScope = CoroutineScope(Dispatchers.IO)

    fun runAllTests() {
        testScope.launch {
            try {
                Log.d("RepositoryTest", "=== Starting Tests ===")
                repository.clearLocalDatabase()
                /*testLocalOperations()
                testRemoteOperations()
                testSyncOperations()
                testObservationFlows()
                testSearchOperations()
                testCountOperations()*/
                testInsertUpdateDeleteFunctions()
                repository.clearLocalDatabase()
                Log.d("RepositoryTest", "=== All Tests Completed ===")
            } catch (e: Exception) {
                Log.e("RepositoryTest", "Test failed", e)
            }
        }
    }

    private suspend fun testLocalOperations() {
        Log.d("RepositoryTest", "-- Testing Local Operations --")

        // Test insert
        var testCustomer = CustomerTestUtils.generateTestCustomer()
        testCustomer = testCustomer.copy(
            email = "testlocal-${
                UUID.randomUUID().toString().substring(0, 8)
            }@example.com",
            name = "Test Local Customer ${UUID.randomUUID().toString().substring(0, 8)}"
        )
        val inserted = repository.insertLocal(testCustomer)
        logTestResult("Insert", inserted.id == testCustomer.id)

        // Test get by id
        val retrieved = repository.getByIdLocal(inserted.id)
        logTestResult("Get by ID", retrieved?.id == inserted.id)

        // Test update
        val updatedCustomer = retrieved?.copy(name = "Updated${retrieved.id}")
        if (updatedCustomer != null) {
            repository.updateLocal(updatedCustomer)
            val afterUpdate = repository.getByIdLocal(updatedCustomer.id)
            logTestResult("Update", afterUpdate?.name == "Updated${retrieved?.id}")
        }

        // Test delete
        repository.deleteLocal(inserted.id, "Test")
        val afterDelete = repository.getByIdLocal(inserted.id)
        logTestResult("Delete", afterDelete?.deletedAt != null)
        val updated: Customer = updatedCustomer?.copy(
            updatedAt = inserted.updatedAt,
            isSynced = true
        ) ?: inserted.copy(
            updatedAt = inserted.updatedAt,
            isSynced = true
        )
        repository.updateRemote(updated)
        // Clean up
        repository.deleteLocal(inserted.id, "Real")
        repository.deleteRemote(inserted.id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun testRemoteOperations() {
        if (!connectivityMonitor.isConnected) {
            Log.d("RepositoryTest", "-- Skipping Remote Operations (no internet) --")
            return
        }

        Log.d("RepositoryTest", "-- Testing Repository Remote Operations --")

        val testCustomer = CustomerTestUtils.generateTestCustomer(
            "repo-test-${
                UUID.randomUUID().toString().substring(0, 8)
            }"
        )
        CustomerTestUtils.printCustomer(testCustomer, "Generated Test Customer")

        try {
            Log.d("RepositoryTest", "Attempting repository.insertRemote()")
            val serverId = repository.insertRemote(testCustomer)
            Log.d("RepositoryTest", "✅ Success - insertRemote returned serverId: $serverId")

            Log.d("RepositoryTest", "Attempting to fetch inserted customer")
            val fetchedRemoteCustomer = repository.getByIdRemote(serverId)
            val fetchedCustomer = fetchedRemoteCustomer?.toLocal()

            if (fetchedCustomer != null) {
                Log.d("RepositoryTest", "✅ Successfully fetched inserted customer")
                CustomerTestUtils.printCustomer(fetchedCustomer, "Fetched Customer")

                // Verify data matches
                if (fetchedCustomer.name == testCustomer.name &&
                    fetchedCustomer.email == testCustomer.email
                ) {
                    Log.d("RepositoryTest", "✅ Data verification passed")
                } else {
                    Log.e("RepositoryTest", "❌ Data mismatch between inserted and fetched")
                }
            } else {
                Log.e("RepositoryTest", "❌ Failed to fetch inserted customer")
            }

            // Test repository's updateRemote
            fetchedCustomer?.let { customer ->
                val updatedCustomer = customer.copy(
                    name = "Updated ${customer.name}",
                    phone_Number = "999-${(1000..9999).random()}"
                )
                Log.d("RepositoryTest", "Attempting repository.updateRemote()")
                val remotecustomer = repository.updateRemote(updatedCustomer)
                Log.d(
                    "RepositoryTest",
                    "✅ Success - updateRemote completed, user updated_at: ${remotecustomer.updated_at}"
                )
            }

            // Test repository's deleteRemote
            serverId.let { id ->
                Log.d("RepositoryTest", "Attempting repository.deleteRemote()")
                repository.deleteRemote(id)
                Log.d("RepositoryTest", "✅ Success - deleteRemote completed")

                // Verify deletion
                val deletedCustomer = repository.getByIdRemote(id)
                if (deletedCustomer == null) {
                    Log.d("RepositoryTest", "✅ Verification - customer successfully deleted")
                } else {
                    Log.e("RepositoryTest", "❌ Verification - customer still exists after deletion")
                }
            }

        } catch (e: Exception) {
            Log.e("RepositoryTest", "❌ Repository remote operation failed", e)

            when {
                e is IOException ->
                    Log.e("RepositoryTest", "Network connection failed")

                e is ResponseException ->
                    Log.e(
                        "RepositoryTest",
                        "Supabase request failed: ${e.response.status.value} ${e.response.status.description}"
                    )

                e.message?.contains("violates unique constraint") == true ->
                    Log.e("RepositoryTest", "Duplicate data error")

                else ->
                    Log.e("RepositoryTest", "Unexpected error: ${e.javaClass.simpleName}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun testSyncOperations() {
        if (!connectivityMonitor.isConnected) {
            Log.d("RepositoryTest", "-- Skipping Sync Operations (no internet) --")
            return
        }

        Log.d("RepositoryTest", "-- Testing Sync Operations --")

        // Create unsynced local records
        val unsyncedCustomer1 =
            CustomerTestUtils.generateTestCustomer("customer1").copy(isSynced = false)
        val unsyncedCustomer2 =
            CustomerTestUtils.generateTestCustomer("customer2").copy(isSynced = false)
        repository.insertLocal(unsyncedCustomer1)
        repository.insertLocal(unsyncedCustomer2)

        // Trigger sync
        repository.syncChanges()

        // Verify sync - now we should have serverId populated
        val afterSync1 = repository.getByIdLocal(unsyncedCustomer1.id)
        val afterSync2 = repository.getByIdLocal(unsyncedCustomer2.id)
        logTestResult(
            "Sync Created Items",
            afterSync1?.serverId != null && afterSync2?.serverId != null
        )

        // Clean up
        afterSync1?.let { repository.deleteRemote(it.serverId!!) }
        afterSync2?.let { repository.deleteRemote(it.serverId!!) }
        repository.deleteLocal(unsyncedCustomer1.id, "Real")
        repository.deleteLocal(unsyncedCustomer2.id, "Real")
    }

    private suspend fun testObservationFlows() {
        Log.d("RepositoryTest", "-- Testing Observation Flows --")
        val testCustomer2 = CustomerTestUtils.generateTestCustomer(
            "ObservationTestAll-${
                UUID.randomUUID().toString().substring(0, 8)
            }"
        )
        repository.insertLocal(testCustomer2)
        // Test observeAllActive
        val activeFlow = repository.observeAllActive()
        val activeCustomers = activeFlow.first()
        logTestResult("ObserveAllActive", activeCustomers.isNotEmpty())

        // Test observeById
        val testCustomer = CustomerTestUtils.generateTestCustomer(
            "ObservationTest-${
                UUID.randomUUID().toString().substring(0, 8)
            }"
        )
        repository.insertLocal(testCustomer)
        val byIdFlow = repository.observeById(testCustomer.id)
        val observedCustomer = byIdFlow.first()
        logTestResult("ObserveById", observedCustomer?.id == testCustomer.id)

        // Clean up
        repository.deleteLocal(testCustomer.id, "Real")
        repository.deleteLocal(testCustomer2.id, "Real")
        repository.deleteRemote(testCustomer.id)
        repository.deleteRemote(testCustomer2.id)

    }

    private suspend fun testSearchOperations() {
        Log.d("RepositoryTest", "-- Testing Search Operations --")

        // Test search by name
        val searchCustomer = CustomerTestUtils.generateTestCustomer(
            "TestSearch.${
                UUID.randomUUID().toString().substring(0, 8)
            }"
        )
        repository.insertLocal(searchCustomer)

        val searchResults = repository.searchByName("TestSearch")
        logTestResult("Search by Name", searchResults.any { it.name.contains("TestSearch") })

        // Clean up
        repository.deleteLocal(searchCustomer.id, "Real")
        repository.deleteRemote(searchCustomer.id)
    }

    private suspend fun testCountOperations() {
        Log.d("RepositoryTest", "-- Testing Count Operations --")

        val initialActiveCount = repository.getActiveCount()

        // Add test data
        val testCustomer = CustomerTestUtils.generateTestCustomer(
            "TestCount.${
                UUID.randomUUID().toString().substring(0, 8)
            }"
        )
        repository.insertLocal(testCustomer)

        val afterInsertActiveCount = repository.getActiveCount()
        logTestResult("Active Count", afterInsertActiveCount > initialActiveCount)

        // Clean up
        repository.deleteLocal(testCustomer.id, "Real")
        repository.deleteRemote(testCustomer.id)
    }

    private fun logTestResult(testName: String, passed: Boolean) {
        Log.d("RepositoryTest", "$testName: ${if (passed) "PASSED" else "FAILED"}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun testInsertUpdateDeleteFunctions() {
        Log.d("RepositoryTest", "-- Teste: Insert, Update e Delete (Local e Remoto) --")

        // Teste remoto (com internet)
        if (connectivityMonitor.isConnected) {
            val remoteCustomer = CustomerTestUtils.generateTestCustomer(
                "RemoteTest-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            val insertedRemote = repository.insert(remoteCustomer)
            logTestResult(
                "Insert remoto",
                insertedRemote.isSynced && insertedRemote.serverId != null
            )

            val updatedRemote = insertedRemote.copy(name = "Remoto Atualizado")
            repository.update(updatedRemote)
            val fetchedRemote = repository.getByIdRemote(updatedRemote.serverId!!)
            logTestResult("Update remoto", fetchedRemote?.name == "Remoto Atualizado")

            repository.delete(insertedRemote.id)
            val deletedRemote = repository.getByIdRemote(insertedRemote.serverId!!)
            logTestResult("Delete remoto", deletedRemote == null)
        } else {
            Log.d("RepositoryTest", "Sem internet, pulando teste remoto.")
        }

        // Delay para desconectar a internet manualmente
        Log.d(
            "RepositoryTest",
            "Desconecte a internet agora para testar operações locais. Aguardando 30 segundos..."
        )
        kotlinx.coroutines.delay(30_000)

        // Limpa localmente se já existir
        val localTestId = "local-test-${UUID.randomUUID().toString().substring(0, 8)}"
        repository.deleteLocal(localTestId, "Real")

        // Teste local (sem internet)
        val localCustomer = CustomerTestUtils.generateTestCustomer(localTestId)
        val insertedLocal = repository.insert(localCustomer)
        logTestResult("Insert local", !insertedLocal.isSynced)

        val updatedLocal = insertedLocal.copy(name = "Local Atualizado")
        repository.update(updatedLocal)
        val fetchedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Update local", fetchedLocal?.name == "Local Atualizado")

        repository.delete(insertedLocal.id)
        val deletedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Delete local", deletedLocal?.deletedAt != null)

        val localCustomer2 =
            CustomerTestUtils.generateTestCustomer(UUID.randomUUID().toString().substring(0, 8))
        val insertedLocal2 = repository.insert(localCustomer2)
        logTestResult("Insert local", !insertedLocal.isSynced)
        // Reconecte a internet e sincronize
        Log.d(
            "RepositoryTest",
            "Reconecte a internet para testar sincronização. Aguardando 30 segundos..."
        )
        kotlinx.coroutines.delay(30_000)

        repository.syncChanges()
        val syncedCustomer = repository.getByIdLocal(insertedLocal2.id)
        repository.delete(insertedLocal2.id)
        logTestResult(
            "Sync após reconexão",
            syncedCustomer?.serverId != null && syncedCustomer.isSynced
        )


    }


}