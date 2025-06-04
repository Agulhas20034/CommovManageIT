package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permissions")
data class Permission(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "label")
    val label: String,
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