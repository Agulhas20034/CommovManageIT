package com.example.commovmanageit.remote.dto

import com.example.commovmanageit.db.entities.ProjectUser
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectUserRemote(
    @SerialName("id") val id: String,
    @SerialName("project_id") val project_id: String,
    @SerialName("user_id") val user_id: String,
    @SerialName("inviter_id") val inviter_id: String?,
    @SerialName("speed") val speed: Int?,
    @SerialName("quality") val quality: Int?,
    @SerialName("collaboration") val collaboration: Int?,
    @SerialName("status") val status: String,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String?
)

fun ProjectUserRemote.toLocal() = ProjectUser(
    id = id,
    projectId = project_id,
    userId = user_id,
    inviterId = inviter_id,
    speed = speed,
    quality = quality,
    collaboration = collaboration,
    status = status,
    createdAt = parseDateTimeString(created_at),
    updatedAt = parseDateTimeString(updated_at),
    deletedAt = deleted_at?.let { parseDateTimeString(it) }
)

fun ProjectUser.toRemote() = ProjectUserRemote(
    id = id,
    project_id = projectId,
    user_id = userId,
    inviter_id = inviterId,
    speed = speed,
    quality = quality,
    collaboration = collaboration,
    status = status,
    created_at = createdAt.toString(),
    updated_at = updatedAt.toString(),
    deleted_at = deletedAt?.let { deletedAt.toString() }
)
private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}