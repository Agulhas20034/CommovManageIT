package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.Logs
import kotlinx.datetime.Clock
import java.util.UUID

object LogsTestUtils {
    fun generateTestLog(
        prefix: String,
        entityId: String,
        entityType: String = "TestType",
        action: String = "CREATE"
    ): Logs {
        return Logs(
            id = UUID.randomUUID().toString(),
            entityId = entityId,
            entityType = entityType,
            action = action,
            userId = "user_$prefix",
            oldValues = "{\"field\":\"oldValue\"}",
            newValues = "{\"field\":\"newValue\"}",
            createdAt = Clock.System.now(),
            isSynced = false,
            serverId = null
        )
    }

    fun printLog(log: Logs, tag: String = "") {
        println("$tag Log: ${log.id}")
        println("EntityId: ${log.entityId}")
        println("EntityType: ${log.entityType}")
        println("Action: ${log.action}")
        println("UserId: ${log.userId}")
        println("OldValues: ${log.oldValues}")
        println("NewValues: ${log.newValues}")
        println("CreatedAt: ${log.createdAt}")
        println("IsSynced: ${log.isSynced}")
        println("ServerId: ${log.serverId}")
        println("---------------------")
    }
}