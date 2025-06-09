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
import com.example.commovmanageit.tests.ProjectRepositoryTest
import com.example.commovmanageit.tests.RoleRepositoryTest
import com.example.commovmanageit.tests.UsersRepositoryTests
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

class TestActivityRole : AppCompatActivity() {

    private lateinit var app : com.example.commovmanageit.App
    private lateinit var repositoryTest: RoleRepositoryTest

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        app = application as com.example.commovmanageit.App
        val roleRepository = app.roleRepository
        var connectivityMonitor = app.connectivityMonitor
        val appCoroutineScope = CoroutineScope(Dispatchers.IO)
        connectivityMonitor = ConnectivityMonitor(this)

        repositoryTest = RoleRepositoryTest(roleRepository, connectivityMonitor)

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
            Toast.makeText(this@TestActivityRole, message, Toast.LENGTH_SHORT).show()
        }
    }
}

class TestActivityUser : AppCompatActivity() {

    private lateinit var app : com.example.commovmanageit.App
    private lateinit var repositoryTest: UsersRepositoryTests

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        app = application as com.example.commovmanageit.App
        val userRepository = app.userRepository
        var connectivityMonitor = app.connectivityMonitor
        val appCoroutineScope = CoroutineScope(Dispatchers.IO)
        connectivityMonitor = ConnectivityMonitor(this)

        repositoryTest = UsersRepositoryTests(userRepository, connectivityMonitor)

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
            Toast.makeText(this@TestActivityUser, message, Toast.LENGTH_SHORT).show()
        }
    }
}

class TestActivityProject : AppCompatActivity() {

    private lateinit var app : com.example.commovmanageit.App
    private lateinit var repositoryTest: ProjectRepositoryTest

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        app = application as com.example.commovmanageit.App
        val projectRepository = app.projectRepository
        var connectivityMonitor = app.connectivityMonitor
        val appCoroutineScope = CoroutineScope(Dispatchers.IO)
        connectivityMonitor = ConnectivityMonitor(this)

        repositoryTest = ProjectRepositoryTest(projectRepository, connectivityMonitor)

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
            Toast.makeText(this@TestActivityProject, message, Toast.LENGTH_SHORT).show()
        }
    }
}