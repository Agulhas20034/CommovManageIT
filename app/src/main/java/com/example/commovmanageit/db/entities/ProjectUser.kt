package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "project_users"
)
data class ProjectUser(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "project_id")
    val projectId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "inviter_id")
    val inviterId: String?,
    @ColumnInfo(name = "speed")
    val speed: Int?,
    @ColumnInfo(name = "quality")
    val quality: Int?,
    @ColumnInfo(name = "collaboration")
    val collaboration: Int?,
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