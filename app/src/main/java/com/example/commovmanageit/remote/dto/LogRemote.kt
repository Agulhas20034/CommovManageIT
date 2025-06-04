package com.example.commovmanageit.remote.dto

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
/*
@Serializable
data class LogsRemote(
    val id: String,
    val entity_id: String,
    val entity_type: String,
    val action: String,
    val user_id: String?,
    val old_values: String?,
    val new_values: String?,
    val created_at: String
)
@RequiresApi(Build.VERSION_CODES.O)
fun LogsRemote.toLocal() = Logs(
    id = id, // Server ID becomes local ID
    entityId = entity_id,
    entityType = entity_type,
    action = action,
    userId = user_id,
    oldValues = old_values,
    newValues = new_values,
    createdAt = Instant.parse(created_at).toEpochMilli(),
    isSynced = true,
    serverId = id
)

@RequiresApi(Build.VERSION_CODES.O)
fun Logs.toRemote() = LogsRemote(
    id = id,
    entity_id = entityId,
    entity_type = entityType,
    action = action,
    user_id = userId,
    old_values = oldValues,
    new_values = newValues,
    created_at = Instant.ofEpochMilli(createdAt).toString()
)*/