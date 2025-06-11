package com.example.commovmanageit.db.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.entities.ProjectUser
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.utils.ProjectUserTestUtils
import com.example.commovmanageit.utils.ConnectivityMonitor
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
@RequiresApi(Build.VERSION_CODES.O)
class ProjectUserRepositoryTest(
    private val repository: ProjectUserRepository,
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

    private fun logTestResult(testName: String, passed: Boolean) {
        Log.d("RepositoryTest", "$testName: ${if (passed) "PASSED" else "FAILED"}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun testInsertUpdateDeleteFunctions() {
        Log.d("RepositoryTest", "-- Teste: Insert, Update e Delete (Local e Remoto) --")

        // Teste remoto (com internet)
        if (connectivityMonitor.isConnected) {
            val remoteProjectUser = ProjectUserTestUtils.generateTestProjectUser(
                "RemoteTest-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            val insertedRemote = repository.insert(remoteProjectUser)
            logTestResult(
                "Insert remoto",
                insertedRemote.isSynced && insertedRemote.serverId != null
            )

            val updatedRemote = insertedRemote.copy()
            repository.update(updatedRemote)
            val fetchedRemote = repository.getByIdRemote(updatedRemote.serverId!!)
            logTestResult("Update remoto", fetchedRemote?.user_id == updatedRemote.id)

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
        val localProjectUser = ProjectUserTestUtils.generateTestProjectUser(localTestId)
        val insertedLocal = repository.insert(localProjectUser)
        logTestResult("Insert local", !insertedLocal.isSynced)

        val updatedLocal = insertedLocal.copy()
        repository.update(updatedLocal)
        val fetchedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Update local", fetchedLocal?.updatedAt != updatedLocal.createdAt)

        repository.delete(insertedLocal.id)
        val deletedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Delete local", deletedLocal?.deletedAt != null)

        val localProjectUser2 =
            ProjectUserTestUtils.generateTestProjectUser(UUID.randomUUID().toString().substring(0, 8))
        val insertedLocal2 = repository.insert(localProjectUser2)
        logTestResult("Insert local", !insertedLocal.isSynced)
        // Reconecte a internet e sincronize
        Log.d(
            "RepositoryTest",
            "Reconecte a internet para testar sincronização. Aguardando 30 segundos..."
        )
        kotlinx.coroutines.delay(30_000)

        repository.syncChanges()
        val syncedProjectUser = repository.getByIdLocal(insertedLocal2.id)
        repository.delete(insertedLocal2.id)
        logTestResult(
            "Sync após reconexão",
            syncedProjectUser?.serverId != null && syncedProjectUser.isSynced
        )


    }


}