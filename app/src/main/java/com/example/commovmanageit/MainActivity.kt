package com.example.commovmanageit

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.commovmanageit.TestActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var currentLanguage = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLanguage()
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
            startActivity(Intent(this, TestActivity::class.java))
        }
    }

    private fun toggleLanguage() {
        currentLanguage = if (currentLanguage == "en") "pt" else "en"
        saveLanguage(currentLanguage)
        updateLanguage(currentLanguage)
        recreate()
    }

    private fun loadLanguage() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        currentLanguage = sharedPref.getString("language", "en") ?: "en"
        updateLanguage(currentLanguage)
    }

    private fun saveLanguage(lang: String) {
        getPreferences(Context.MODE_PRIVATE).edit {
            putString("language", lang)
        }
    }

    private fun updateLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}