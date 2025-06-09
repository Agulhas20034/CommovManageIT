package com.example.commovmanageit.tests

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.entities.Role
import com.example.commovmanageit.db.repositories.RoleRepository
import com.example.commovmanageit.utils.RoleTestUtils
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class RoleRepositoryTest(
    private val repository: RoleRepository,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val testScope = CoroutineScope(Dispatchers.IO)

    fun runAllTests() {
        testScope.launch {
            try {
                Log.d("RoleRepositoryTest", "=== Iniciando Testes ===")
                repository.clearLocalDatabase()
                testInsertUpdateDeleteFunctions()
                repository.clearLocalDatabase()
                Log.d("RoleRepositoryTest", "=== Todos os Testes Finalizados ===")
            } catch (e: Exception) {
                Log.e("RoleRepositoryTest", "Falha no teste", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun testInsertUpdateDeleteFunctions() {
        Log.d("RoleRepositoryTest", "-- Teste: Insert, Update e Delete (Local e Remoto) --")

        // Teste remoto (com internet)
        if (connectivityMonitor.isConnected) {
            val remoteRole = RoleTestUtils.generateTestRole("RemoteTest-${UUID.randomUUID().toString().substring(0, 8)}")
            val insertedRemote = repository.insert(remoteRole)
            logTestResult("Insert remoto", insertedRemote.isSynced && insertedRemote.serverId != null)

            val updatedRemote = insertedRemote.copy(name = "Remoto Atualizado")
            repository.update(updatedRemote)
            val fetchedRemote = repository.getByIdLocal(updatedRemote.id)
            logTestResult("Update remoto", fetchedRemote?.name == "Remoto Atualizado")

            repository.delete(insertedRemote.id)
            val deletedRemote = repository.getByIdLocal(insertedRemote.id)
            logTestResult("Delete remoto", deletedRemote?.deletedAt != null)


        } else {
            Log.d("RoleRepositoryTest", "Sem internet, pulando teste remoto.")
        }
        // Delay para desconectar a internet manualmente
        Log.d(
            "RepositoryTest",
            "Desconecte a internet agora para testar operações locais. Aguardando 15 segundos..."
        )
        kotlinx.coroutines.delay(15_000)
        // Teste local (sem internet)
        val localRole = RoleTestUtils.generateTestRole("LocalTest-${UUID.randomUUID().toString().substring(0, 8)}")
        val insertedLocal = repository.insert(localRole)
        logTestResult("Insert local", !insertedLocal.isSynced)
        RoleTestUtils.printRole(localRole)

        val updatedLocal = insertedLocal.copy(name = "Local Atualizado")
        repository.update(updatedLocal)
        val fetchedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Update local", fetchedLocal?.name == "Local Atualizado")
        repository.delete(insertedLocal.id)
        val deletedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Delete local", deletedLocal?.deletedAt != null)

        // Delay para desconectar a internet manualmente
        Log.d(
            "RepositoryTest",
            "Reconecte a internet agora para testar operações locais. Aguardando 30 segundos..."
        )
        kotlinx.coroutines.delay(15_000)

        repository.syncChanges()

    }

    private fun logTestResult(testName: String, passed: Boolean) {
        Log.d("RoleRepositoryTest", "$testName: ${if (passed) "PASSOU" else "FALHOU"}")
    }
}