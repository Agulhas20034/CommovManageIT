package com.example.commovmanageit.remote.dto

import com.example.commovmanageit.db.entities.Role
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoleRemote(
    @SerialName("id") val id: String,
    @SerialName("permission_id") val permission_id: String,
    @SerialName("name") val name: String,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String?
)

fun RoleRemote.toLocal() = Role(
    id = id,
    permissionId = permission_id,
    name = name,
    createdAt = parseDateTimeString(created_at),
    updatedAt = parseDateTimeString(updated_at),
    deletedAt = deleted_at?.let { parseDateTimeString(it) }
)

fun Role.toRemote() = RoleRemote(
    id = id,
    permission_id = permissionId,
    name = name,
    created_at = createdAt.toString(),
    updated_at = updatedAt.toString(),
    deleted_at = deletedAt?.let { it.toString() }
)
private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}