package com.example.commovmanageit.remote.dto

import com.example.commovmanageit.db.entities.User
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class UserRemote(
    @SerialName("id") val id: String,
    @SerialName("role_id") val role_id: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String? = null,
    @SerialName("daily_work_hours") val daily_work_hours: Int,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String?,
    @SerialName("is_synced") val is_synced: Boolean? = null,
    @SerialName("server_id") val server_id: String? = null
)

fun UserRemote.toLocal() = User(
        id = id,
        roleId = role_id,
        email = email,
        password = password ?: "",
        dailyWorkHours = daily_work_hours,
        createdAt = parseDateTimeString(created_at),
        updatedAt = parseDateTimeString(updated_at),
        deletedAt = deleted_at?.let { parseDateTimeString(it) },
        isSynced = true,
        serverId = server_id ?: id
    )

fun User.toRemote() = UserRemote(
        id = serverId ?: id,
        role_id = roleId,
        email = email,
        password = if (password.isBlank()) null else password,
        daily_work_hours = dailyWorkHours,
        created_at = createdAt.toString(),
        updated_at = updatedAt.toString(),
        deleted_at = deletedAt?.let { it.toString() }
    )

private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}