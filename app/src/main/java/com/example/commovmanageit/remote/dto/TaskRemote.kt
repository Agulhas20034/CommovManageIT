package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import com.example.commovmanageit.db.entities.Task
import kotlinx.serialization.Serializable

@Serializable
data class TaskRemote(
    val id: String,
    val project_id: String,
    val name: String?,
    val description: String,
    val hourly_rate: Float?,
    val status: String,
    val userId:String?,
    val startDate:Long?,
    val endDate:Long?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun TaskRemote.toLocal() = Task(
    id = id,
    projectId = project_id,
    name = name,
    description = description,
    hourlyRate = hourly_rate,
    status = status,
    createdAt = Instant.parse(created_at).toEpochMilli(),
    updatedAt = Instant.parse(updated_at).toEpochMilli(),
    deletedAt = deleted_at?.let { Instant.parse(it).toEpochMilli() },
    userId = userId,
    startDate = startDate,
    endDate = endDate
)

@RequiresApi(Build.VERSION_CODES.O)
fun Task.toRemote() = TaskRemote(
    id = id,
    project_id = projectId,
    name = name,
    description = description,
    hourly_rate = hourlyRate,
    status = status,
    created_at = Instant.ofEpochMilli(createdAt).toString(),
    updated_at = Instant.ofEpochMilli(updatedAt).toString(),
    deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() },
    userId = userId,
    startDate = startDate,
    endDate = endDate

)