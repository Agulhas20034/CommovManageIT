package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.Customer
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock.System
import java.util.UUID
import kotlin.random.Random

object CustomerTestUtils {
    fun generateTestCustomer(prefix: String = ""): Customer {
        return Customer(
            id = UUID.randomUUID().toString(),
            name = "TestCustomer_$prefix",
            email = "test_$prefix@example.com",
            phone_Number = "555-${Random.nextInt(1000,9999)}",
            createdAt = System.now(),
            updatedAt = System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printCustomer(customer: Customer, tag: String = "") {
        println("$tag Customer: ${customer.id}")
        println("Name: ${customer.name}")
        println("Email: ${customer.email}")
        println("Phone: ${customer.phone_Number}")
        println("Synced: ${customer.isSynced}")
        println("ServerID: ${customer.serverId}")
        println("---------------------")
    }

    suspend fun waitForSync(delay: Long = 2000) {
        delay(delay)
    }
}