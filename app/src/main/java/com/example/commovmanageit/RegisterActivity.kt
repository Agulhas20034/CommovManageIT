package com.example.commovmanageit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.db.entities.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnLanguage: ImageButton
    private lateinit var tvCurrentLanguage: TextView
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var btnOnredirect: Button


    private var currentLanguage: String = "en"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.register)
        btnOnredirect = findViewById(R.id.tvLoginRedirect)
        btnBack = findViewById(R.id.btnBack)
        btnLanguage = findViewById(R.id.btnLanguage)
        tvCurrentLanguage = findViewById(R.id.CurrentLanguage)
        btnRegister = findViewById(R.id.btnRegister)
        etEmail = findViewById(R.id.Email)
        etPassword = findViewById(R.id.password)

        tvCurrentLanguage.text = currentLanguage

        btnBack.setOnClickListener {
            finish()
        }

        btnLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                val userRepository = (application as App).userRepository
                val userExists = userRepository.getByemailRemote(email) != null
                if (userExists) {
                    Toast.makeText(this@RegisterActivity, getString(R.string.email_already_registered), Toast.LENGTH_SHORT).show()
                } else {
                    val id = UUID.randomUUID().toString()
                    val user = User(
                        email = email, password = password,
                        roleId = "3",
                        dailyWorkHours = 0,
                        id =  id,
                        serverId = id,
                        isSynced = true
                    )
                    userRepository.insert(user)
                    Toast.makeText(this@RegisterActivity, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    fun onLoginRedirectClick(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}