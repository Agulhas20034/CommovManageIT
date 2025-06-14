package com.example.commovmanageit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commovmanageit.ProfileActivity
import com.example.commovmanageit.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageButton
    private lateinit var btnLanguage: ImageButton
    private lateinit var tvCurrentLanguage: TextView
    private lateinit var logo: ImageView
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etDailyWorkHours: TextInputEditText
    private lateinit var btnSave: Button
    private var userRole: String = ""

    private var currentLanguage: String = "en"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.edit_profile)

        btnBack = findViewById(R.id.btnBack)
        btnLanguage = findViewById(R.id.btnLanguage)
        tvCurrentLanguage = findViewById(R.id.CurrentLanguage)
        logo = findViewById(R.id.logo)
        etEmail = findViewById(R.id.editEmail)
        etPassword = findViewById(R.id.editPassword)
        etDailyWorkHours = findViewById(R.id.editDailyWorkHours)
        btnSave = findViewById(R.id.btnSave)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        tvCurrentLanguage.text = currentLanguage

        val app = application as App
        val userRepository = app.userRepository
        val user = app.currentUser

        user?.let {
            etEmail.setText(it.email)
            etPassword.setText(it.password)
            etDailyWorkHours.setText(it.dailyWorkHours.toString())
        }

        CoroutineScope(Dispatchers.Main).launch {
            tvCurrentLanguage.text = currentLanguage




            userRole = kotlinx.coroutines.runBlocking {
                (application as App).roleRepository.getByIdRemote(user?.roleId ?: "")?.name ?: ""
            }
            if (userRole.isNotEmpty()) {
                bottomNav?.let {
                    BottomNavigationHelper(this@EditProfileActivity, it, userRole).setup()
                }
            }
        }
        btnBack.setOnClickListener { finish() }

        btnLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }

        btnSave.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val dailyWorkHours = etDailyWorkHours.text.toString().toIntOrNull() ?: 0

            if (email.isEmpty() || password.isEmpty() || dailyWorkHours <= 0) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val exists = userRepository.getByemailRemote(email)
                if (exists != null && exists.email != user?.email) {
                    Toast.makeText(this@EditProfileActivity, getString(R.string.email_already_registered), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                user?.let {
                    it.email = email
                    it.password = password
                    it.dailyWorkHours = dailyWorkHours
                    Log.d("EditProfileActivity", "Updating user: $it")
                    userRepository.update(it)
                    (application as App).currentUser = it
                    Toast.makeText(this@EditProfileActivity, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}