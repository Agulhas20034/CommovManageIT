package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.TaskUser
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.random.Random

object TaskUserTestUtils {
    fun generateTestTaskUser(prefix: String = ""): TaskUser {
        return TaskUser(
            id = UUID.randomUUID().toString(),
            taskId = "task_$prefix",
            userId = "user_$prefix",
            startDate = Clock.System.now(),
            endDate = null,
            location = "Localização $prefix",
            conclusionRate = Random.nextFloat() * 100,
            timeUsed = Random.nextFloat() * 10,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printTaskUser(taskUser: TaskUser, tag: String = "") {
        println("$tag TaskUser: ${taskUser.id}")
        println("TaskID: ${taskUser.taskId}")
        println("UserID: ${taskUser.userId}")
        println("StartDate: ${taskUser.startDate}")
        println("EndDate: ${taskUser.endDate}")
        println("Location: ${taskUser.location}")
        println("ConclusionRate: ${taskUser.conclusionRate}")
        println("TimeUsed: ${taskUser.timeUsed}")
        println("CreatedAt: ${taskUser.createdAt}")
        println("UpdatedAt: ${taskUser.updatedAt}")
        println("DeletedAt: ${taskUser.deletedAt}")
        println("Synced: ${taskUser.isSynced}")
        println("ServerID: ${taskUser.serverId}")
        println("---------------------")
    }

    suspend fun waitForSync(delay: Long = 2000) {
        delay(delay)
    }
}