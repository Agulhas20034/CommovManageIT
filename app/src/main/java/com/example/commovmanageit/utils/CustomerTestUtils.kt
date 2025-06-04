import com.example.commovmanageit.db.entities.Customer
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

object CustomerTestUtils {
    fun generateTestCustomer(prefix: String = ""): Customer {
        return Customer(
            id = UUID.randomUUID().toString(),
            name = "TestCustomer_$prefix",
            email = "test_$prefix@example.com",
            phoneNumber = "555-${Random.nextInt(1000,9999)}"
        )
    }

    fun printCustomer(customer: Customer, tag: String = "") {
        println("$tag Customer: ${customer.id}")
        println("Name: ${customer.name}")
        println("Email: ${customer.email}")
        println("Phone: ${customer.phoneNumber}")
        println("Synced: ${customer.isSynced}")
        println("ServerID: ${customer.serverId}")
        println("---------------------")
    }

    suspend fun waitForSync(delay: Long = 2000) {
        delay(delay)
    }
}