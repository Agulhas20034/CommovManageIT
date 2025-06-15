package com.example.commovmanageit.db.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.utils.TaskTestUtils
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class TaskRepositoryTest(
    private val repository: TaskRepository,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val testScope = CoroutineScope(Dispatchers.IO)

    fun runAllTests() {
        testScope.launch {
            try {
                Log.d("RepositoryTest", "=== Starting Tests ===")
                repository.clearLocalDatabase()
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

        if (connectivityMonitor.isConnected) {
            val remoteTask = TaskTestUtils.generateTestTask(
                "RemoteTest-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
            )
            val insertedRemote = repository.insert(remoteTask)
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

        Log.d(
            "RepositoryTest",
            "Desconecte a internet agora para testar operações locais. Aguardando 30 segundos..."
        )
        kotlinx.coroutines.delay(30_000)

        val localTestId = "local-test-${UUID.randomUUID().toString().substring(0, 8)}"
        repository.deleteLocal(localTestId, "Real")

        val localTask = TaskTestUtils.generateTestTask(localTestId)
        val insertedLocal = repository.insert(localTask)
        logTestResult("Insert local", !insertedLocal.isSynced)

        val updatedLocal = insertedLocal.copy(name = "Local Atualizado")
        repository.update(updatedLocal)
        val fetchedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Update local", fetchedLocal?.name == "Local Atualizado")

        repository.delete(insertedLocal.id)
        val deletedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Delete local", deletedLocal?.deletedAt != null)

        val localTask2 =
            TaskTestUtils.generateTestTask(UUID.randomUUID().toString().substring(0, 8))
        val insertedLocal2 = repository.insert(localTask2)
        logTestResult("Insert local", !insertedLocal.isSynced)
        Log.d(
            "RepositoryTest",
            "Reconecte a internet para testar sincronização. Aguardando 30 segundos..."
        )
        kotlinx.coroutines.delay(30_000)

        repository.syncChanges()
        val syncedTask = repository.getByIdLocal(insertedLocal2.id)
        repository.delete(insertedLocal2.id)
        logTestResult(
            "Sync após reconexão",
            syncedTask?.serverId != null && syncedTask.isSynced
        )


    }


}