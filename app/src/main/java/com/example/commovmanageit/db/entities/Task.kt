package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "tasks"
)
data class Task(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "project_id")
    var projectId: String? = null,
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "user_id")
    var userId: String? = null,
    @ColumnInfo(name = "description")
    var description: String,
    @ColumnInfo(name = "hourly_rate")
    var hourlyRate: Float?,
    @ColumnInfo(name = "status")
    var status: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now(),
    @ColumnInfo(name = "updated_at")
    var updatedAt: Instant = Clock.System.now(),
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Instant? = null,
    @ColumnInfo(name = "is_synced", defaultValue = "0")
    var isSynced: Boolean = false,
    @ColumnInfo(name = "server_id")
    var serverId: String? = null
)