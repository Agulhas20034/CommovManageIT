package com.example.commovmanageit.tests

import ProjectRepository
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.entities.Project
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.utils.ConnectivityMonitor
import com.example.commovmanageit.utils.ProjectTestUtils
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class ProjectRepositoryTest(
    private val repository: ProjectRepository,
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
            var testproject = ProjectTestUtils.generateTestProject()
            testproject = testproject.copy(
                name = "Test Local project ${UUID.randomUUID().toString().substring(0, 8)}"
            )
            val inserted = repository.insertLocal(testproject)
            logTestResult("Insert", inserted.id == testproject.id)

            // Test get by id
            val retrieved = repository.getByIdLocal(inserted.id)
            logTestResult("Get by ID", retrieved?.id == inserted.id)

            // Test update
            val updatedproject = retrieved?.copy(name = "Updated${retrieved.id}")
            if (updatedproject != null) {
                repository.updateLocal(updatedproject)
                val afterUpdate = repository.getByIdLocal(updatedproject.id)
                logTestResult("Update", afterUpdate?.name == "Updated${retrieved?.id}")
            }

            // Test delete
            repository.deleteLocal(inserted.id, "Test")
            val afterDelete = repository.getByIdLocal(inserted.id)
            logTestResult("Delete", afterDelete?.deletedAt != null)
            val updated: Project = updatedproject?.copy(
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

            val testproject = ProjectTestUtils.generateTestProject(
                "repo-test-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            ProjectTestUtils.printProject(testproject, "Generated Test project")

            try {
                Log.d("RepositoryTest", "Attempting repository.insertRemote()")
                val serverId = repository.insertRemote(testproject)
                Log.d("RepositoryTest", "✅ Success - insertRemote returned serverId: $serverId")

                Log.d("RepositoryTest", "Attempting to fetch inserted project")
                val fetchedRemoteproject = repository.getByIdRemote(serverId)
                val fetchedproject = fetchedRemoteproject?.toLocal()

                if (fetchedproject != null) {
                    Log.d("RepositoryTest", "✅ Successfully fetched inserted project")
                    ProjectTestUtils.printProject(fetchedproject, "Fetched project")

                    // Verify data matches
                    if (fetchedproject.name == testproject.name
                    ) {
                        Log.d("RepositoryTest", "✅ Data verification passed")
                    } else {
                        Log.e("RepositoryTest", "❌ Data mismatch between inserted and fetched")
                    }
                } else {
                    Log.e("RepositoryTest", "❌ Failed to fetch inserted project")
                }

                // Test repository's updateRemote
                fetchedproject?.let { project ->
                    val updatedproject = project.copy(
                        name = "Updated ${project.name}",
                    )
                    Log.d("RepositoryTest", "Attempting repository.updateRemote()")
                    val remoteproject = repository.updateRemote(updatedproject)
                    Log.d(
                        "RepositoryTest",
                        "✅ Success - updateRemote completed, user updated_at: ${remoteproject.updated_at}"
                    )
                }

                // Test repository's deleteRemote
                serverId.let { id ->
                    Log.d("RepositoryTest", "Attempting repository.deleteRemote()")
                    repository.deleteRemote(id)
                    Log.d("RepositoryTest", "✅ Success - deleteRemote completed")

                    // Verify deletion
                    val deletedproject = repository.getByIdRemote(id)
                    if (deletedproject == null) {
                        Log.d("RepositoryTest", "✅ Verification - project successfully deleted")
                    } else {
                        Log.e("RepositoryTest", "❌ Verification - project still exists after deletion")
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
            val unsyncedproject1 =
                ProjectTestUtils.generateTestProject("project1").copy(isSynced = false)
            val unsyncedproject2 =
                ProjectTestUtils.generateTestProject("project2").copy(isSynced = false)
            repository.insertLocal(unsyncedproject1)
            repository.insertLocal(unsyncedproject2)

            // Trigger sync
            repository.syncChanges()

            // Verify sync - now we should have serverId populated
            val afterSync1 = repository.getByIdLocal(unsyncedproject1.id)
            val afterSync2 = repository.getByIdLocal(unsyncedproject2.id)
            logTestResult(
                "Sync Created Items",
                afterSync1?.serverId != null && afterSync2?.serverId != null
            )

            // Clean up
            afterSync1?.let { repository.deleteRemote(it.serverId!!) }
            afterSync2?.let { repository.deleteRemote(it.serverId!!) }
            repository.deleteLocal(unsyncedproject1.id, "Real")
            repository.deleteLocal(unsyncedproject2.id, "Real")
        }

        private suspend fun testObservationFlows() {
            Log.d("RepositoryTest", "-- Testing Observation Flows --")
            val testproject2 = ProjectTestUtils.generateTestProject(
                "ObservationTestAll-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            repository.insertLocal(testproject2)
            // Test observeAllActive
            val activeFlow = repository.observeAllActive()
            val activeprojects = activeFlow.first()
            logTestResult("ObserveAllActive", activeprojects.isNotEmpty())

            // Test observeById
            val testproject = ProjectTestUtils.generateTestProject(
                "ObservationTest-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            repository.insertLocal(testproject)
            val byIdFlow = repository.observeById(testproject.id)
            val observedproject = byIdFlow.first()
            logTestResult("ObserveById", observedproject?.id == testproject.id)

            // Clean up
            repository.deleteLocal(testproject.id, "Real")
            repository.deleteLocal(testproject2.id, "Real")
            repository.deleteRemote(testproject.id)
            repository.deleteRemote(testproject2.id)

        }

        private suspend fun testSearchOperations() {
            Log.d("RepositoryTest", "-- Testing Search Operations --")

            // Test search by name
            val searchproject = ProjectTestUtils.generateTestProject(
                "TestSearch.${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            repository.insertLocal(searchproject)


            // Clean up
            repository.deleteLocal(searchproject.id, "Real")
            repository.deleteRemote(searchproject.id)
        }

        private suspend fun testCountOperations() {
            Log.d("RepositoryTest", "-- Testing Count Operations --")

            val initialActiveCount = repository.getActiveCount()

            // Add test data
            val testproject = ProjectTestUtils.generateTestProject(
                "TestCount.${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            repository.insertLocal(testproject)

            val afterInsertActiveCount = repository.getActiveCount()
            logTestResult("Active Count", afterInsertActiveCount > initialActiveCount)

            // Clean up
            repository.deleteLocal(testproject.id, "Real")
            repository.deleteRemote(testproject.id)
        }

        private fun logTestResult(testName: String, passed: Boolean) {
            Log.d("RepositoryTest", "$testName: ${if (passed) "PASSED" else "FAILED"}")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun testInsertUpdateDeleteFunctions() {
            Log.d("RepositoryTest", "-- Teste: Insert, Update e Delete (Local e Remoto) --")

            // Teste remoto (com internet)
            if (connectivityMonitor.isConnected) {
                val remoteproject = ProjectTestUtils.generateTestProject(
                    "RemoteTest-${
                        UUID.randomUUID().toString().substring(0, 8)
                    }"
                )
                val insertedRemote = repository.insert(remoteproject)
                logTestResult(
                    "Insert remoto",
                    insertedRemote.isSynced && insertedRemote.serverId != null
                )

                val updatedRemote = insertedRemote.copy(name = "Remoto Atualizado", serverId = insertedRemote.serverId)
                repository.update(updatedRemote)
                logTestResult("Update remoto", updatedRemote.isSynced)
                val fetchedRemote = repository.getByIdRemote(updatedRemote.id)
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
            val localproject = ProjectTestUtils.generateTestProject(localTestId)
            val insertedLocal = repository.insert(localproject)
            logTestResult("Insert local", !insertedLocal.isSynced)

            val updatedLocal = insertedLocal.copy(name = "Local Atualizado")
            repository.update(updatedLocal)
            val fetchedLocal = repository.getByIdLocal(insertedLocal.id)
            logTestResult("Update local", fetchedLocal?.name == "Local Atualizado")

            repository.delete(insertedLocal.id)
            val deletedLocal = repository.getByIdLocal(insertedLocal.id)
            logTestResult("Delete local", deletedLocal?.deletedAt != null)

            val localproject2 =
                ProjectTestUtils.generateTestProject(UUID.randomUUID().toString().substring(0, 8))
            val insertedLocal2 = repository.insert(localproject2)
            logTestResult("Insert local", !insertedLocal.isSynced)
            // Reconecte a internet e sincronize
            Log.d(
                "RepositoryTest",
                "Reconecte a internet para testar sincronização. Aguardando 30 segundos..."
            )
            kotlinx.coroutines.delay(30_000)

            repository.syncChanges()
            val syncedproject = repository.getByIdLocal(insertedLocal2.id)
            repository.delete(insertedLocal2.id)
            logTestResult(
                "Sync após reconexão",
                syncedproject?.serverId != null && syncedproject.isSynced
            )


        }
}