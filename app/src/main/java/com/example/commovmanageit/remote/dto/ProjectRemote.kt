package com.example.commovmanageit.remote.dto

import com.example.commovmanageit.db.entities.Project
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectRemote(
    @SerialName("id") val id: String,
    @SerialName("user_id") val user_id: String?,
    @SerialName("customer_id") val customer_id: String?,
    @SerialName("name") val name: String,
    @SerialName("hourly_rate") val hourly_rate: Float?,
    @SerialName("daily_work_hours") val daily_work_hours: Int?,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String?
)

fun ProjectRemote.toLocal() = Project(
    id = id,
    userId = user_id,
    customerId = customer_id,
    name = name,
    hourlyRate = hourly_rate,
    dailyWorkHours = daily_work_hours,
    createdAt = parseDateTimeString(created_at),
    updatedAt = parseDateTimeString(updated_at),
    deletedAt = deleted_at?.let { parseDateTimeString(it) }
)

fun Project.toRemote() = ProjectRemote(
    id = id,
    user_id = userId,
    customer_id = customerId,
    name = name,
    hourly_rate = hourlyRate,
    daily_work_hours = dailyWorkHours,
    created_at = createdAt.toString(),
    updated_at = updatedAt.toString(),
    deleted_at = deletedAt?.let { it.toString() }
)

private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}