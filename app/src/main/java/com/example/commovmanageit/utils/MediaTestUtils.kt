package com.example.commovmanageit.utils

import java.util.UUID
import com.example.commovmanageit.db.entities.Media
import kotlinx.datetime.Clock

object MediaTestUtils {
    fun generateTestMedia(
        prefix: String = "",
        projectId: String? = "1",
        reportId: String? = "1",
        name: String = "TestMedia_$prefix",
        type: String = "image/png",
        path: String = "/tmp/testmedia_$prefix.png"
    ): Media {
        return Media(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            reportId = reportId,
            name = name,
            type = type,
            path = path,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            deletedAt = null,
            isSynced = false,
            serverId = null
        )
    }

    fun printMedia(media: Media, tag: String = "") {
        println("$tag Media: ${media.id}")
        println("ProjectId: ${media.projectId}")
        println("ReportId: ${media.reportId}")
        println("Name: ${media.name}")
        println("Type: ${media.type}")
        println("Path: ${media.path}")
        println("CreatedAt: ${media.createdAt}")
        println("UpdatedAt: ${media.updatedAt}")
        println("DeletedAt: ${media.deletedAt}")
        println("IsSynced: ${media.isSynced}")
        println("ServerId: ${media.serverId}")
        println("---------------------")
    }
}