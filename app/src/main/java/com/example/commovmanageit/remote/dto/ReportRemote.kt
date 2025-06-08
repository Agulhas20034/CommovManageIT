package com.example.commovmanageit.remote.dto

import kotlinx.datetime.Instant
import com.example.commovmanageit.db.entities.Report
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportRemote(
    @SerialName("id") val id: String,
    @SerialName("user_id") val user_id: String?,
    @SerialName("project_id") val project_id: String,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String?
)

fun ReportRemote.toLocal() = Report(
    id = id,
    userId = user_id,
    projectId = project_id,
    createdAt = parseDateTimeString(created_at),
    updatedAt = parseDateTimeString(updated_at),
    deletedAt = deleted_at?.let { parseDateTimeString(deleted_at) }
)

fun Report.toRemote() = ReportRemote(
    id = id,
    user_id = userId,
    project_id = projectId,
    created_at = createdAt.toString(),
    updated_at = updatedAt.toString(),
    deleted_at = deletedAt?.let { it.toString() }
)
private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}