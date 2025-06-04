package com.example.commovmanageit.remote.dto

import com.example.commovmanageit.db.entities.Customer
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class CustomerRemote(
    val id: String,
    val name: String,
    val email: String,
    val phone_number: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
) {
    companion object {
        val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}

fun CustomerRemote.toLocal() = Customer(
    id = UUID.randomUUID().toString(),
    serverId = id,
    name = name,
    email = email,
    phoneNumber = phone_number ?: "",
    createdAt = CustomerRemote.timestampFormat.parse(created_at) ?: Date(),
    updatedAt = CustomerRemote.timestampFormat.parse(updated_at) ?: Date(),
    deletedAt = deleted_at?.let { CustomerRemote.timestampFormat.parse(it) },
    isSynced = true
)

fun Customer.toRemote() = CustomerRemote(
    id = serverId ?: id,
    name = name,
    email = email,
    phone_number = phoneNumber,
    created_at = CustomerRemote.timestampFormat.format(createdAt),
    updated_at = CustomerRemote.timestampFormat.format(updatedAt),
    deleted_at = deletedAt?.let { CustomerRemote.timestampFormat.format(it) }
)