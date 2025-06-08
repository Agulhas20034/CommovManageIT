package com.example.commovmanageit.remote.dto

import com.example.commovmanageit.db.entities.Media
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaRemote(
    @SerialName("id") val id: String,
    @SerialName("project_id") val project_id: String?,
    @SerialName("report_id") val report_id: String?,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("path") val path: String,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String?
)

fun MediaRemote.toLocal() = Media(
    id = id,
    projectId = project_id,
    reportId = report_id,
    name = name,
    type = type,
    path = path,
    createdAt = parseDateTimeString(created_at),
    updatedAt = parseDateTimeString(updated_at),
    deletedAt = deleted_at?.let { parseDateTimeString(it) }
)

fun Media.toRemote() = MediaRemote(
    id = id,
    project_id = projectId,
    report_id = reportId,
    name = name,
    type = type,
    path = path,
    created_at = createdAt.toString(),
    updated_at = updatedAt.toString(),
    deleted_at = deletedAt?.let { it.toString() }
)
private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}