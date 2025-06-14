package com.example.commovmanageit.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.commovmanageit.DashboardActivity
import com.example.commovmanageit.ProfileActivity
import com.example.commovmanageit.R
import com.example.commovmanageit.TasksActivity
import com.example.commovmanageit.UsersActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.jvm.java

class BottomNavigationHelper(
    private val activity: Activity,
    private val bottomNav: BottomNavigationView,
    private val currentUserRole: String
) {
    fun setup() {
        Log.d("BottomNavHelper", "Setting up BottomNavigationView for role: $currentUserRole")
        bottomNav.menu.clear()
        bottomNav.inflateMenu(
            when (currentUserRole) {
                "admin" -> R.menu.bottom_nav_menu_a
                "manager" -> R.menu.bottom_nav_menu_g
                else -> R.menu.bottom_nav_menu_u
            }
        )
        bottomNav.setOnItemSelectedListener { item ->
            val target = when (item.itemId) {
                R.id.nav_main -> DashboardActivity::class.java
                R.id.nav_profile -> ProfileActivity::class.java
                R.id.nav_tasks -> TasksActivity::class.java
                R.id.nav_users -> UsersActivity::class.java
                else -> null
            }
            if (target != null && activity.javaClass != target) {
                activity.startActivity(Intent(activity, target).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
                activity.overridePendingTransition(0, 0)
                true
            } else false
        }
        bottomNav.selectedItemId = when (activity) {
            is DashboardActivity -> R.id.nav_main
            is ProfileActivity -> R.id.nav_profile
            is UsersActivity -> R.id.nav_users
            is TasksActivity -> R.id.nav_tasks
            else -> -1
        }
    }
}