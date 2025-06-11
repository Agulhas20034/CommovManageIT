package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.ProjectUser
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.random.Random

object ProjectUserTestUtils {
    fun generateTestProjectUser(
        prefix: String = "",
        projectId: String = "1",
        userId: String = "1",
        inviterId: String? = "1",
        speed: Int? = Random.nextInt(1, 10),
        quality: Int? = Random.nextInt(1, 10),
        collaboration: Int? = Random.nextInt(1, 10),
        status: String = "active"
    ): ProjectUser {
        return ProjectUser(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            userId = userId,
            inviterId = inviterId,
            speed = speed,
            quality = quality,
            collaboration = collaboration,
            status = status,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printProjectUser(projectUser: ProjectUser, tag: String = "") {
        println("$tag ProjectUser: ${projectUser.id}")
        println("ProjectId: ${projectUser.projectId}")
        println("UserId: ${projectUser.userId}")
        println("InviterId: ${projectUser.inviterId}")
        println("Speed: ${projectUser.speed}")
        println("Quality: ${projectUser.quality}")
        println("Collaboration: ${projectUser.collaboration}")
        println("Status: ${projectUser.status}")
        println("CreatedAt: ${projectUser.createdAt}")
        println("UpdatedAt: ${projectUser.updatedAt}")
        println("DeletedAt: ${projectUser.deletedAt}")
        println("IsSynced: ${projectUser.isSynced}")
        println("ServerId: ${projectUser.serverId}")
        println("---------------------")
    }
}