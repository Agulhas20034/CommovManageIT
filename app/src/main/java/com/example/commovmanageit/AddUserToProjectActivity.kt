package com.example.commovmanageit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlin.collections.none
import kotlin.collections.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.db.entities.ProjectUser
import com.example.commovmanageit.db.entities.User
import com.example.commovmanageit.remote.dto.ProjectUserRemote
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.commovmanageit.utils.BottomNavigationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AddUserToProjectActivity : AppCompatActivity() {

    private val userRepository by lazy { (application as App).userRepository }
    private val projectusersRepository by lazy { (application as App).projectusersRepository }
    private val roleRepository by lazy { (application as App).roleRepository }
    private var currentLanguage: String = "en"
    private lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.add_user_to_project)

        val spinnerUsers = findViewById<Spinner>(R.id.spinnerUsers)
        val btnAddUser = findViewById<Button>(R.id.btnAddUser)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.tvCurrentLanguage)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

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
                startActivity(Intent(this@AddUserToProjectActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                runOnUiThread {
                    bottomNav.post {
                        BottomNavigationHelper(this@AddUserToProjectActivity, bottomNav, userRole).setup()
                    }
                }
            }

            val allUsers = userRepository.getAllRemote()
            val projectUsers: List<ProjectUserRemote> = projectusersRepository.getByProjectIdRemoteList(projectId) ?: emptyList()
            val projectUserIds: List<String> = projectUsers.map { it.user_id }
            val availableUsers = allUsers.filterNot { it.serverId in projectUserIds }
            val adapter = ArrayAdapter(this@AddUserToProjectActivity, android.R.layout.simple_spinner_item, availableUsers.map { it.email })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUsers.adapter = adapter

            btnAddUser.setOnClickListener {
                val selectedPosition = spinnerUsers.selectedItemPosition
                if (selectedPosition != AdapterView.INVALID_POSITION && availableUsers.isNotEmpty()) {
                    val selectedUser = availableUsers[selectedPosition]
                    CoroutineScope(Dispatchers.Main).launch {
                        val project = ProjectUser(
                            projectId = projectId,
                            userId = selectedUser.id,
                            id = UUID.randomUUID().toString(),
                            inviterId = currentUser.serverId ?: "",
                            status = "active",
                            speed = 0,
                            quality = 0,
                            collaboration = 0
                        )
                        projectusersRepository.insert(project)
                        Toast.makeText(this@AddUserToProjectActivity, getString(R.string.user_added_to_project), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }
}