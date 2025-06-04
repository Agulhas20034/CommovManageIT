package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import com.example.commovmanageit.db.entities.Project
import kotlinx.serialization.Serializable

@Serializable
data class ProjectRemote(
    val id: String,
    val user_id: String?,
    val customer_id: String?,
    val name: String,
    val hourly_rate: Float?,
    val daily_work_hours: Int?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun ProjectRemote.toLocal() = Project(
    id = id,
    userId = user_id,
    customerId = customer_id,
    name = name,
    hourlyRate = hourly_rate,
    dailyWorkHours = daily_work_hours,
    createdAt = Instant.parse(created_at).toEpochMilli(),
    updatedAt = Instant.parse(updated_at).toEpochMilli(),
    deletedAt = deleted_at?.let { Instant.parse(it).toEpochMilli() }
)

@RequiresApi(Build.VERSION_CODES.O)
fun Project.toRemote() = ProjectRemote(
    id = id,
    user_id = userId,
    customer_id = customerId,
    name = name,
    hourly_rate = hourlyRate,
    daily_work_hours = dailyWorkHours,
    created_at = Instant.ofEpochMilli(createdAt).toString(),
    updated_at = Instant.ofEpochMilli(updatedAt).toString(),
    deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() }
)