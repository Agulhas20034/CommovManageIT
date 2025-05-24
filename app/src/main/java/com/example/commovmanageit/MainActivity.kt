package com.example.commovmanageit

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    private var currentLanguage = "en" // default language

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get current language from shared preferences
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        currentLanguage = sharedPref.getString("language", "en") ?: "en"

        // Set up language switcher button
        val btnLanguage = findViewById<ImageButton>(R.id.btnLanguage)
        btnLanguage.setOnClickListener {
            toggleLanguage()
        }

        // Set up start button
        val btnStart = findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun toggleLanguage() {
        currentLanguage = if (currentLanguage == "en") "pt" else "en"

        // Save language preference
        getPreferences(Context.MODE_PRIVATE).edit {
            putString("language", currentLanguage)
        }

        // Update app language
        updateLanguage(currentLanguage)

        // Restart activity to apply changes
        recreate()
    }

    private fun updateLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}