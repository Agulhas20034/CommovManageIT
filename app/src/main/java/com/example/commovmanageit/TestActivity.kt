package com.example.commovmanageit

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commovmanageit.db.AppDatabase
import com.example.commovmanageit.db.entities.Customer
import com.example.commovmanageit.db.repositories.CustomerRepository
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class TestActivity : AppCompatActivity() {

    private lateinit var customerRepository: CustomerRepository
    private val appCoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var connectivityMonitor: ConnectivityMonitor

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        connectivityMonitor = ConnectivityMonitor(this)

        findViewById<Button>(R.id.runTestButton).setOnClickListener {
            safeInsertCustomer()
        }

        initializeDatabase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@TestActivity)
                customerRepository = CustomerRepository(
                    customerDao = db.customerDao(),
                    connectivityMonitor = connectivityMonitor,
                    coroutineScope = appCoroutineScope
                )
            } catch (e: Exception) {
                showToast("Database initialization failed: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun safeInsertCustomer() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val customer = Customer(
                    id = UUID.randomUUID().toString(),
                    serverId = null,
                    name = "Test Customer",
                    email = "test@example.com",
                    phoneNumber = "123-456-7890",
                    createdAt = Date(System.currentTimeMillis()),
                    updatedAt = Date(System.currentTimeMillis()),
                    deletedAt = null,
                    isSynced = false
                )

                customerRepository.insert(customer)
                showToast("Customer saved successfully!")
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@TestActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}