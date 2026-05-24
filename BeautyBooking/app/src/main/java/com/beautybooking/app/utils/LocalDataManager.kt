package com.beautybooking.app.utils

import android.content.Context
import com.beautybooking.app.models.Appointment
import org.json.JSONArray
import org.json.JSONObject

object LocalDataManager {
    private const val PREFS = "beauty_booking_data"
    private const val KEY_APPOINTMENTS = "appointments"
    private const val KEY_HOURS_PREFIX = "working_hours_"
    private const val KEY_RECURRING_PREFIX = "recurring_"
    private const val KEY_ACCESS_CODE = "access_code"
    private const val KEY_CLIENT_AUTH = "client_authenticated"

    private const val ADMIN_USER = "admin"
    private const val ADMIN_PASS = "12345"
    private const val DEFAULT_ACCESS_CODE = "2026"

    fun authenticate(username: String, password: String): Boolean =
        username.trim() == ADMIN_USER && password.trim() == ADMIN_PASS

    // ── Client Access Code ──

    fun getAccessCode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_CODE, DEFAULT_ACCESS_CODE) ?: DEFAULT_ACCESS_CODE

    fun setAccessCode(context: Context, code: String) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACCESS_CODE, code).apply()

    fun verifyAccessCode(context: Context, code: String): Boolean =
        code == getAccessCode(context)

    fun isClientAuthenticated(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_CLIENT_AUTH, false)

    fun setClientAuthenticated(context: Context, auth: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_CLIENT_AUTH, auth).apply()

    // ── Working Days + Hours ──

    data class WorkingDay(val startHour: String, val endHour: String)

    fun getWorkingDaysMap(context: Context, location: String): Map<String, WorkingDay> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HOURS_PREFIX + location, "{}") ?: "{}"
        val obj = JSONObject(json)
        val map = mutableMapOf<String, WorkingDay>()
        for (key in obj.keys()) {
            val day = obj.getJSONObject(key)
            map[key] = WorkingDay(day.getString("start"), day.getString("end"))
        }
        return map
    }

    fun setWorkingDay(context: Context, location: String, date: String, startHour: String, endHour: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = KEY_HOURS_PREFIX + location
        val json = prefs.getString(key, "{}") ?: "{}"
        val obj = JSONObject(json)
        obj.put(date, JSONObject().apply { put("start", startHour); put("end", endHour) })
        prefs.edit().putString(key, obj.toString()).apply()
    }

    fun removeWorkingDay(context: Context, location: String, date: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = KEY_HOURS_PREFIX + location
        val json = prefs.getString(key, "{}") ?: "{}"
        val obj = JSONObject(json)
        obj.remove(date)
        prefs.edit().putString(key, obj.toString()).apply()
    }

    fun isWorkingDay(context: Context, location: String, date: String): Boolean =
        getWorkingDaysMap(context, location).containsKey(date)

    fun getWorkingDays(context: Context, location: String): Set<String> =
        getWorkingDaysMap(context, location).keys

    fun getWorkingHours(context: Context, location: String, date: String): WorkingDay? =
        getWorkingDaysMap(context, location)[date]

    // ── Recurring Days ──

    data class RecurringRule(val dayOfWeek: Int, val startHour: String, val endHour: String)

    fun getRecurringRules(context: Context, location: String): List<RecurringRule> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RECURRING_PREFIX + location, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<RecurringRule>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(RecurringRule(o.getInt("dow"), o.getString("start"), o.getString("end")))
        }
        return list
    }

    fun setRecurringRules(context: Context, location: String, rules: List<RecurringRule>) {
        val arr = JSONArray()
        for (r in rules) {
            arr.put(JSONObject().apply { put("dow", r.dayOfWeek); put("start", r.startHour); put("end", r.endHour) })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_RECURRING_PREFIX + location, arr.toString()).apply()
    }

    fun isWorkingDayWithRecurring(context: Context, location: String, date: String): Boolean {
        if (isWorkingDay(context, location, date)) return true
        val rules = getRecurringRules(context, location)
        if (rules.isEmpty()) return false
        val parts = date.split("-")
        val cal = java.util.Calendar.getInstance().apply { set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()) }
        return rules.any { it.dayOfWeek == cal.get(java.util.Calendar.DAY_OF_WEEK) }
    }

    fun getEffectiveWorkingHours(context: Context, location: String, date: String): WorkingDay? {
        val manual = getWorkingHours(context, location, date)
        if (manual != null) return manual
        val rules = getRecurringRules(context, location)
        if (rules.isEmpty()) return null
        val parts = date.split("-")
        val cal = java.util.Calendar.getInstance().apply { set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()) }
        val rule = rules.firstOrNull { it.dayOfWeek == cal.get(java.util.Calendar.DAY_OF_WEEK) } ?: return null
        return WorkingDay(rule.startHour, rule.endHour)
    }

    // ── Appointments ──

    fun addAppointment(context: Context, appointment: Appointment): String {
        val list = getAllAppointments(context).toMutableList()
        val id = "appt_${System.currentTimeMillis()}"
        val code = Constants.generateBookingCode()
        list.add(appointment.copy(id = id, bookingCode = code))
        saveAll(context, list)
        return id
    }

    fun getBookingCode(context: Context, appointmentId: String): String =
        getAllAppointments(context).firstOrNull { it.id == appointmentId }?.bookingCode ?: ""

    fun cancelByCode(context: Context, code: String): Boolean {
        val list = getAllAppointments(context).toMutableList()
        val index = list.indexOfFirst { it.bookingCode == code && it.status != Appointment.STATUS_CANCELLED && it.status != Appointment.STATUS_REJECTED }
        if (index < 0) return false
        list[index] = list[index].copy(status = Appointment.STATUS_CANCELLED)
        saveAll(context, list)
        return true
    }

    fun getAllAppointments(context: Context): List<Appointment> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_APPOINTMENTS, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<Appointment>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(Appointment(
                id = o.optString("id"), clientName = o.optString("clientName"),
                clientPhone = o.optString("clientPhone"), clientEmail = o.optString("clientEmail"),
                serviceType = o.optString("serviceType"), serviceKey = o.optString("serviceKey"),
                serviceSubType = o.optString("serviceSubType"), location = o.optString("location"),
                date = o.optString("date"), time = o.optString("time"),
                status = o.optString("status", Appointment.STATUS_PENDING),
                bookingCode = o.optString("bookingCode", ""),
                createdAt = o.optLong("createdAt", 0L)
            ))
        }
        return list
    }

    fun updateStatus(context: Context, appointmentId: String, newStatus: String) {
        val list = getAllAppointments(context).toMutableList()
        val index = list.indexOfFirst { it.id == appointmentId }
        if (index >= 0) { list[index] = list[index].copy(status = newStatus); saveAll(context, list) }
    }

    fun getAvailableTimeSlots(context: Context, date: String, location: String, serviceKey: String): List<String> {
        val hours = getEffectiveWorkingHours(context, location, date) ?: return emptyList()
        val startMin = Constants.timeToMinutes(hours.startHour)
        val endMin = Constants.timeToMinutes(hours.endHour)
        val allSlots = mutableListOf<String>()
        var m = startMin; while (m < endMin) { allSlots.add(Constants.minutesToTime(m)); m += 30 }
        val appointments = getAllAppointments(context)
            .filter { it.date == date && it.location == location && it.status != Appointment.STATUS_REJECTED && it.status != Appointment.STATUS_CANCELLED }
        if (appointments.isEmpty()) return allSlots
        val newDuration = Constants.getServiceDuration(serviceKey)
        return allSlots.filter { slot ->
            val slotStart = Constants.timeToMinutes(slot); val slotEnd = slotStart + newDuration
            if (slotEnd > endMin) return@filter false
            appointments.all { e ->
                val eStart = Constants.timeToMinutes(e.time); val eEnd = eStart + Constants.getServiceDuration(e.serviceKey)
                slotEnd <= eStart || slotStart >= eEnd
            }
        }
    }

    // ── Theme ──
    private const val KEY_DARK_MODE = "dark_mode"

    fun isDarkMode(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(context: Context, dark: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_DARK_MODE, dark).apply()

    private fun saveAll(context: Context, list: List<Appointment>) {
        val arr = JSONArray()
        for (a in list) {
            arr.put(JSONObject().apply {
                put("id", a.id); put("clientName", a.clientName); put("clientPhone", a.clientPhone)
                put("clientEmail", a.clientEmail); put("serviceType", a.serviceType)
                put("serviceKey", a.serviceKey); put("serviceSubType", a.serviceSubType)
                put("location", a.location); put("date", a.date); put("time", a.time)
                put("status", a.status); put("bookingCode", a.bookingCode); put("createdAt", a.createdAt)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_APPOINTMENTS, arr.toString()).apply()
    }
}
