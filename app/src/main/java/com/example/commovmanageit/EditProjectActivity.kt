package com.example.commovmanageit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.db.entities.Project
import com.example.commovmanageit.db.entities.User
import com.example.commovmanageit.remote.dto.toLocal
import com.example.commovmanageit.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProjectActivity : AppCompatActivity() {

    private val projectRepository by lazy { (application as App).projectRepository }
    private val userRepository by lazy { (application as App).userRepository }
    private val roleRepository by lazy { (application as App).roleRepository }
    private var currentLanguage: String = "en"
    private var projectId: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.edit_project)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.tvCurrentLanguage)
        val etProjectName = findViewById<EditText>(R.id.etProjectName)
        val etProjectDescription = findViewById<EditText>(R.id.etProjectDescription)
        val spinnerProjectManager = findViewById<Spinner>(R.id.spinnerProjectManager)
        val btnCreateProject = findViewById<Button>(R.id.btnCreateProject)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val etHourlyRate = findViewById<EditText>(R.id.etHourlyRate)
        val etDailyWorkHours = findViewById<EditText>(R.id.etDailyWorkHours)

        tvCurrentLanguage.text = currentLanguage

        projectId = intent.getStringExtra("projectId")

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
                startActivity(Intent(this@EditProjectActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            val userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                runOnUiThread {
                    bottomNav.post {
                        BottomNavigationHelper(this@EditProjectActivity, bottomNav, userRole).setup()
                    }
                }
            }

            // Carregar dados do projeto
            val project = projectRepository.getByIdRemote(projectId ?: "")?.toLocal()
            etProjectName.setText(project?.name ?: "")
            etProjectDescription.setText(project?.description ?: "")
            etHourlyRate.setText(project?.hourlyRate?.toString() ?: "")
            etDailyWorkHours.setText(project?.dailyWorkHours?.toString() ?: "")

            // Carregar usuários para o spinner (apenas gerentes)
            val users = userRepository.getAllRemote()?.filter { it.roleId == "2" } ?: listOf()
            Toast.makeText(this@EditProjectActivity, "Usuários carregados: ${users.size}", Toast.LENGTH_SHORT).show()
            val adapter = ArrayAdapter(this@EditProjectActivity, android.R.layout.simple_spinner_item, users.map { it.email })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerProjectManager.adapter = adapter

            // Selecionar gerente atual
            val managerIndex = users.indexOfFirst { it.id == project?.userId }
            if (managerIndex >= 0) spinnerProjectManager.setSelection(managerIndex)

            btnCreateProject.setOnClickListener {
                val name = etProjectName.text.toString().trim()
                val description = etProjectDescription.text.toString().trim()
                val manager = users.getOrNull(spinnerProjectManager.selectedItemPosition)
                val hourlyRate = etHourlyRate.text.toString().toFloat()
                val dailyWorkHours = etDailyWorkHours.text.toString().toInt()
                if (name.isEmpty() || description.isEmpty() || manager == null || hourlyRate <= 0 || dailyWorkHours <= 0) {
                    Toast.makeText(this@EditProjectActivity, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val updatedProject = project?.copy(
                        name = name,
                        description = description,
                        userId = manager.id,
                        hourlyRate = hourlyRate,
                        dailyWorkHours = dailyWorkHours
                    )
                    if (updatedProject != null) {
                        projectRepository.update(updatedProject)
                        Toast.makeText(this@EditProjectActivity, getString(R.string.project_updated), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }
}