package com.example.commovmanageit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.jvm.java

class TasksActivity : AppCompatActivity() {

    private val taskRepository by lazy { (application as App).taskRepository }
    private val projectRepository by lazy { (application as App).projectRepository }
    private val userRepository by lazy { (application as App).userRepository }
    private val roleRepository by lazy { (application as App).roleRepository }
    private var currentLanguage: String = "en"
    lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.manage_tasks)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.CurrentLanguage)
        val tvProjectName = findViewById<TextView>(R.id.ProjectName)
        val btnCreateTask = findViewById<Button>(R.id.btnCreateTask)
        val llTasksList = findViewById<LinearLayout>(R.id.llTasksList)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

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
                startActivity(Intent(this@TasksActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                BottomNavigationHelper(this@TasksActivity, bottomNav, userRole).setup()
            }
            val isAdmin = userRole == "admin"

            val tasks = taskRepository.getAllRemote()
            val inflater = LayoutInflater.from(this@TasksActivity)
            llTasksList.removeAllViews()
            tasks.forEach { task ->
                val card = inflater.inflate(R.layout.card_template, llTasksList, false)
                card.findViewById<TextView>(R.id.tvProjectName).text = task.name
                if (task.projectId.toString() != "0") {
                    card.findViewById<TextView>(R.id.tvTaskCount).text =
                        getString(R.string.project_id) + " " + (projectRepository.getByIdRemote(task.projectId.toString())?.name
                            ?: getString(R.string.no_project))
                } else {
                    card.findViewById<TextView>(R.id.tvTaskCount).text = getString(R.string.no_project)
                }
                card.findViewById<TextView>(R.id.status).text = task.status

                card.setOnClickListener {
                    val popup = PopupMenu(this@TasksActivity, card)
                    popup.menu.add(getString(R.string.edit_task))
                    popup.menu.add(getString(R.string.delete_task))
                    popup.setOnMenuItemClickListener { item ->
                        when (item.title) {
                            getString(R.string.edit_task) -> {
                                val intent = Intent(this@TasksActivity, EditTasksActivity::class.java)
                                intent.putExtra("TASK_ID", task.id)
                                startActivity(intent)
                            }
                            getString(R.string.delete_task) -> {
                                CoroutineScope(Dispatchers.Main).launch {
                                    task.deletedAt = Clock.System.now()
                                    task.status = "archived"
                                    taskRepository.updateRemote(task)
                                    recreate()
                                }
                            }
                        }
                        true
                    }
                    popup.show()
                }

                llTasksList.addView(card)
            }

        }

        btnCreateTask.setOnClickListener {
            val intent = Intent(this, CreateTasksActivity::class.java)
            startActivity(intent)
        }
    }
}