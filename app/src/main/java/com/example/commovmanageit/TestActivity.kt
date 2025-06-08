package com.example.commovmanageit

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commovmanageit.db.repositories.CustomerRepositoryTest
import com.example.commovmanageit.tests.LogRepositoryTest
import com.example.commovmanageit.tests.PermissionRepositoryTest
import com.example.commovmanageit.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestActivityCustomer : AppCompatActivity() {

    private lateinit var app : com.example.commovmanageit.App
    private lateinit var repositoryTest: CustomerRepositoryTest

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        app = application as com.example.commovmanageit.App
        val customerRepository = app.customerRepository
        var connectivityMonitor = app.connectivityMonitor
        val appCoroutineScope = CoroutineScope(Dispatchers.IO)
        connectivityMonitor = ConnectivityMonitor(this)

        repositoryTest = CustomerRepositoryTest(customerRepository, connectivityMonitor)

        findViewById<Button>(R.id.runTestButton).setOnClickListener {
            runAllTests()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun runAllTests() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repositoryTest.runAllTests()
                showToast("All tests executed. Check Logcat for results.")
            } catch (e: Exception) {
                showToast("Test execution failed: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@TestActivityCustomer, message, Toast.LENGTH_SHORT).show()
        }
    }
}

class TestActivityLog : AppCompatActivity() {

    private lateinit var app : com.example.commovmanageit.App
    private lateinit var repositoryTest: LogRepositoryTest

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        app = application as com.example.commovmanageit.App
        val customerRepository = app.logsRepository
        var connectivityMonitor = app.connectivityMonitor
        val appCoroutineScope = CoroutineScope(Dispatchers.IO)
        connectivityMonitor = ConnectivityMonitor(this)

        repositoryTest = LogRepositoryTest(customerRepository, connectivityMonitor)

        findViewById<Button>(R.id.runTestButton).setOnClickListener {
            runAllTests()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun runAllTests() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repositoryTest.runAllTests()
                showToast("All tests executed. Check Logcat for results.")
            } catch (e: Exception) {
                showToast("Test execution failed: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@TestActivityLog, message, Toast.LENGTH_SHORT).show()
        }
    }
}

class TestActivityPermission : AppCompatActivity() {

    private lateinit var app : com.example.commovmanageit.App
    private lateinit var repositoryTest: PermissionRepositoryTest

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        app = application as com.example.commovmanageit.App
        val permissionRepository = app.permissionRepository
        var connectivityMonitor = app.connectivityMonitor
        val appCoroutineScope = CoroutineScope(Dispatchers.IO)
        connectivityMonitor = ConnectivityMonitor(this)

        repositoryTest = PermissionRepositoryTest(permissionRepository, connectivityMonitor)

        findViewById<Button>(R.id.runTestButton).setOnClickListener {
            runAllTests()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun runAllTests() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repositoryTest.runAllTests()
                showToast("All tests executed. Check Logcat for results.")
            } catch (e: Exception) {
                showToast("Test execution failed: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@TestActivityPermission, message, Toast.LENGTH_SHORT).show()
        }
    }
}