package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "media"
)
data class Media(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "project_id")
    val projectId: String?,
    @ColumnInfo(name = "report_id")
    val reportId: String?,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "path")
    val path: String,
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