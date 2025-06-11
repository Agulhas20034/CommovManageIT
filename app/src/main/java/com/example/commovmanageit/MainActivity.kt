package com.example.commovmanageit

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var currentLanguage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = (application as App).getSavedLanguage(this)
        (application as App).loadLanguage(this)
        setContentView(R.layout.activity_main)

        setupLanguageButton()
        setupStartButton()
    }

    private fun setupLanguageButton() {
        findViewById<ImageButton>(R.id.btnLanguage).setOnClickListener {
            toggleLanguage()
        }
    }

    private fun setupStartButton() {
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun toggleLanguage() {
        currentLanguage = if (currentLanguage == "en") "pt" else "en"
        (application as App).saveLanguage(this, currentLanguage)
        (application as App).updateLanguage(this, currentLanguage)
        recreate()
    }
}