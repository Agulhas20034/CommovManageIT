package com.example.commovmanageit.tests

import UsersRepository
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.entities.User
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.utils.ConnectivityMonitor
import com.example.commovmanageit.utils.UserTestUtils
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class UsersRepositoryTests (
    private val repository: UsersRepository,
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
            var testUser = UserTestUtils.generateTestUser()
            testUser = testUser.copy(
                email = "testlocal-${
                    UUID.randomUUID().toString().substring(0, 8)
                }@example.com",
            )
            val inserted = repository.insertLocal(testUser)
            logTestResult("Insert", inserted.id == testUser.id)
    
            // Test get by id
            val retrieved = repository.getByIdLocal(inserted.id)
            logTestResult("Get by ID", retrieved?.id == inserted.id)
    
            // Test update
            val updatedUser = retrieved?.copy(email = "Updated${retrieved.id}")
            if (updatedUser != null) {
                repository.updateLocal(updatedUser)
                val afterUpdate = repository.getByIdLocal(updatedUser.id)
                logTestResult("Update", afterUpdate?.email== "Updated${retrieved?.id}")
            }
    
            // Test delete
            repository.deleteLocal(inserted.id, "Test")
            val afterDelete = repository.getByIdLocal(inserted.id)
            logTestResult("Delete", afterDelete?.deletedAt != null)
            val updated: User = updatedUser?.copy(
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
    
            val testUser = UserTestUtils.generateTestUser(
                "repo-test-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            UserTestUtils.printUser(testUser, "Generated Test User")
    
            try {
                Log.d("RepositoryTest", "Attempting repository.insertRemote()")
                val serverId = repository.insertRemote(testUser)
                Log.d("RepositoryTest", "✅ Success - insertRemote returned serverId: $serverId")
    
                Log.d("RepositoryTest", "Attempting to fetch inserted User")
                val fetchedRemoteUser = repository.getByIdRemote(serverId)
                val fetchedUser = fetchedRemoteUser?.toLocal()
    
                if (fetchedUser != null) {
                    Log.d("RepositoryTest", "✅ Successfully fetched inserted User")
                    UserTestUtils.printUser(fetchedUser, "Fetched User")
    
                    // Verify data matches
                    if (fetchedUser.email == testUser.email
                    ) {
                        Log.d("RepositoryTest", "✅ Data verification passed")
                    } else {
                        Log.e("RepositoryTest", "❌ Data mismatch between inserted and fetched")
                    }
                } else {
                    Log.e("RepositoryTest", "❌ Failed to fetch inserted User")
                }
    
                // Test repository's updateRemote
                fetchedUser?.let { User ->
                    val updatedUser = User.copy(
                        email = "Updated${User.id}",
                    )
                    Log.d("RepositoryTest", "Attempting repository.updateRemote()")
                    val remoteUser = repository.updateRemote(updatedUser)
                    Log.d(
                        "RepositoryTest",
                        "✅ Success - updateRemote completed, user updated_at: ${remoteUser.updated_at}"
                    )
                }
    
                // Test repository's deleteRemote
                serverId.let { id ->
                    Log.d("RepositoryTest", "Attempting repository.deleteRemote()")
                    repository.deleteRemote(id)
                    Log.d("RepositoryTest", "✅ Success - deleteRemote completed")
    
                    // Verify deletion
                    val deletedUser = repository.getByIdRemote(id)
                    if (deletedUser == null) {
                        Log.d("RepositoryTest", "✅ Verification - User successfully deleted")
                    } else {
                        Log.e("RepositoryTest", "❌ Verification - User still exists after deletion")
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
            val unsyncedUser1 =
                UserTestUtils.generateTestUser("User1").copy(isSynced = false)
            val unsyncedUser2 =
                UserTestUtils.generateTestUser("User2").copy(isSynced = false)
            repository.insertLocal(unsyncedUser1)
            repository.insertLocal(unsyncedUser2)
    
            // Trigger sync
            repository.syncChanges()
    
            // Verify sync - now we should have serverId populated
            val afterSync1 = repository.getByIdLocal(unsyncedUser1.id)
            val afterSync2 = repository.getByIdLocal(unsyncedUser2.id)
            logTestResult(
                "Sync Created Items",
                afterSync1?.serverId != null && afterSync2?.serverId != null
            )
    
            // Clean up
            afterSync1?.let { repository.deleteRemote(it.serverId!!) }
            afterSync2?.let { repository.deleteRemote(it.serverId!!) }
            repository.deleteLocal(unsyncedUser1.id, "Real")
            repository.deleteLocal(unsyncedUser2.id, "Real")
        }
    
        private suspend fun testObservationFlows() {
            Log.d("RepositoryTest", "-- Testing Observation Flows --")
            val testUser2 = UserTestUtils.generateTestUser(
                "ObservationTestAll-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            repository.insertLocal(testUser2)
            // Test observeAllActive
            val activeFlow = repository.observeAllActive()
            val activeUsers = activeFlow.first()
            logTestResult("ObserveAllActive", activeUsers.isNotEmpty())
    
            // Test observeById
            val testUser = UserTestUtils.generateTestUser(
                "ObservationTest-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            repository.insertLocal(testUser)
            val byIdFlow = repository.observeById(testUser.id)
            val observedUser = byIdFlow.first()
            logTestResult("ObserveById", observedUser?.id == testUser.id)
    
            // Clean up
            repository.deleteLocal(testUser.id, "Real")
            repository.deleteLocal(testUser2.id, "Real")
            repository.deleteRemote(testUser.id)
            repository.deleteRemote(testUser2.id)
    
        }
    
        private suspend fun testSearchOperations() {
            Log.d("RepositoryTest", "-- Testing Search Operations --")
    
            // Test search by name
            val searchUser = UserTestUtils.generateTestUser(
                "TestSearch.${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            repository.insertLocal(searchUser)

            // Clean up
            repository.deleteLocal(searchUser.id, "Real")
            repository.deleteRemote(searchUser.id)
        }
    
        private suspend fun testCountOperations() {
            Log.d("RepositoryTest", "-- Testing Count Operations --")
    
            val initialActiveCount = repository.getActiveCount()
    
            // Add test data
            val testUser = UserTestUtils.generateTestUser(
                "TestCount.${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            repository.insertLocal(testUser)
    
            val afterInsertActiveCount = repository.getActiveCount()
            logTestResult("Active Count", afterInsertActiveCount > initialActiveCount)
    
            // Clean up
            repository.deleteLocal(testUser.id, "Real")
            repository.deleteRemote(testUser.id)
        }
    
        private fun logTestResult(testName: String, passed: Boolean) {
            Log.d("RepositoryTest", "$testName: ${if (passed) "PASSED" else "FAILED"}")
        }
    
        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun testInsertUpdateDeleteFunctions() {
            Log.d("RepositoryTest", "-- Teste: Insert, Update e Delete (Local e Remoto) --")
    
            // Teste remoto (com internet)
            if (connectivityMonitor.isConnected) {
                val remoteUser = UserTestUtils.generateTestUser(
                    "RemoteTest-${
                        UUID.randomUUID().toString().substring(0, 8)
                    }"
                )
                val insertedRemote = repository.insert(remoteUser)
                logTestResult(
                    "Insert remoto",
                    insertedRemote.isSynced && insertedRemote.serverId != null
                )
    
                val updatedRemote = insertedRemote.copy(email = "Remoto Atualizado")
                repository.update(updatedRemote)
                val fetchedRemote = repository.getByIdRemote(updatedRemote.serverId!!)
                logTestResult("Update remoto", fetchedRemote?.email == "Remoto Atualizado")
    
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
            val localUser = UserTestUtils.generateTestUser(localTestId)
            val insertedLocal = repository.insert(localUser)
            logTestResult("Insert local", !insertedLocal.isSynced)
    
            val updatedLocal = insertedLocal.copy(email = "Local Atualizado")
            repository.update(updatedLocal)
            val fetchedLocal = repository.getByIdLocal(insertedLocal.id)
            logTestResult("Update local", fetchedLocal?.email == "Local Atualizado")
    
            repository.delete(insertedLocal.id)
            val deletedLocal = repository.getByIdLocal(insertedLocal.id)
            logTestResult("Delete local", deletedLocal?.deletedAt != null)
    
            val localUser2 =
                UserTestUtils.generateTestUser(UUID.randomUUID().toString().substring(0, 8))
            val insertedLocal2 = repository.insert(localUser2)
            logTestResult("Insert local", !insertedLocal.isSynced)
            // Reconecte a internet e sincronize
            Log.d(
                "RepositoryTest",
                "Reconecte a internet para testar sincronização. Aguardando 30 segundos..."
            )
            kotlinx.coroutines.delay(30_000)
    
            repository.syncChanges()
            val syncedUser = repository.getByIdLocal(insertedLocal2.id)
            repository.delete(insertedLocal2.id)
            logTestResult(
                "Sync após reconexão",
                syncedUser?.serverId != null && syncedUser.isSynced
            )
    
    
        }
    
    
    }
