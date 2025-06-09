package com.example.commovmanageit.remote

import android.util.Log
import com.example.commovmanageit.db.entities.User
import com.example.commovmanageit.remote.dto.CustomerRemote
import com.example.commovmanageit.remote.dto.LogsRemote
import com.example.commovmanageit.remote.dto.PermissionRemote
import com.example.commovmanageit.remote.dto.ProjectRemote
import com.example.commovmanageit.remote.dto.RoleRemote
import com.example.commovmanageit.remote.dto.UserRemote
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

    suspend inline fun <reified T : Any> insertRole(data: T): RoleRemote {
        val response = client.postgrest["roles"].insert(data) { select() }.decodeSingle<RoleRemote>()
        Log.d("RoleInsert", "Role inserido: $response")
        return response
    }

    suspend inline fun <reified T : Any> insertUser(data: T): UserRemote {
        val response = client.postgrest["users"].insert(data){ select() }.decodeSingle<UserRemote>()
        Log.d("UserInsert", "User inserido: $response")
        return response
    }

    suspend inline fun <reified T : Any> insertProject(data: T): ProjectRemote {
        val response = client.postgrest["projects"].insert(data){ select() }.decodeSingle<ProjectRemote>()
        Log.d("ProjectInsert", "Projeto inserido: $response")
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
            filter { PermissionRemote::id eq id }
        }
        return fetchById("permissions",id)
    }

    suspend inline fun <reified T : Any> updateRole(id: String, data: RoleRemote): RoleRemote{
        client.postgrest["roles"].update({
            RoleRemote::name setTo data.name
            RoleRemote::updated_at setTo Clock.System.now().toString()
        }) {
            filter { RoleRemote::id eq id }
        }
        return fetchById("roles",id)
    }

    suspend inline fun <reified T : Any> updateUser(id: String, data: UserRemote): UserRemote{
        client.postgrest["users"].update({
            UserRemote::role_id setTo data.role_id
            UserRemote::email setTo data.email
            UserRemote::password setTo data.password
            UserRemote::daily_work_hours setTo data.daily_work_hours
            UserRemote::updated_at setTo Clock.System.now().toString()
        }) {
            filter { UserRemote::id eq id }
        }
        return fetchById("users",id)
    }

    suspend inline fun <reified T : Any> updateProject(id: String, data: ProjectRemote): ProjectRemote{
        client.postgrest["projects"].update({
            ProjectRemote::name setTo data.name
            ProjectRemote::daily_work_hours setTo data.daily_work_hours
            ProjectRemote::customer_id setTo data.customer_id
            ProjectRemote::user_id setTo data.user_id
            ProjectRemote::hourly_rate setTo data.hourly_rate
            ProjectRemote::updated_at setTo Clock.System.now().toString()
        }) {
            filter { ProjectRemote::id eq id }
        }
        return fetchById("projects",id)
    }
}