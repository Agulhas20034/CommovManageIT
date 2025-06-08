package com.example.commovmanageit.remote

import android.util.Log
import com.example.commovmanageit.remote.dto.CustomerRemote
import com.example.commovmanageit.remote.dto.LogsRemote
import com.example.commovmanageit.remote.dto.PermissionRemote
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

object SupabaseManager {
    private lateinit var table: String
    private lateinit var schema: String
    private const val SUPABASE_URL = "https://sjmonvblderewjyznzna.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNqbW9udmJsZGVyZXdqeXpuem5hIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU3MTg3MjYsImV4cCI6MjA2MTI5NDcyNn0.k1ae3t4jbdghqopg0kMW_qVgVE1NK-fE9066cusgdR0"
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest) {
                serializer = KotlinXSerializer()

            }
            install(Storage){
                transferTimeout = 90.seconds
            }
            install(Realtime){
                reconnectDelay = 5.seconds
            }
        }

    }

    suspend fun delete(table: String, id: String) {
        client.postgrest[table].delete {
            filter { eq("id", id) }
        }
    }

    suspend inline fun <reified T : Any> fetchById(table: String, id: String): T {
        return client.postgrest[table].select {
            filter { eq("id", id) }
        }.decodeSingle()
    }

    suspend inline fun <reified T : Any> fetchAll(table: String): List<T> {
        return client.postgrest[table].select().decodeList()
    }

    suspend inline fun <reified T : Any> getAll(tableName: String): List<T> {
        return client.postgrest.from(tableName).select().decodeList<T>()
    }

    suspend inline fun <reified T : Any> insertCustomer(data: T): CustomerRemote {
        val response = client.postgrest["customers"].insert(data){ select() }.decodeSingle<CustomerRemote>()
        Log.d("id:"," ${response.id}")
        Log.d("email: ","${response.email}")
        return response
    }

    suspend inline fun <reified T : Any> insertLog(data: T): LogsRemote {
        val response = client.postgrest["logs"].insert(data) { select() }.decodeSingle<LogsRemote>()
        Log.d("LogInsert", "Log inserido: $response")
        return response
    }

    suspend inline fun <reified T : Any> insertPermission(data: T): PermissionRemote {
        val response = client.postgrest["permissions"].insert(data) { select() }.decodeSingle<PermissionRemote>()
        Log.d("PermissionInsert", "Permissao inserida: $response")
        return response
    }

    suspend inline fun <reified T : Any> updateCustomer(id: String, data: CustomerRemote): CustomerRemote{
        client.postgrest["customers"].update({
            CustomerRemote::name setTo data.name
            CustomerRemote::email setTo data.email
            CustomerRemote::phone_number setTo data.phone_number
            CustomerRemote::updated_at setTo Clock.System.now().toString()
        }) {
            filter { CustomerRemote::id eq id }
        }
        return fetchById("customers",id)
    }

    suspend inline fun <reified T : Any> updatePermission(id: String, data: PermissionRemote): PermissionRemote{
        client.postgrest["permissions"].update({
            PermissionRemote::label setTo data.label
            PermissionRemote::updated_at setTo Clock.System.now().toString()
        }) {
            filter { CustomerRemote::id eq id }
        }
        return fetchById("permissions",id)
    }
}