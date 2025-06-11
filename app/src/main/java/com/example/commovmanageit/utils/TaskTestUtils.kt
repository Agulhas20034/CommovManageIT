package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.Task
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.random.Random

object TaskTestUtils {
    fun generateTestTask(prefix: String = ""): Task {
        return Task(
            id = UUID.randomUUID().toString(),
            projectId = "1",
            name = "Task_$prefix",
            description = "Descrição de teste $prefix",
            hourlyRate = Random.nextFloat() * 100,
            status = "open",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printTask(task: Task, tag: String = "") {
        println("$tag Task: ${task.id}")
        println("ProjectID: ${task.projectId}")
        println("Name: ${task.name}")
        println("Description: ${task.description}")
        println("HourlyRate: ${task.hourlyRate}")
        println("Status: ${task.status}")
        println("CreatedAt: ${task.createdAt}")
        println("UpdatedAt: ${task.updatedAt}")
        println("DeletedAt: ${task.deletedAt}")
        println("Synced: ${task.isSynced}")
        println("ServerID: ${task.serverId}")
        println("---------------------")
    }

    suspend fun waitForSync(delay: Long = 2000) {
        delay(delay)
    }
}