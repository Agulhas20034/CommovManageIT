package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import com.example.commovmanageit.db.entities.ProjectUser
import kotlinx.serialization.Serializable

@Serializable
data class ProjectUserRemote(
    val id: String,
    val project_id: String,
    val user_id: String,
    val inviter_id: String?,
    val speed: Int?,
    val quality: Int?,
    val collaboration: Int?,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun ProjectUserRemote.toLocal() = ProjectUser(
    id = id,
    projectId = project_id,
    userId = user_id,
    inviterId = inviter_id,
    speed = speed,
    quality = quality,
    collaboration = collaboration,
    status = status,
    createdAt = Instant.parse(created_at).toEpochMilli(),
    updatedAt = Instant.parse(updated_at).toEpochMilli(),
    deletedAt = deleted_at?.let { Instant.parse(it).toEpochMilli() }
)

@RequiresApi(Build.VERSION_CODES.O)
fun ProjectUser.toRemote() = ProjectUserRemote(
    id = id,
    project_id = projectId,
    user_id = userId,
    inviter_id = inviterId,
    speed = speed,
    quality = quality,
    collaboration = collaboration,
    status = status,
    created_at = Instant.ofEpochMilli(createdAt).toString(),
    updated_at = Instant.ofEpochMilli(updatedAt).toString(),
    deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() }
)