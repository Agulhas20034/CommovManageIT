package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.Permission
import kotlinx.datetime.Clock
import java.util.UUID

object PermissionTestUtils {
    fun generateTestPermission(
        prefix: String = "",
        label: String = "TestPermission_$prefix"
    ): Permission {
        return Permission(
            id = UUID.randomUUID().toString(),
            label = label,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printPermission(permission: Permission, tag: String = "") {
        println("$tag Permission: ${permission.id}")
        println("Label: ${permission.label}")
        println("CreatedAt: ${permission.createdAt}")
        println("UpdatedAt: ${permission.updatedAt}")
        println("DeletedAt: ${permission.deletedAt}")
        println("IsSynced: ${permission.isSynced}")
        println("ServerId: ${permission.serverId}")
        println("---------------------")
    }
}