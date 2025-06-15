package com.example.commovmanageit
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.commovmanageit.utils.BottomNavigationHelper

class DashboardActivity : AppCompatActivity() {

    private val projectusersRepository by lazy { (application as App).projectusersRepository }
    private val projectRepository by lazy { (application as App).projectRepository }
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
        setContentView(R.layout.main_page)
        val btnLogout = findViewById<TextView>(R.id.btnLogout)
        val btnCreateProject = findViewById<TextView>(R.id.btnCreateProject).apply { visibility = View.GONE }
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.tvCurrentLanguage)
        val projectsContainer = findViewById<LinearLayout>(R.id.projectsContainer)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        tvCurrentLanguage.text = currentLanguage

        val currentUser = (application as App).currentUser
        CoroutineScope(Dispatchers.Main).launch {
            if (currentUser == null) {
                Log.e("DashboardActivity", "Current user is null, redirecting to LoginActivity")
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                runOnUiThread {
                    bottomNav.post {
                        BottomNavigationHelper(this@DashboardActivity, bottomNav, userRole).setup()
                    }
                }
            }
        }


        btnLogout.setOnClickListener {
            (application as App).logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnCreateProject.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, CreateProjectActivity::class.java))
        }

        btnLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }

        CoroutineScope(Dispatchers.Main).launch {
            if (currentUser == null) {
                Log.e("DashboardActivity", "Current user is null, redirecting to LoginActivity")
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                runOnUiThread {
                    bottomNav.post {
                        BottomNavigationHelper(this@DashboardActivity, bottomNav, userRole).setup()
                    }
                }
            }
            val isAdmin = userRole == "admin"
            val allProjects = projectusersRepository.getByUserIdRemote(currentUser.id)
            if (isAdmin) {
                val projects = allProjects
                projectsContainer.removeAllViews()
                val inflater = LayoutInflater.from(this@DashboardActivity)
                if (projects != null) {
                    for (project in projects) {
                        if (!isAdmin && project.user_id != currentUser.id) continue
                        val card =
                            inflater.inflate(R.layout.card_template, projectsContainer, false)
                        card.findViewById<TextView>(R.id.tvProjectName).text =
                            projectRepository.getByIdRemote(project.project_id)?.name
                        val taskCount =
                            taskuserRepository.getByUserIdRemote(currentUser.id ?: "")?.size ?: 0
                        android.util.Log.d("DashboardActivity", "taskCount: $taskCount")
                        card.findViewById<TextView>(R.id.tvTaskCount).text = "Tarefas: $taskCount"
                        card.setOnClickListener {
                            val intent =
                                Intent(this@DashboardActivity, ProjectsActivity::class.java)
                            intent.putExtra("projectId", project.id)
                            startActivity(intent)
                        }
                        projectsContainer.addView(card)
                    }
                }
            }else{
                val projects = allProjects?.filter { it.user_id == currentUser.id }
                projectsContainer.removeAllViews()
                val inflater = LayoutInflater.from(this@DashboardActivity)
                if (projects != null) {
                    for (project in projects) {
                        val card =
                            inflater.inflate(R.layout.card_template, projectsContainer, false)
                        card.findViewById<TextView>(R.id.tvProjectName).text =
                            projectRepository.getByIdRemote(project.project_id)?.name
                        val taskCount =
                            taskuserRepository.getByUserIdRemote(currentUser.id ?: "")?.size ?: 0
                        android.util.Log.d("DashboardActivity", "taskCount: $taskCount")
                        card.findViewById<TextView>(R.id.tvTaskCount).text = "Tarefas: $taskCount"
                        card.setOnClickListener {
                            val intent =
                                Intent(this@DashboardActivity, ProjectsActivity::class.java)
                            intent.putExtra("projectId", project.id)
                            startActivity(intent)
                        }
                        projectsContainer.addView(card)
                    }
                }
            }
            btnCreateProject.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }
    }
}
