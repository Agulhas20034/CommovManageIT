package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.datetime.Instant
import com.example.commovmanageit.db.entities.TaskUser
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskUserRemote(
    @SerialName("id") val id: String,
    @SerialName("task_id") val task_id: String,
    @SerialName("user_id") val user_id: String,
    @SerialName("start_date") val start_date: String?,
    @SerialName("end_date") val end_date: String?,
    @SerialName("location") val location: String?,
    @SerialName("conclusion_rate") val conclusion_rate: Float?,
    @SerialName("time_used") val time_used: Float?,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String?
)

fun TaskUserRemote.toLocal() = TaskUser(
    id = id,
    taskId = task_id,
    userId = user_id,
    startDate = start_date?.let { Instant.parse(it) },
    endDate = end_date?.let { Instant.parse(it) },
    location = location,
    conclusionRate = conclusion_rate,
    timeUsed = time_used,
    createdAt = parseDateTimeString(created_at),
    updatedAt = parseDateTimeString(updated_at),
    deletedAt = deleted_at?.let { parseDateTimeString(it) }
)

fun TaskUser.toRemote() = TaskUserRemote(
    id = id,
    task_id = taskId,
    user_id = userId,
    start_date = startDate?.let { it.toString() },
    end_date = endDate?.let { it.toString() },
    location = location,
    conclusion_rate = conclusionRate,
    time_used = timeUsed,
    created_at = createdAt.toString(),
    updated_at = updatedAt.toString(),
    deleted_at = deletedAt?.let { it.toString() }
)
private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}