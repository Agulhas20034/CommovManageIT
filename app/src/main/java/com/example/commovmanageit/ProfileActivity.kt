package com.example.commovmanageit
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.commovmanageit.utils.BottomNavigationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private var currentLanguage: String = "en"
    private lateinit var userRole: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.profile)
        val btnLogout = findViewById<TextView>(R.id.btnLogout)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnLogout2 = findViewById<Button>(R.id.btnLogout2)
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.CurrentLanguage)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val currentUser = (application as App).currentUser
        if (currentUser == null) {
            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            finish()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            tvCurrentLanguage.text = currentLanguage




            userRole = kotlinx.coroutines.runBlocking {
                (application as App).roleRepository.getByIdRemote(currentUser.roleId)?.name ?: ""
            }
            if (userRole.isNotEmpty()) {
                bottomNav?.let {
                    BottomNavigationHelper(this@ProfileActivity, it, userRole).setup()
                }
            }
        }
        btnLogout.setOnClickListener {
            (application as App).logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, EditProfileActivity::class.java))
            finish()
        }

        btnLogout2.setOnClickListener {
            (application as App).logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "en") "pt" else "en"
            (application as App).saveLanguage(this, currentLanguage)
            (application as App).updateLanguage(this, currentLanguage)
            recreate()
        }
    }
}