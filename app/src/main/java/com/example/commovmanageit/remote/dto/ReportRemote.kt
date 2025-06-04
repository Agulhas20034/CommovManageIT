package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import com.example.commovmanageit.db.entities.Report
import kotlinx.serialization.Serializable

@Serializable
data class ReportRemote(
    val id: String,
    val user_id: String?,
    val project_id: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun ReportRemote.toLocal() = Report(
    id = id,
    userId = user_id,
    projectId = project_id,
    createdAt = Instant.parse(created_at).toEpochMilli(),
    updatedAt = Instant.parse(updated_at).toEpochMilli(),
    deletedAt = deleted_at?.let { Instant.parse(it).toEpochMilli() }
)

@RequiresApi(Build.VERSION_CODES.O)
fun Report.toRemote() = ReportRemote(
    id = id,
    user_id = userId,
    project_id = projectId,
    created_at = Instant.ofEpochMilli(createdAt).toString(),
    updated_at = Instant.ofEpochMilli(updatedAt).toString(),
    deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() }
)