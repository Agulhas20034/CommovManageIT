package com.example.commovmanageit.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

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
            install(Postgrest)
        }
    }

    // Generic CRUD Operations
    suspend inline fun <reified T : Any> insert(table: String, data: T): T {
        return client.postgrest[table].insert(data).decodeSingle()
    }

    suspend inline fun <reified T : Any> update(table: String, id: String, data: T): T {
        return client.postgrest[table].update(data) {
            filter { eq("id", id) }
        }.decodeSingle()
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

}