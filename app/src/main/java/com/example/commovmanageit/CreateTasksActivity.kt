package com.example.commovmanageit

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.db.entities.Project
import com.example.commovmanageit.db.entities.Task
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Types.NULL
import java.util.UUID

class CreateTasksActivity : AppCompatActivity() {

    private val projectRepository by lazy { (application as App).projectRepository }
    private val taskRepository by lazy { (application as App).taskRepository }
    private var currentLanguage: String = "en"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.create_task)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.CurrentLanguage)
        val etTaskName = findViewById<EditText>(R.id.etTaskName)
        val etHourly = findViewById<TextInputEditText>(R.id.etHourlyRate)
        val etTaskDescription = findViewById<EditText>(R.id.etTaskDescription)
        val spinnerProject = findViewById<Spinner>(R.id.spinnerproject)
        val spinnerStatus = findViewById<Spinner>(R.id.spinnerstatus)
        val btnCreate = findViewById<Button>(R.id.btnCreateTask)

        tvCurrentLanguage.text = if (currentLanguage == "en") "en" else "pt"

        btnBack.setOnClickListener { finish() }

        btnLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }

        CoroutineScope(Dispatchers.Main).launch {
            val isAdmin = (application as App).currentUser?.roleId?.let { (application as App).roleRepository.getByIdRemote(it)?.name } == "admin"
            var projects: List<Project>? = null
            if (isAdmin) {
                projects = projectRepository.getAllRemote().filter{it.id != "0" }
            } else {
                projects = projectRepository.getAllRemote().filter { it.userId == (application as App).currentUser?.id }
            }
            val projectNames = mutableListOf(getString(R.string.no_project))
            val projectIds = mutableListOf<String?>(null)
            projects.forEach {
                projectNames.add(it.name)
                projectIds.add(it.id)
            }
            val projectAdapter = ArrayAdapter(this@CreateTasksActivity, android.R.layout.simple_spinner_item, projectNames)
            projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerProject.adapter = projectAdapter

            val statusKeys = listOf("open", "in_progress", "completed", "archived")
            val statusLabels = listOf(
                getString(R.string.status_open),
                getString(R.string.status_in_progress),
                getString(R.string.status_completed),
                getString(R.string.status_archived)
            )
            val statusAdapter = ArrayAdapter(this@CreateTasksActivity, android.R.layout.simple_spinner_item, statusLabels)
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = statusAdapter

            btnCreate.setOnClickListener {
                val name = etTaskName.text.toString().trim()
                val description = etTaskDescription.text.toString().trim()
                val status = statusKeys[spinnerStatus.selectedItemPosition]
                val selectedProjectId = projectIds[spinnerProject.selectedItemPosition]

                if (name.isEmpty()) {
                    etTaskName.error = getString(R.string.required_field)
                    return@setOnClickListener
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val task = Task(
                        name = name,
                        description = description,
                        status = status,
                        id = UUID.randomUUID().toString(),
                        hourlyRate = etHourly.text.toString().toFloat(),
                    )
                    if (selectedProjectId != null) {
                        task.projectId = selectedProjectId
                    }else {
                        task.projectId = "0"
                    }

                    taskRepository.insert(task)
                    runOnUiThread { finish() }
                }
            }
        }
    }
}