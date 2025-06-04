package com.example.commovmanageit.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    var updatedAt: Date = Date(),
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Date? = null,
    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "server_id")
    val serverId: String? = null
)