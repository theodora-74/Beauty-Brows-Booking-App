package com.beautybooking.app.models

import java.io.Serializable

data class Appointment(
    var id: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientEmail: String = "",
    val serviceType: String = "",
    val serviceKey: String = "",
    val serviceSubType: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = STATUS_PENDING,
    val bookingCode: String = "",
    val createdAt: Long = 0L
) : Serializable {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"
        const val STATUS_REJECTED = "rejected"
        const val STATUS_CANCELLED = "cancelled"
    }

    fun getDisplayService(): String =
        if (serviceSubType.isNotEmpty()) "$serviceType ($serviceSubType)" else serviceType

    fun getLocationDisplay(): String = when (location) {
        "thessaloniki" -> "Thessaloniki"
        "litochoro" -> "Litochoro"
        else -> location
    }
}
