package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.User
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.random.Random

object UserTestUtils {
    fun generateTestUser(prefix: String = ""): User {
        return User(
            id = UUID.randomUUID().toString(),
            roleId = "1",
            email = "user_$prefix@example.com",
            password = "senha_$prefix",
            dailyWorkHours = Random.nextInt(4, 10),
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printUser(user: User, tag: String = "") {
        println("$tag User: ${user.id}")
        println("RoleID: ${user.roleId}")
        println("Email: ${user.email}")
        println("Password: ${user.password}")
        println("DailyWorkHours: ${user.dailyWorkHours}")
        println("CreatedAt: ${user.createdAt}")
        println("UpdatedAt: ${user.updatedAt}")
        println("DeletedAt: ${user.deletedAt}")
        println("Synced: ${user.isSynced}")
        println("ServerID: ${user.serverId}")
        println("---------------------")
    }

    suspend fun waitForSync(delay: Long = 2000) {
        delay(delay)
    }
}