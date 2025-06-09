package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.Project
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.random.Random

object ProjectTestUtils {
    fun generateTestProject(
        prefix: String = "",
        userId: String? = "1",
        customerId: String? = "1",
        name: String = "TestProject_$prefix",
        hourlyRate: Float? = Random.nextFloat() * 100,
        dailyWorkHours: Int? = Random.nextInt(4, 10)
    ): Project {
        return Project(
            id = UUID.randomUUID().toString(),
            userId = userId,
            customerId = customerId,
            name = name,
            hourlyRate = hourlyRate,
            dailyWorkHours = dailyWorkHours,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printProject(project: Project, tag: String = "") {
        println("$tag Project: ${project.id}")
        println("UserId: ${project.userId}")
        println("CustomerId: ${project.customerId}")
        println("Name: ${project.name}")
        println("HourlyRate: ${project.hourlyRate}")
        println("DailyWorkHours: ${project.dailyWorkHours}")
        println("CreatedAt: ${project.createdAt}")
        println("UpdatedAt: ${project.updatedAt}")
        println("DeletedAt: ${project.deletedAt}")
        println("IsSynced: ${project.isSynced}")
        println("ServerId: ${project.serverId}")
        println("---------------------")
    }
}