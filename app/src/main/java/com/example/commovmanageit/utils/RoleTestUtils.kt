package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.Role
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import java.util.UUID

object RoleTestUtils {
    fun generateTestRole(prefix: String = ""): Role {
        return Role(
            id = UUID.randomUUID().toString(),
            permissionId = "perm_$prefix",
            name = "Role_$prefix",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printRole(role: Role, tag: String = "") {
        println("$tag Role: ${role.id}")
        println("PermissionID: ${role.permissionId}")
        println("Name: ${role.name}")
        println("CreatedAt: ${role.createdAt}")
        println("UpdatedAt: ${role.updatedAt}")
        println("DeletedAt: ${role.deletedAt}")
        println("Synced: ${role.isSynced}")
        println("ServerID: ${role.serverId}")
        println("---------------------")
    }

    suspend fun waitForSync(delay: Long = 2000) {
        delay(delay)
    }
}