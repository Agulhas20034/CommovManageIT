package com.example.commovmanageit.tests

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.db.repositories.CustomerRepository

@RequiresApi(Build.VERSION_CODES.O)
class CustomerRepositoryTests (
    private val repository: CustomerRepository,
    private val context: Context
) {
    private val testCustomers = mutableListOf<Customer>()

    suspend fun runAllTests() {
        try {
            val testCustomer = Customer(
                id = "test_${System.currentTimeMillis()}",
                name = "TEST",
                email = "test@test.com",
                phoneNumber = "1234567890"
            )

            val inserted = repository.insert(testCustomer)
            println("INSERTED: $inserted")

            val fetched = repository.getByIdLocal(inserted.id)
            println("FETCHED: $fetched")

        } catch (e: Exception) {
            println("FUCK IT FAILED: ${e.stackTraceToString()}")
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun testBasicCRUD() {
        println("\n=== TEST 1: Basic CRUD (Online Only) ===")

        val customer = CustomerTestUtils.generateTestCustomer("online_test")
        val inserted = repository.insert(customer)
        CustomerTestUtils.printCustomer(inserted, "Created")
        testCustomers.add(inserted)

        val fetched = repository.getByIdLocal(inserted.id)
        check(fetched != null) { "Failed to retrieve created customer" }
        CustomerTestUtils.printCustomer(fetched, "Fetched")

        val updated = fetched.copy(name = "Updated_${fetched.name}")
        repository.update(updated)
        val afterUpdate = repository.getByIdLocal(updated.id)
        check(afterUpdate?.name == updated.name) { "Update failed" }
        CustomerTestUtils.printCustomer(afterUpdate!!, "Updated")


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun testBatchOperations() {
        println("\n=== TEST 2: Batch Operations (Online Only) ===")

        val batch = List(3) { CustomerTestUtils.generateTestCustomer("batch_online_$it") }
        batch.forEach {
            val inserted = repository.insert(it)
            testCustomers.add(inserted)
        }

        val count = repository.getActiveCount()
        println("Total customers after batch: $count")

        repository.getAllLocal().forEach {
            CustomerTestUtils.printCustomer(it, "Batch Item")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun cleanup() {
        println("\nCleaning up test data...")
        testCustomers.forEach {
            try {
                repository.delete(it.id)
            } catch (e: Exception) {
                println("Failed to clean up ${it.id}: ${e.message}")
            }
        }
        println("Cleanup complete")
    }


}