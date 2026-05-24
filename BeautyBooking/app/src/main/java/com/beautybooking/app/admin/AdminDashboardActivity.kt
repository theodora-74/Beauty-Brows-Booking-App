package com.beautybooking.app.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.MainActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityAdminDashboardBinding
import com.beautybooking.app.models.Appointment
import com.beautybooking.app.utils.Constants
import com.beautybooking.app.utils.LocalDataManager
import java.util.Calendar

class AdminDashboardActivity : BaseActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private var all = mutableListOf<Appointment>()
    private lateinit var apptAdapter: AppointmentAdapter
    private lateinit var calAdapter: CalendarAdapter
    private var filter = 0
    private var year = Calendar.getInstance().get(Calendar.YEAR)
    private var month = Calendar.getInstance().get(Calendar.MONTH)
    private var selDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_manage_days -> { startActivity(Intent(this, AdminDaysActivity::class.java)); true }
                R.id.action_change_pin -> { showChangePinDialog(); true }
                R.id.action_toggle_calendar -> {
                    binding.cardCalendar.visibility = if (binding.cardCalendar.visibility == View.VISIBLE) View.GONE else View.VISIBLE; true
                }
                R.id.action_logout -> {
                    startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }); finish(); true
                }
                else -> false
            }
        }

        binding.tabStatus.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) { filter = tab?.position ?: 0; selDate = null; display() }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        setupCal(); setupList()
    }

    override fun onResume() { super.onResume(); load() }

    private fun setupCal() {
        val days = listOf("S", "M", "T", "W", "T", "F", "S")
        binding.llDayHeaders.removeAllViews()
        for (d in days) {
            val tv = TextView(this); tv.text = d; tv.textSize = 12f; tv.setTextColor(getColor(R.color.text_hint))
            tv.gravity = Gravity.CENTER; tv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            binding.llDayHeaders.addView(tv)
        }
        calAdapter = CalendarAdapter(emptyList()) { day -> selDate = Constants.formatDateForStorage(day.year, day.month, day.day); display() }
        binding.rvCalendar.layoutManager = GridLayoutManager(this, 7); binding.rvCalendar.adapter = calAdapter
        binding.tvMonth.text = Constants.getMonthYear(year, month)
        binding.btnPrevMonth.setOnClickListener { if (month == 0) { month = 11; year-- } else month--; selDate = null; binding.tvMonth.text = Constants.getMonthYear(year, month); updateGrid() }
        binding.btnNextMonth.setOnClickListener { if (month == 11) { month = 0; year++ } else month++; selDate = null; binding.tvMonth.text = Constants.getMonthYear(year, month); updateGrid() }
    }

    private fun updateGrid() {
        val cal = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1) }
        val today = Calendar.getInstance()
        val first = cal.get(Calendar.DAY_OF_WEEK) - 1; val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dates = all.filter { it.status != Appointment.STATUS_REJECTED && it.status != Appointment.STATUS_CANCELLED }.map { it.date }.toSet()
        val daysList = mutableListOf<CalendarDay>()
        for (i in 0 until first) daysList.add(CalendarDay(0, month, year))
        for (d in 1..max) {
            val ds = Constants.formatDateForStorage(year, month, d)
            val isT = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) == d
            daysList.add(CalendarDay(d, month, year, isT, ds in dates))
        }
        calAdapter.updateDays(daysList)
    }

    private fun setupList() {
        apptAdapter = AppointmentAdapter(mutableListOf(), { confirm(it, true) }, { confirm(it, false) })
        binding.rvAppointments.layoutManager = LinearLayoutManager(this); binding.rvAppointments.adapter = apptAdapter
    }

    private fun load() { all.clear(); all.addAll(LocalDataManager.getAllAppointments(this)); updateGrid(); display() }

    private fun display() {
        var list = when (filter) {
            0 -> all.filter { it.status == Appointment.STATUS_PENDING }
            1 -> all.filter { it.status == Appointment.STATUS_APPROVED }
            else -> all.toList()
        }
        if (selDate != null) list = list.filter { it.date == selDate }
        val sorted = list.sortedWith(compareBy(
            { when (it.status) { Appointment.STATUS_PENDING -> 0; Appointment.STATUS_APPROVED -> 1; else -> 2 } },
            { it.date }, { it.time }
        ))
        apptAdapter.updateData(sorted)
        binding.emptyState.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
        binding.rvAppointments.visibility = if (sorted.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun confirm(appt: Appointment, approve: Boolean) {
        val title = if (approve) getString(R.string.approve_title) else getString(R.string.reject_title)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("${appt.clientName}\n${appt.getDisplayService()}\n${appt.getLocationDisplay()} - ${Constants.formatDateForDisplay(appt.date)} ${appt.time}")
            .setPositiveButton(if (approve) getString(R.string.btn_approve) else getString(R.string.btn_reject)) { _, _ ->
                val status = if (approve) Appointment.STATUS_APPROVED else Appointment.STATUS_REJECTED
                LocalDataManager.updateStatus(this, appt.id, status)
                load()
                Toast.makeText(this, if (approve) getString(R.string.appointment_approved) else getString(R.string.appointment_rejected), Toast.LENGTH_SHORT).show()
                showNotifyDialog(appt, approve)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showNotifyDialog(appt: Appointment, approved: Boolean) {
        val statusText = if (approved) getString(R.string.status_approved_label) else getString(R.string.status_rejected_label)
        val msg = getString(R.string.notify_message, appt.clientName, appt.getDisplayService(),
            Constants.formatDateForDisplay(appt.date), appt.time, statusText)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.notify_client))
            .setMessage(getString(R.string.notify_client_question))
            .setPositiveButton("WhatsApp") { _, _ -> sendWhatsApp(appt.clientPhone, msg) }
            .setNegativeButton("SMS") { _, _ -> sendSMS(appt.clientPhone, msg) }
            .setNeutralButton(getString(R.string.skip), null)
            .show()
    }

    private fun sendWhatsApp(phone: String, message: String) {
        try {
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            val url = "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.whatsapp_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSMS(phone: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone"))
            intent.putExtra("sms_body", message)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.sms_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangePinDialog() {
        val currentCode = LocalDataManager.getAccessCode(this)
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(currentCode)
            textSize = 24f
            gravity = android.view.Gravity.CENTER
            maxLines = 1
            filters = arrayOf(android.text.InputFilter.LengthFilter(4))
            setPadding(40, 30, 40, 30)
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.change_access_code))
            .setMessage(getString(R.string.change_access_code_hint))
            .setView(input)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val newCode = input.text.toString().trim()
                if (newCode.length == 4) {
                    LocalDataManager.setAccessCode(this, newCode)
                    Toast.makeText(this, "${getString(R.string.access_code_changed)}: $newCode", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.access_code_invalid), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}

