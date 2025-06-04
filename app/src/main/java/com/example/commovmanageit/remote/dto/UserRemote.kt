package com.example.commovmanageit.remote.dto

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.commovmanageit.db.entities.User
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeParseException
@Serializable
data class UserRemote(
    val id: String,
    val role_id: String,
    val email: String,
    val password: String? = null,
    val daily_work_hours: Int,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val is_synced: Boolean? = null,
    val server_id: String? = null
)

data class CreateUserRequest(
    val email: String,
    val password: String,
    val role_id: String,
    val daily_work_hours: Int
)

data class UpdateUserRequest(
    val id: String,
    val email: String? = null,
    val password: String? = null,
    val role_id: String? = null,
    val daily_work_hours: Int? = null
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val user: UserRemote,
    val token: String
)

data class UserListResponse(
    val users: List<UserRemote>,
    val total: Int,
    val page: Int,
    val per_page: Int
)

// Conversion functions
@RequiresApi(Build.VERSION_CODES.O)
fun UserRemote.toLocal() = try {
    User(
        id = id,
        roleId = role_id,
        email = email,
        password = password ?: "",
        dailyWorkHours = daily_work_hours,
        createdAt = parseInstantSafe(created_at),
        updatedAt = parseInstantSafe(updated_at),
        deletedAt = deleted_at?.let { parseInstantSafe(it) },
        isSynced = true,
        serverId = server_id ?: id
    )
} catch (e: Exception) {
    throw DtoConversionException("Failed to convert UserRemote to local", e)
}

@RequiresApi(Build.VERSION_CODES.O)
fun User.toRemote() = try {
    UserRemote(
        id = serverId ?: id,
        role_id = roleId,
        email = email,
        password = if (password.isBlank()) null else password,
        daily_work_hours = dailyWorkHours,
        created_at = Instant.ofEpochMilli(createdAt).toString(),
        updated_at = Instant.ofEpochMilli(updatedAt).toString(),
        deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() }
    )
} catch (e: Exception) {
    throw DtoConversionException("Failed to convert User to remote", e)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseInstantSafe(timestamp: String): Long {
    return try {
        Instant.parse(timestamp).toEpochMilli()
    } catch (e: DateTimeParseException) {
        System.currentTimeMillis()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun List<UserRemote>.toLocalList() = mapNotNull { try {
    it.toLocal()
} catch (e: Exception) {
    null
}}

@RequiresApi(Build.VERSION_CODES.O)
fun List<User>.toRemoteList() = map { it.toRemote() }

class DtoConversionException(message: String, cause: Throwable? = null) :
    Exception(message, cause)