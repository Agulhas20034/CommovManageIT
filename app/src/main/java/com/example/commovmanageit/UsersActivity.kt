package com.example.commovmanageit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat.startActivity
import com.example.commovmanageit.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.jvm.java

class UsersActivity : AppCompatActivity() {

    private val userRepository by lazy { (application as App).userRepository }
    private val roleRepository by lazy { (application as App).roleRepository }
    private var currentLanguage: String = "en"
    private lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.manage_users)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnAddUser = findViewById<Button>(R.id.btnAddUser)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.CurrentLanguage)
        val llUsersList = findViewById<LinearLayout>(R.id.llUsersList)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        tvCurrentLanguage.text = currentLanguage

        val currentUser = (application as App).currentUser

        CoroutineScope(Dispatchers.Main).launch {
            if (currentUser == null) {
                Log.e("ManageUsersActivity", "Current user is null, redirecting to LoginActivity")
                startActivity(Intent(this@UsersActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            userRole = roleRepository.getByIdRemote(currentUser.roleId)?.name.toString()
            if (userRole.isNotEmpty()) {
                runOnUiThread {
                    bottomNav.post {
                        BottomNavigationHelper(this@UsersActivity, bottomNav, userRole).setup()
                    }
                }
            }
            val isAdmin = userRole.equals("admin", ignoreCase = true)
            btnAddUser.visibility = if (isAdmin) View.VISIBLE else View.GONE

            val users = userRepository.getAllRemote().filter { it.id != currentUser.id }
            llUsersList.removeAllViews()
            val inflater = LayoutInflater.from(this@UsersActivity)
            users.forEach { user ->
                val userView = inflater.inflate(R.layout.card_template, llUsersList, false)
                userView.findViewById<TextView>(R.id.tvProjectName).text = user.email
                if (user.deletedAt != null) {
                    userView.findViewById<TextView>(R.id.tvTaskCount).text = "Deleted"
                } else {
                    userView.findViewById<TextView>(R.id.tvTaskCount).text = "Active"
                }
                userView.setOnClickListener {
                    val options = arrayOf(getString(R.string.Edit), getString(R.string.delete))
                    androidx.appcompat.app.AlertDialog.Builder(this@UsersActivity)
                        .setTitle(R.string.choose_option)
                        .setItems(options) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val intent = Intent(this@UsersActivity, EditUserActivity::class.java)
                                    intent.putExtra("userId", user.id)
                                    startActivity(intent)
                                }
                                1 -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        user.deletedAt = Clock.System.now()
                                        userRepository.update(user)
                                        recreate()
                                    }
                                }
                            }
                        }
                        .setNegativeButton(R.string.cancel_button, null)
                        .show()
                }
                llUsersList.addView(userView)
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnAddUser.setOnClickListener {
            startActivity(Intent(this@UsersActivity, CreateUserActivity::class.java))
        }

        btnLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }
    }
}