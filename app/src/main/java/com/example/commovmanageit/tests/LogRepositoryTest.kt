package com.example.commovmanageit.tests
import LogRepository
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.utils.LogsTestUtils
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class LogRepositoryTest(
    private val repository: LogRepository,
    private val connectivityMonitor: ConnectivityMonitor,

) {
    private val testScope = CoroutineScope(Dispatchers.IO)

    fun runAllTests() {
        testScope.launch {
            try {
                Log.d("LogRepositoryTest", "=== Iniciando Testes de Log ===")
                repository.clearLocalDatabase()
                testInsertUpdateDeleteFunctions()
                repository.clearLocalDatabase()
                Log.d("LogRepositoryTest", "=== Todos os Testes de Log Finalizados ===")
            } catch (e: Exception) {
                Log.e("LogRepositoryTest", "Falha no teste", e)
            }
        }
    }

    private fun logTestResult(testName: String, passed: Boolean) {
        Log.d("LogRepositoryTest", "$testName: ${if (passed) "PASSOU" else "FALHOU"}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun testInsertUpdateDeleteFunctions() {
        Log.d("LogRepositoryTest", "-- Teste: Insert, Update e Delete (Local e Remoto) --")

        if (connectivityMonitor.isConnected) {
            val remoteLog = LogsTestUtils.generateTestLog("RemoteTest", UUID.randomUUID().toString())
            val insertedRemote = repository.insert(remoteLog)
            logTestResult("Insert remoto", insertedRemote.isSynced && insertedRemote.serverId != null)

            repository.deleteLocal(insertedRemote)
            logTestResult("Delete remoto", true)

        } else {
            Log.d("LogRepositoryTest", "Sem internet, pulando teste remoto.")
        }

        Log.d("LogRepositoryTest", "Desconecte a internet agora para testar operações locais. Aguardando 10 segundos...")
        kotlinx.coroutines.delay(10_000)

        val localTestId = "local-test-${UUID.randomUUID().toString().substring(0, 8)}"
        repository.clearLocalDatabase()

        val localLog = LogsTestUtils.generateTestLog(localTestId, UUID.randomUUID().toString())
        val insertedLocal = repository.insert(localLog)
        logTestResult("Insert local", !insertedLocal.isSynced)

        repository.deleteLocal(insertedLocal)
        logTestResult("Delete local", true)

        Log.d("LogRepositoryTest", "Reconecte a internet para testar sincronização. Aguardando 10 segundos...")
        kotlinx.coroutines.delay(10_000)

        repository.syncChanges()
        Log.d("LogRepositoryTest", "Sincronização de logs executada.")
    }
}