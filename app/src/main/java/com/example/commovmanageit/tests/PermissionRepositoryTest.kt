package com.example.commovmanageit.tests

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.entities.Permission
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.repository.PermissionRepository
import com.example.commovmanageit.utils.ConnectivityMonitor
import com.example.commovmanageit.utils.PermissionTestUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class PermissionRepositoryTest(
    private val repository: PermissionRepository,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val testScope = CoroutineScope(Dispatchers.IO)

    fun runAllTests() {
        testScope.launch {
            try {
                Log.d("PermissionRepositoryTest", "=== Iniciando Testes de Permissão ===")
                repository.clearLocalDatabase()
                testLocalOperations()
                repository.syncChanges()
                testRemoteOperations()
                //testInsertUpdateDeleteFunctions()
                repository.clearLocalDatabase()
                Log.d("PermissionRepositoryTest", "=== Todos os Testes de Permissão Finalizados ===")
            } catch (e: Exception) {
                Log.e("PermissionRepositoryTest", "Falha no teste", e)
            }
        }
    }

    private suspend fun testLocalOperations() {
        Log.d("PermissionRepositoryTest", "-- Testando Operações Locais --")

        // Inserir
        var testPermission = PermissionTestUtils.generateTestPermission()
        testPermission = testPermission.copy(label = "Permissão Local ${UUID.randomUUID().toString().substring(0, 8)}")
        val inserted = repository.insertLocal(testPermission)
        logTestResult("Insert Local", inserted.id == testPermission.id)

        val retrieved = repository.getByIdLocal(inserted.id)
        logTestResult("GetById Local", retrieved?.id == inserted.id)

        val updatedPermission = retrieved?.copy(label = "Atualizada${retrieved.id}")
        if (updatedPermission != null) {
            repository.updateLocal(updatedPermission)
            val afterUpdate = repository.getByIdLocal(updatedPermission.id)
            logTestResult("Update Local", afterUpdate?.label == "Atualizada${retrieved?.id}")
        }

        repository.deleteLocal(inserted.id)
        val afterDelete = repository.getByIdLocal(inserted.id)
        logTestResult("Delete Local", afterDelete == null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun testRemoteOperations() {
        if (!connectivityMonitor.isConnected) {
            Log.d("PermissionRepositoryTest", "-- Pulando Operações Remotas (sem internet) --")
            return
        }

        Log.d("PermissionRepositoryTest", "-- Testando Operações Remotas --")

        val testPermission = PermissionTestUtils.generateTestPermission("repo-test-${UUID.randomUUID().toString().substring(0, 8)}")
        PermissionTestUtils.printPermission(testPermission, "Permissão de Teste Gerada")

        try {
            Log.d("PermissionRepositoryTest", "Tentando repository.insertRemote()")
            val serverId = repository.insertRemote(testPermission)
            Log.d("PermissionRepositoryTest", "✅ Sucesso - insertRemote retornou serverId: $serverId")

            Log.d("PermissionRepositoryTest", "Buscando permissão inserida")
            val fetchedRemotePermission = repository.getByIdRemote(serverId)
            if (fetchedRemotePermission != null) {
                Log.d("PermissionRepositoryTest", "✅ Permissão remota buscada com sucesso")
                PermissionTestUtils.printPermission(fetchedRemotePermission.toLocal(), "Permissão Buscada")

                if (fetchedRemotePermission.label == testPermission.label) {
                    Log.d("PermissionRepositoryTest", "✅ Dados conferem")
                } else {
                    Log.e("PermissionRepositoryTest", "❌ Dados divergentes entre inserido e buscado")
                }
            } else {
                Log.e("PermissionRepositoryTest", "❌ Falha ao buscar permissão inserida")
            }

            fetchedRemotePermission?.let { permission ->
                val updatedPermission = permission.toLocal().copy(label = "Atualizada ${permission.label}")
                Log.d("PermissionRepositoryTest", "Tentando repository.updateRemote()")
                val remotePermission = repository.updateRemote(updatedPermission)
                Log.d("PermissionRepositoryTest", "✅ Sucesso - updateRemote, atualizado em: ${remotePermission.updated_at}")
            }

            serverId.let { id ->
                Log.d("PermissionRepositoryTest", "Tentando repository.deleteRemote()")
                repository.deleteRemote(id)
                Log.d("PermissionRepositoryTest", "✅ Sucesso - deleteRemote concluído")

                val deletedPermission = repository.getByIdRemote(id)
                if (deletedPermission == null) {
                    Log.d("PermissionRepositoryTest", "✅ Permissão deletada com sucesso")
                } else {
                    Log.e("PermissionRepositoryTest", "❌ Permissão ainda existe após deleção")
                }
            }

        } catch (e: Exception) {
            Log.e("PermissionRepositoryTest", "❌ Falha em operação remota", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun testInsertUpdateDeleteFunctions() {
        Log.d("PermissionRepositoryTest", "-- Teste: Insert, Update e Delete (Local e Remoto) --")

        if (connectivityMonitor.isConnected) {
            val remotePermission = PermissionTestUtils.generateTestPermission("RemoteTest-${UUID.randomUUID().toString().substring(0, 8)}")
            val insertedRemote = repository.insert(remotePermission)
            logTestResult("Insert Remoto", insertedRemote.isSynced && insertedRemote.serverId != null)

            val updatedRemote = insertedRemote.copy(label = "Remoto Atualizado")
            repository.update(updatedRemote)
            val fetchedRemote = repository.getByIdRemote(updatedRemote.serverId!!)
            logTestResult("Update Remoto", fetchedRemote?.label == "Remoto Atualizado")

            repository.deleteRemote(insertedRemote.id)
            val deletedRemote = repository.getByIdRemote(insertedRemote.serverId!!)
            logTestResult("Delete Remoto", deletedRemote == null)
        } else {
            Log.d("PermissionRepositoryTest", "Sem internet, pulando teste remoto.")
        }

        Log.d("PermissionRepositoryTest", "Desconecte a internet agora para testar operações locais. Aguardando 15 segundos...")
        kotlinx.coroutines.delay(15_000)

        val localTestId = "local-test-${UUID.randomUUID().toString().substring(0, 8)}"
        repository.deleteLocal(localTestId)

        val localPermission = PermissionTestUtils.generateTestPermission(localTestId)
        val insertedLocal = repository.insertLocal(localPermission)
        logTestResult("Insert Local", !insertedLocal.isSynced)

        val updatedLocal = insertedLocal.copy(label = "Local Atualizado")
        repository.updateLocal(updatedLocal)
        val fetchedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Update Local", fetchedLocal?.label == "Local Atualizado")

        repository.deleteLocal(insertedLocal.id)
        val deletedLocal = repository.getByIdLocal(insertedLocal.id)
        logTestResult("Delete Local", deletedLocal == null)

        val localPermission2 = PermissionTestUtils.generateTestPermission(UUID.randomUUID().toString().substring(0, 8))
        val insertedLocal2 = repository.insertLocal(localPermission2)
        logTestResult("Insert Local 2", !insertedLocal2.isSynced)

        Log.d("PermissionRepositoryTest", "Reconecte a internet para testar sincronização. Aguardando 15 segundos...")
        kotlinx.coroutines.delay(15_000)

        repository.syncChanges()
        val syncedPermission = repository.getByIdLocal(insertedLocal2.id)
        logTestResult("Sync após reconexão", syncedPermission?.serverId != null && syncedPermission.isSynced)
    }

    private fun logTestResult(testName: String, passed: Boolean) {
        Log.d("PermissionRepositoryTest", "$testName: ${if (passed) "PASSOU" else "FALHOU"}")
    }
}