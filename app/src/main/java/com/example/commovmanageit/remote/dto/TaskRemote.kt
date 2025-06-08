package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.datetime.Instant
import com.example.commovmanageit.db.entities.Task
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskRemote(
    @SerialName("id") val id: String,
    @SerialName("project_id") val project_id: String,
    @SerialName("name") val name: String?,
    @SerialName("description") val description: String,
    @SerialName("hourly_rate") val hourly_rate: Float?,
    @SerialName("status") val status: String,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun TaskRemote.toLocal() = Task(
    id = id,
    projectId = project_id,
    name = name,
    description = description,
    hourlyRate = hourly_rate,
    status = status,
    createdAt = parseDateTimeString(created_at),
    updatedAt = parseDateTimeString(updated_at),
    deletedAt = deleted_at?.let { parseDateTimeString(it) }
)

@RequiresApi(Build.VERSION_CODES.O)
fun Task.toRemote() = TaskRemote(
    id = id,
    project_id = projectId,
    name = name,
    description = description,
    hourly_rate = hourlyRate,
    status = status,
    created_at = createdAt.toString(),
    updated_at = updatedAt.toString(),
    deleted_at = deletedAt?.let { it.toString() },
)

private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}