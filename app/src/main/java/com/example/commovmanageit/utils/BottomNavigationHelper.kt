package com.example.commovmanageit.utils

import android.app.Activity
import android.content.Intent
import androidx.annotation.MenuRes
import com.example.commovmanageit.CreateProjectActivity
import com.example.commovmanageit.DashboardActivity
import com.example.commovmanageit.ProfileActivity
import com.example.commovmanageit.ProjectsActivity
import com.example.commovmanageit.R
import com.example.commovmanageit.TasksActivity
import com.example.commovmanageit.UsersActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationHelper(
    private val activity: Activity,
    private val bottomNav: BottomNavigationView,
    private val currentUserRole: String
) {

    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_MANAGER = "manager"
        const val ROLE_USER = "user"

        @MenuRes
        fun getMenuForRole(role: String): Int {
            return when (role) {
                ROLE_ADMIN -> R.menu.bottom_nav_menu_a
                ROLE_MANAGER -> R.menu.bottom_nav_menu_g

                else -> R.menu.bottom_nav_menu_u
            }
        }
    }

    fun setup() {
        loadMenuForCurrentRole()
        setNavigationItemSelectedListener()
        highlightCurrentActivity()
    }

    private fun loadMenuForCurrentRole() {
        bottomNav.inflateMenu(getMenuForRole(currentUserRole))
    }

    private fun setNavigationItemSelectedListener() {
        bottomNav.setOnNavigationItemSelectedListener { item ->
            val targetActivity = getTargetActivityForItem(item.itemId)

            if (targetActivity != null && !activity.javaClass.equals(targetActivity)) {
                activity.startActivity(Intent(activity, targetActivity))
                activity.overridePendingTransition(0, 0) // Disable transition
                true
            } else {
                false
            }
        }
    }

    private fun getTargetActivityForItem(itemId: Int): Class<out Activity>? {
        return when (itemId) {
            R.id.nav_main -> DashboardActivity::class.java
            R.id.nav_create -> ProjectsActivity::class.java
            R.id.nav_profile -> ProfileActivity::class.java
            R.id.nav_tasks -> TasksActivity::class.java
            R.id.nav_users -> UsersActivity::class.java
            else -> null
        }
    }

    private fun highlightCurrentActivity() {
        val currentItemId = getCurrentNavItemId()
        if (currentItemId != -1) {
            bottomNav.selectedItemId = currentItemId
        }
    }

    private fun getCurrentNavItemId(): Int {
        return when (activity) {
            is DashboardActivity -> R.id.nav_main
            is ProjectsActivity,
            is CreateProjectActivity -> R.id.nav_create
            is ProfileActivity -> R.id.nav_profile
            is UsersActivity -> R.id.nav_users
            is TasksActivity -> R.id.nav_tasks
            else -> -1
        }
    }
}

