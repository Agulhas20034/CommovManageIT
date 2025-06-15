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
    
            Log.d(
                "RepositoryTest",
                "Desconecte a internet agora para testar operações locais. Aguardando 30 segundos..."
            )
            kotlinx.coroutines.delay(30_000)
    
            val localTestId = "local-test-${UUID.randomUUID().toString().substring(0, 8)}"
            repository.deleteLocal(localTestId, "Real")
    
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
