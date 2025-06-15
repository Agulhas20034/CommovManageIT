package com.example.commovmanageit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.db.entities.Project
import com.example.commovmanageit.db.entities.ProjectUser
import com.example.commovmanageit.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateProjectActivity : AppCompatActivity() {

    private val projectRepository by lazy { (application as App).projectRepository }
    private val userRepository by lazy { (application as App).userRepository }
    private val customerRepository by lazy { (application as App).customerRepository }
    private val projectusersRepository by lazy { (application as App).projectusersRepository }

    private var currentLanguage: String = "en"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.create_project)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.tvCurrentLanguage)
        val etProjectName = findViewById<EditText>(R.id.etProjectName)
        val etProjectDescription = findViewById<EditText>(R.id.etProjectDescription)
        val spinnerProjectManager = findViewById<Spinner>(R.id.spinnerProjectManager)
        val btnCreateProject = findViewById<Button>(R.id.btnCreateProject)
        val spinnerCustomer = findViewById<Spinner>(R.id.spinnerCustomer)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        tvCurrentLanguage.text = currentLanguage

        CoroutineScope(Dispatchers.Main).launch {
            val users = userRepository.getAllRemote()
            val customers = customerRepository.getAllRemote()
            val userNames = users.map { it.email }
            val customerNames = customers.map { it.name }
            val adapter = ArrayAdapter(this@CreateProjectActivity, android.R.layout.simple_spinner_item, userNames)
            val adapter2 = ArrayAdapter(this@CreateProjectActivity, android.R.layout.simple_spinner_item, customerNames)

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerProjectManager.adapter = adapter
            spinnerCustomer.adapter=adapter2
            val userRole = (application as App).currentUser?.roleId?.let { (application as App).roleRepository.getByIdRemote(it)?.name } ?: ""

            BottomNavigationHelper(this@CreateProjectActivity, bottomNav, userRole).setup()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }

        btnCreateProject.setOnClickListener {
            val name = etProjectName.text.toString().trim()
            val description = etProjectDescription.text.toString().trim()
            val managerPosition = spinnerProjectManager.selectedItemPosition
            val customerPosition = spinnerCustomer.selectedItemPosition
            val etHourlyRate = findViewById<EditText>(R.id.etHourlyRate)
            val etDailyWorkHours = findViewById<EditText>(R.id.etDailyWorkHours)

            if (spinnerProjectManager.adapter == null || spinnerProjectManager.adapter.count == 0 ||
                spinnerCustomer.adapter == null || spinnerCustomer.adapter.count == 0) {
                Toast.makeText(this, getString(R.string.no_items_in_spinners), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isEmpty() || description.isEmpty() || managerPosition == AdapterView.INVALID_POSITION ||
                spinnerCustomer.selectedItemPosition == AdapterView.INVALID_POSITION ||
                etHourlyRate.text.isNullOrEmpty() || etDailyWorkHours.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                val users = userRepository.getAllRemote()
                val customers = customerRepository.getAllRemote()
                val manager = users[managerPosition]
                val customer = customers[customerPosition]
                val project = Project(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    userId = manager.id,
                    customerId = customer.id,
                    hourlyRate = etHourlyRate.text.toString().toFloatOrNull() ?: 0f,
                    dailyWorkHours = etDailyWorkHours.text.toString().toIntOrNull() ?: 0,
                    description = description
                )
                projectRepository.insert(project)
                if((application as App).currentUser?.serverId != project.userId){
                    val projectUser = ProjectUser(
                        id = UUID.randomUUID().toString(),
                        projectId = project.id,
                        userId = project.userId.toString(),
                        inviterId = (application as App).currentUser?.id ?: "",
                        speed = 1,
                        quality = 1,
                        collaboration = 1,
                        status = "pending"
                    )
                    projectusersRepository.insert(projectUser)
                }
                val projectUser = ProjectUser(
                    id = UUID.randomUUID().toString(),
                    projectId = project.id,
                    userId = (application as App).currentUser?.id ?: "",
                    inviterId = (application as App).currentUser?.id ?: "",
                    speed = 1,
                    quality = 1,
                    collaboration = 1,
                    status = "pending"
                )
                projectusersRepository.insert(projectUser)
                Toast.makeText(this@CreateProjectActivity, getString(R.string.project_created), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@CreateProjectActivity, DashboardActivity::class.java))
                finish()
            }
        }



    }
}