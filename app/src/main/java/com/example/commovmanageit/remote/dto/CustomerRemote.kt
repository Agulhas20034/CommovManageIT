package com.example.commovmanageit.remote.dto

import com.example.commovmanageit.db.entities.Customer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import java.util.*
import kotlinx.serialization.SerialName

@Serializable
data class CustomerRemote(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone_number") val phone_number: String,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("deleted_at") val deleted_at: String? = null
)

fun CustomerRemote.toLocal() = Customer(
    id = UUID.randomUUID().toString(),
    serverId = id,
    name = name,
    email = email,
    phone_Number = phone_number,
    createdAt = parseDateTimeString(created_at),
    updatedAt = parseDateTimeString(updated_at),
    deletedAt = deleted_at?.let { parseDateTimeString(it) },
    isSynced = true,
)

fun Customer.toRemote() = CustomerRemote(
    id = serverId ?: id,
    name = name,
    email = email,
    phone_number = phone_Number,
    created_at = createdAt.toString(),
    updated_at = updatedAt.toString(),
    deleted_at = deletedAt?.toString()
)
private fun parseDateTimeString(dateTimeString: String): Instant {
    val localDateTime = LocalDateTime.parse(dateTimeString)
    return localDateTime.toInstant(TimeZone.UTC)
}