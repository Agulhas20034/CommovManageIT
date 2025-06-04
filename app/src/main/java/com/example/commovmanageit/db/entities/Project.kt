package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "projects"
)
data class Project(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String?,
    @ColumnInfo(name = "customer_id")
    val customerId: String?,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "hourly_rate")
    val hourlyRate: Float?,
    @ColumnInfo(name = "daily_work_hours")
    val dailyWorkHours: Int?,
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