package com.example.commovmanageit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.commovmanageit.RegisterActivity
import com.example.commovmanageit.db.entities.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.jvm.java

class CreateUserActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_user)

        val emailEdit = findViewById<TextInputEditText>(R.id.editEmail)
        val passwordEdit = findViewById<TextInputEditText>(R.id.editPassword)
        val workHoursEdit = findViewById<TextInputEditText>(R.id.editDailyWorkHours)
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            val workHours = workHoursEdit.text.toString()

            if (email.isBlank() || password.isBlank() || workHours.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val userRepository = (application as App).userRepository
                    val userExists = userRepository.getByemailRemote(email) != null
                    if (userExists) {
                        Toast.makeText(this@CreateUserActivity, getString(R.string.email_already_registered), Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@CreateUserActivity, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@CreateUserActivity, UsersActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}