package com.example.commovmanageit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.db.entities.TaskUser
import com.example.commovmanageit.remote.dto.toLocal
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.commovmanageit.utils.BottomNavigationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID

class AddUserToTaskActivity : AppCompatActivity() {

    private val projectusersRepository by lazy { (application as App).projectusersRepository }
    private val projectRepository by lazy { (application as App).projectRepository }
    private val taskRepository by lazy { (application as App).taskRepository }
    private val taskuserRepository by lazy { (application as App).taskuserRepository }
    private val userRepository by lazy { (application as App).userRepository }
    private val roleRepository by lazy { (application as App).roleRepository }
    private var currentLanguage: String = "en"
    lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.add_user_to_task)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.tvCurrentLanguage)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val spinnerUsers = findViewById<Spinner>(R.id.spinnerUsers)
        val spinnerTasks = findViewById<Spinner>(R.id.spinnerTask)
        val btnAddUser = findViewById<Button>(R.id.btnAddUser)
        tvCurrentLanguage?.text = currentLanguage

        val projectId = intent.getStringExtra("projectId") ?: return

        btnBack?.setOnClickListener { finish() }

        btnLanguage?.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }

        CoroutineScope(Dispatchers.Main).launch {
            val currentUser = (application as App).currentUser
            if (currentUser == null) {
                startActivity(Intent(this@AddUserToTaskActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                runOnUiThread {
                    bottomNav?.post {
                        BottomNavigationHelper(this@AddUserToTaskActivity, bottomNav, userRole).setup()
                    }
                }
            }

            val users = userRepository.getByProjectIdRemote(projectId)
            val userAdapter = ArrayAdapter(
                this@AddUserToTaskActivity,
                android.R.layout.simple_spinner_item,
                users?.map { it.email } ?: emptyList()
            )
            userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUsers.adapter = userAdapter

            val tasks = taskRepository.getByProjectIdRemote(projectId)
            Log.d("AddUserToTaskActivity", "Tasks loaded: ${tasks?.map { it.name }}")
            val taskMap = tasks?.associate { it.name to it.id } ?: emptyMap()
            val taskAdapter = ArrayAdapter(
                this@AddUserToTaskActivity,
                android.R.layout.simple_spinner_item,
                taskMap.keys.toList()
            )
            taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTasks.adapter = taskAdapter
        }

        btnAddUser.setOnClickListener {
            val selectedUserEmail = spinnerUsers.selectedItem as? String
            val selectedTaskName = spinnerTasks.selectedItem as? String

            if (selectedUserEmail != null && selectedTaskName != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val user = userRepository.getByemailRemote(selectedUserEmail)?.toLocal()
                    val tasks = taskRepository.getByProjectIdRemote(projectId)
                    val taskMap = tasks?.associate { it.name to it.id } ?: emptyMap()
                    val selectedTaskId = taskMap[selectedTaskName]
                    if (user != null && selectedTaskId != null) {
                        val taskUser = TaskUser(
                            id = UUID.randomUUID().toString(),
                            taskId = selectedTaskId,
                            userId = user.serverId.toString(),
                            startDate = Clock.System.now(),
                            endDate = null,
                            location = "default",
                            conclusionRate = 1.0f,
                            timeUsed = 1.0f,
                        )
                        taskuserRepository.insert(taskUser)
                        Toast.makeText(
                            this@AddUserToTaskActivity,
                            getString(R.string.user_added_to_task),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddUserToTaskActivity,
                            getString(R.string.select_user_and_task),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@AddUserToTaskActivity,
                    getString(R.string.select_user_and_task),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}