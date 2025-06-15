package com.example.commovmanageit

import kotlin.jvm.java
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.db.entities.ProjectUser
import com.example.commovmanageit.db.entities.Task
import com.example.commovmanageit.db.entities.TaskUser
import com.example.commovmanageit.db.entities.User
import com.example.commovmanageit.remote.dto.TaskRemote
import com.example.commovmanageit.remote.dto.toLocal
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.commovmanageit.utils.BottomNavigationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ProjectsActivity : AppCompatActivity() {

    private val projectRepository by lazy { (application as App).projectRepository }
    private val projectusersRepository by lazy { (application as App).projectusersRepository }
    private val userRepository by lazy { (application as App).userRepository }
    private val taskRepository by lazy { (application as App).taskRepository }
    private val taskuserRepository by lazy { (application as App).taskuserRepository }
    private val roleRepository by lazy { (application as App).roleRepository }
    private var currentLanguage: String = "en"
    lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.manage_project)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.CurrentLanguage)
        val tvProjectName = findViewById<TextView>(R.id.ProjectName)
        val tvProjectDescription = findViewById<TextView>(R.id.ProjectDescription)
        val btnEditProject = findViewById<Button>(R.id.btnEditProject).apply { visibility = View.GONE }
        val btnDeleteProject = findViewById<Button>(R.id.btnDeleteProject).apply { visibility = View.GONE }
        val btnAddTask = findViewById<Button>(R.id.btnAddTask).apply { visibility = View.GONE }
        val btnAddUser = findViewById<Button>(R.id.btnAddUser).apply { visibility = View.GONE }
        val llUsersList = findViewById<LinearLayout>(R.id.llUsersList).apply { visibility = View.GONE }
        val tvUsers = findViewById<TextView>(R.id.tvUsers).apply { visibility = View.GONE }
        val llTasksList = findViewById<LinearLayout>(R.id.llTasksList)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        var projectId2: String? = null
        tvCurrentLanguage.text = currentLanguage

        val projectId = intent.getStringExtra("projectId") ?: return

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
                startActivity(Intent(this@ProjectsActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                runOnUiThread {
                    bottomNav.post {
                        BottomNavigationHelper(this@ProjectsActivity, bottomNav, userRole).setup()
                    }
                }
            }
            val isAdmin = userRole == "admin"
            val isManager = userRole == "manager"
            projectId2 = projectusersRepository.getByIdRemote(projectId)?.project_id ?: ""
            val projectusers = projectusersRepository.getByIdRemote(projectId)
            val tasks = taskRepository.getByProjectIdRemote(projectusers?.project_id ?: "")
            val users = userRepository.getByProjectIdRemote(projectusers?.project_id ?: "")
            val project = projectRepository.getByIdRemote(projectusers?.project_id ?: "")
            tvProjectName.text = project?.name ?: ""
            tvProjectDescription.text = project?.description ?: ""

            val inflater = LayoutInflater.from(this@ProjectsActivity)
            llUsersList.removeAllViews()
            users?.forEach { user ->
                val card = inflater.inflate(R.layout.card_template, llUsersList, false)
                card.findViewById<TextView>(R.id.tvProjectName).text = user.email
                card.findViewById<TextView>(R.id.tvTaskCount).text = "ID: ${user.id}"
                llUsersList.addView(card)
            }

            val inflaterTasks = LayoutInflater.from(this@ProjectsActivity)
            llTasksList.removeAllViews()
            tasks?.filter { task ->
                taskuserRepository.getByTaskIdRemote(task.id)?.any { it.user_id == currentUser.serverId } == true
            }?.forEach { task ->
                Log.d("ProjectsActivity", "Task ${task.name} is visible to user ${currentUser.email}")
                val card = inflaterTasks.inflate(R.layout.card_template, llTasksList, false)
                card.findViewById<TextView>(R.id.tvProjectName).text = task.name
                card.findViewById<TextView>(R.id.tvTaskCount).text = "ID: ${task.id}"
                card.findViewById<TextView>(R.id.status).text = task.status
                card.setOnClickListener {
                    val statuses = listOf("open", "in_progress", "completed", "archived")
                    val currentIndex = statuses.indexOf(task.status)
                    val builder = android.app.AlertDialog.Builder(this@ProjectsActivity)
                    builder.setTitle(getString(R.string.choose_status))
                    builder.setSingleChoiceItems(statuses.toTypedArray(), currentIndex) { dialog, which ->
                        val nextStatus = statuses[which]
                        CoroutineScope(Dispatchers.Main).launch {
                            val updatedTask = task.copy(status = nextStatus)
                            taskRepository.update(updatedTask.toLocal())
                            recreate()
                        }
                        dialog.dismiss()
                    }
                    builder.setNegativeButton(getString(R.string.cancel_button), null)
                    builder.show()
                }
                llTasksList.addView(card)
            }

            btnAddTask.visibility = if (isAdmin || isManager) View.VISIBLE else View.GONE
            btnEditProject.visibility = if (isAdmin) View.VISIBLE else View.GONE
            btnAddUser.visibility = if (isAdmin || isManager) View.VISIBLE else View.GONE
            btnDeleteProject.visibility = if (isAdmin) View.VISIBLE else View.GONE
            llUsersList.visibility = if (isAdmin || isManager) View.VISIBLE else View.GONE
            tvUsers.visibility = if (isAdmin || isManager) View.VISIBLE else View.GONE

        }

        btnEditProject.setOnClickListener {
            val intent = Intent(this, EditProjectActivity::class.java)
            intent.putExtra("projectId", projectId)
            startActivity(intent)
        }

        btnDeleteProject.setOnClickListener {
            val alertDialog = android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_message))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    CoroutineScope(Dispatchers.Main).launch {

                        val project = projectRepository.getByIdRemote(
                            projectusersRepository.getByIdRemote(projectId)?.project_id ?: ""
                        )?.toLocal()?.copy(deletedAt = Clock.System.now())
                        if(project!=null) {
                            projectRepository.update(project)
                        }
                        finish()
                    }
                }
                .setNegativeButton(getString(R.string.no), null)
                .create()
            alertDialog.show()
        }
        btnAddTask.setOnClickListener {
            val intent = Intent(this, AddUserToTaskActivity::class.java)
            intent.putExtra("projectId", projectId2)
            startActivity(intent)
        }

        btnAddUser.setOnClickListener {
            val intent = Intent(this, AddUserToProjectActivity::class.java)
            intent.putExtra("projectId", projectId2)
            startActivity(intent)
        }
    }

}
