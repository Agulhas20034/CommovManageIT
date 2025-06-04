package com.example.commovmanageit.remote.dto
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import com.example.commovmanageit.db.entities.Role
import kotlinx.serialization.Serializable

@Serializable
data class RoleRemote(
    val id: String,
    val permission_id: String,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun RoleRemote.toLocal() = Role(
    id = id,
    permissionId = permission_id,
    name = name,
    createdAt = Instant.parse(created_at).toEpochMilli(),
    updatedAt = Instant.parse(updated_at).toEpochMilli(),
    deletedAt = deleted_at?.let { Instant.parse(it).toEpochMilli() }
)

fun Role.toRemote() = RoleRemote(
    id = id,
    permission_id = permissionId,
    name = name,
    created_at = Instant.ofEpochMilli(createdAt).toString(),
    updated_at = Instant.ofEpochMilli(updatedAt).toString(),
    deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() }
)