package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant

import com.example.commovmanageit.db.entities.TaskUser
import kotlinx.serialization.Serializable

@Serializable
data class TaskUserRemote(
    val id: String,
    val task_id: String,
    val user_id: String,
    val start_date: String?,
    val end_date: String?,
    val location: String?,
    val conclusion_rate: Float?,
    val time_used: Float?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun TaskUserRemote.toLocal() = TaskUser(
    id = id,
    taskId = task_id,
    userId = user_id,
    startDate = start_date?.let { Instant.parse(it).toEpochMilli() },
    endDate = end_date?.let { Instant.parse(it).toEpochMilli() },
    location = location,
    conclusionRate = conclusion_rate,
    timeUsed = time_used,
    createdAt = Instant.parse(created_at).toEpochMilli(),
    updatedAt = Instant.parse(updated_at).toEpochMilli(),
    deletedAt = deleted_at?.let { Instant.parse(it).toEpochMilli() }
)

@RequiresApi(Build.VERSION_CODES.O)
fun TaskUser.toRemote() = TaskUserRemote(
    id = id,
    task_id = taskId,
    user_id = userId,
    start_date = startDate?.let { Instant.ofEpochMilli(it).toString() },
    end_date = endDate?.let { Instant.ofEpochMilli(it).toString() },
    location = location,
    conclusion_rate = conclusionRate,
    time_used = timeUsed,
    created_at = Instant.ofEpochMilli(createdAt).toString(),
    updated_at = Instant.ofEpochMilli(updatedAt).toString(),
    deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() }
)