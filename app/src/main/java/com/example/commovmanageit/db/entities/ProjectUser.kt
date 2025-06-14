package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "project_users"
)
data class ProjectUser(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "project_id")
    var projectId: String,
    @ColumnInfo(name = "user_id")
    var userId: String,
    @ColumnInfo(name = "inviter_id")
    var inviterId: String?,
    @ColumnInfo(name = "speed")
    var speed: Int?,
    @ColumnInfo(name = "quality")
    var quality: Int?,
    @ColumnInfo(name = "collaboration")
    var collaboration: Int?,
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