package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks"
)
data class Task(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String?,
    @ColumnInfo(name = "project_id")
    val projectId: String,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "hourly_rate")
    val hourlyRate: Float?,
    @ColumnInfo(name = "start_date")
    val startDate: Long?,
    @ColumnInfo(name = "end_date")
    val endDate: Long?,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Long? = null,
    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "server_id")
    val serverId: String? = null
)