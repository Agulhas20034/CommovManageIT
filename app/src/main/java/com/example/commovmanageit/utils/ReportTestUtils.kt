package com.example.commovmanageit.utils

import com.example.commovmanageit.db.entities.Report
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import java.util.UUID

object ReportTestUtils {
    fun generateTestReport(prefix: String = ""): Report {
        return Report(
            id = UUID.randomUUID().toString(),
            userId = "user_$prefix",
            projectId = "project_$prefix",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printReport(report: Report, tag: String = "") {
        println("$tag Report: ${report.id}")
        println("UserID: ${report.userId}")
        println("ProjectID: ${report.projectId}")
        println("CreatedAt: ${report.createdAt}")
        println("UpdatedAt: ${report.updatedAt}")
        println("DeletedAt: ${report.deletedAt}")
        println("Synced: ${report.isSynced}")
        println("ServerID: ${report.serverId}")
        println("---------------------")
    }

    suspend fun waitForSync(delay: Long = 2000) {
        delay(delay)
    }
}
