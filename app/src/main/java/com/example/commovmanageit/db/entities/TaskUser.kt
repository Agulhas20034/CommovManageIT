package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "task_users"
)
data class TaskUser(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "task_id")
    val taskId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "start_date")
    var startDate: Instant?,
    @ColumnInfo(name = "end_date")
    var endDate: Instant?,
    @ColumnInfo(name = "location")
    val location: String?,
    @ColumnInfo(name = "conclusion_rate")
    val conclusionRate: Float?,
    @ColumnInfo(name = "time_used")
    val timeUsed: Float?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now(),
    @ColumnInfo(name = "updated_at")
    var updatedAt: Instant = Clock.System.now(),
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Instant? = null,
    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "server_id")
    val serverId: String? = null
)