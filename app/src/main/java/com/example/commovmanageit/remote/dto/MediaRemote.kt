package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import com.example.commovmanageit.db.entities.Media
import kotlinx.serialization.Serializable

@Serializable
data class MediaRemote(
    val id: String,
    val project_id: String?,
    val report_id: String?,
    val name: String,
    val type: String,
    val path: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun MediaRemote.toLocal() = Media(
    id = id,
    projectId = project_id,
    reportId = report_id,
    name = name,
    type = type,
    path = path,
    createdAt = Instant.parse(created_at).toEpochMilli(),
    updatedAt = Instant.parse(updated_at).toEpochMilli(),
    deletedAt = deleted_at?.let { Instant.parse(it).toEpochMilli() }
)

@RequiresApi(Build.VERSION_CODES.O)
fun Media.toRemote() = MediaRemote(
    id = id,
    project_id = projectId,
    report_id = reportId,
    name = name,
    type = type,
    path = path,
    created_at = Instant.ofEpochMilli(createdAt).toString(),
    updated_at = Instant.ofEpochMilli(updatedAt).toString(),
    deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() }
)