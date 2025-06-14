package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "projects"
)
data class Project(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    var userId: String?,
    @ColumnInfo(name = "customer_id")
    var customerId: String?,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "description")
    var description: String,
    @ColumnInfo(name = "hourly_rate")
    var hourlyRate: Float?,
    @ColumnInfo(name = "daily_work_hours")
    var dailyWorkHours: Int?,
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