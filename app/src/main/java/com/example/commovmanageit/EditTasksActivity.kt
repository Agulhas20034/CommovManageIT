package com.example.commovmanageit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.db.entities.Task

import com.example.commovmanageit.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class EditTasksActivity : AppCompatActivity() {

    private val taskRepository by lazy { (application as App).taskRepository }
    private val projectRepository by lazy { (application as App).projectRepository }
    private val roleRepository by lazy { (application as App).roleRepository }
    private var currentLanguage: String = "en"
    lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.edit_task)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.CurrentLanguage)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val spinnerProject = findViewById<Spinner>(R.id.spinnerproject)
        val etTaskName = findViewById<TextInputEditText>(R.id.etTaskName)
        val etTaskDescription = findViewById<TextInputEditText>(R.id.etTaskDescription)
        val etHourlyRate = findViewById<TextInputEditText>(R.id.etHourlyRate)
        val spinnerStatus = findViewById<Spinner>(R.id.spinnerstatus)
        val btnUpdateTask = findViewById<Button>(R.id.btnUpdateTask)

        tvCurrentLanguage.text = if (currentLanguage == "en") "en" else "pt"

        btnBack.setOnClickListener { finish() }

        btnLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }

        CoroutineScope(Dispatchers.Main).launch {
            val currentUser = (application as App).currentUser
            if (currentUser == null) {
                startActivity(Intent(this@EditTasksActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                BottomNavigationHelper(this@EditTasksActivity, bottomNav, userRole).setup()
            }

            val taskId = intent.getStringExtra("TASK_ID")
            var task = taskRepository.getByIdRemote(taskId.toString())?.toLocal()
            if (task == null) {
                finish()
                return@launch
            }

            etTaskName.setText(task.name)
            etTaskDescription.setText(task.description)
            etHourlyRate.setText(task.hourlyRate?.toString() ?: "")

            val projects = projectRepository.getAllRemote()
            val projectNames = projects.map { it.name }
            val projectAdapter = ArrayAdapter(this@EditTasksActivity, android.R.layout.simple_spinner_item, projectNames)
            projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerProject.adapter = projectAdapter
            val projectIndex = projects.indexOfFirst { it.id == task.projectId }
            if (projectIndex >= 0) spinnerProject.setSelection(projectIndex)

            val statusList = listOf("pending", "in_progress", "completed", "archived")
            val statusAdapter = ArrayAdapter(this@EditTasksActivity, android.R.layout.simple_spinner_item, statusList)
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = statusAdapter
            val statusIndex = statusList.indexOf(task.status)
            if (statusIndex >= 0) spinnerStatus.setSelection(statusIndex)

            btnUpdateTask.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    var taskU = Task(
                        id = task.serverId.toString(),
                        serverId = task.serverId,
                        name = etTaskName.text.toString(),
                        description = etTaskDescription.text.toString(),
                        hourlyRate = etHourlyRate.text.toString().toFloat(),
                        projectId = projects[spinnerProject.selectedItemPosition].id.toString(),
                        status = statusList[spinnerStatus.selectedItemPosition]
                    )
                    Log.d("EditTasksActivity", "Updating task: ${taskU.projectId}")
                    taskRepository.update(taskU)
                    finish()
                }
            }
        }
    }
}