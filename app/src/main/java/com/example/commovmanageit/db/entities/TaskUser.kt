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
    var taskId: String,
    @ColumnInfo(name = "user_id")
    var userId: String,
    @ColumnInfo(name = "start_date")
    var startDate: Instant?,
    @ColumnInfo(name = "end_date")
    var endDate: Instant?,
    @ColumnInfo(name = "location")
    var location: String?,
    @ColumnInfo(name = "conclusion_rate")
    var conclusionRate: Float?,
    @ColumnInfo(name = "time_used")
    var timeUsed: Float?,
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