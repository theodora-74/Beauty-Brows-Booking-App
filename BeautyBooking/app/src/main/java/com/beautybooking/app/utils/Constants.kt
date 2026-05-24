package com.beautybooking.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Constants {
    const val EXTRA_SERVICE_TYPE = "service_type"
    const val EXTRA_SERVICE_KEY = "service_key"
    const val EXTRA_SERVICE_SUB_TYPE = "service_sub_type"
    const val EXTRA_DATE = "date"
    const val EXTRA_TIME = "time"
    const val EXTRA_APPOINTMENT = "appointment"
    const val EXTRA_LOCATION = "location"

    const val LOCATION_THESSALONIKI = "thessaloniki"
    const val LOCATION_LITOCHORO = "litochoro"

    val SERVICE_DURATIONS = mapOf(
        "haircut" to 60, "hair_coloring" to 120, "lash_lift" to 90,
        "brow_lift" to 60, "lash_extensions" to 150, "microblading" to 120,
        "lip_tattoo" to 120, "eyeliner_tattoo" to 90, "brow_clean" to 30, "nails" to 90
    )

    fun getServiceDuration(serviceKey: String): Int = SERVICE_DURATIONS[serviceKey] ?: 60

    fun getTimeSlots(): List<String> {
        val slots = mutableListOf<String>()
        for (h in 9..20) for (m in listOf(0, 30)) {
            if (h == 20 && m == 30) break
            slots.add(String.format("%02d:%02d", h, m))
        }
        return slots
    }

    fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun minutesToTime(minutes: Int): String = String.format("%02d:%02d", minutes / 60, minutes % 60)

    fun formatDateForDisplay(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return dateStr
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(date)
        } catch (_: Exception) { dateStr }
    }

    fun formatDateForStorage(year: Int, month: Int, day: Int): String =
        String.format("%04d-%02d-%02d", year, month + 1, day)

    fun getMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month) }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun generateBookingCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
