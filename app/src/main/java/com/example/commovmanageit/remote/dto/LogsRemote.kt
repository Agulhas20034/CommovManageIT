package com.example.commovmanageit.remote.dto

import com.example.commovmanageit.db.entities.Logs
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogsRemote(
    @SerialName("id") val id: String,
    @SerialName("entity_id") val entity_id: String,
    @SerialName("entity_type") val entity_type: String,
    @SerialName("action") val action: String,
    @SerialName("user_id") val user_id: String?,
    @SerialName("old_values") val old_values: String?,
    @SerialName("new_values") val new_values: String?,
    @SerialName("created_at") val created_at: String
)

fun LogsRemote.toLocal() = Logs(
    id = id,
    entityId = entity_id,
    entityType = entity_type,
    action = action,
    userId = user_id,
    oldValues = old_values,
    newValues = new_values,
    createdAt = parseDateTimeString(created_at),
    isSynced = true,
    serverId = id
)

fun Logs.toRemote() = LogsRemote(
    id = id,
    entity_id = entityId,
    entity_type = entityType,
    action = action,
    user_id = userId,
    old_values = oldValues,
    new_values = newValues,
    created_at = createdAt.toString()
)

private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}