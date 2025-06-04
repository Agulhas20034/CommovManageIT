package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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