package com.beautybooking.app.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityAdminDaysBinding
import com.beautybooking.app.utils.Constants
import com.beautybooking.app.utils.LocalDataManager
import java.util.Calendar

class AdminDaysActivity : BaseActivity() {
    private lateinit var binding: ActivityAdminDaysBinding
    private lateinit var calAdapter: CalendarAdapter
    private var location = Constants.LOCATION_THESSALONIKI
    private var year = Calendar.getInstance().get(Calendar.YEAR)
    private var month = Calendar.getInstance().get(Calendar.MONTH)

    private val timeValues = (0..40).map { i ->
        val h = 8 + i / 2; val m = if (i % 2 == 0) "00" else "30"
        String.format("%02d:%s", h, m)
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDaysBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tabLocation.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                location = if (tab?.position == 0) Constants.LOCATION_THESSALONIKI else Constants.LOCATION_LITOCHORO
                updateGrid(); updateCount()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        binding.btnRecurring.setOnClickListener { showRecurringDialog() }

        setupCalendar(); updateCount()
    }

    private fun setupCalendar() {
        val days = listOf("S", "M", "T", "W", "T", "F", "S")
        binding.llDayHeaders.removeAllViews()
        for (d in days) {
            val tv = TextView(this); tv.text = d; tv.textSize = 12f; tv.setTextColor(getColor(R.color.text_hint))
            tv.gravity = Gravity.CENTER; tv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            binding.llDayHeaders.addView(tv)
        }
        calAdapter = CalendarAdapter(emptyList()) { day ->
            if (day.day > 0) {
                val dateStr = Constants.formatDateForStorage(day.year, day.month, day.day)
                if (LocalDataManager.isWorkingDay(this, location, dateStr)) showRemoveDialog(dateStr) else showHourPickerDialog(dateStr)
            }
        }
        binding.rvCalendar.layoutManager = GridLayoutManager(this, 7); binding.rvCalendar.adapter = calAdapter
        binding.tvMonth.text = Constants.getMonthYear(year, month)
        binding.btnPrevMonth.setOnClickListener { if (month == 0) { month = 11; year-- } else month--; binding.tvMonth.text = Constants.getMonthYear(year, month); updateGrid() }
        binding.btnNextMonth.setOnClickListener { if (month == 11) { month = 0; year++ } else month++; binding.tvMonth.text = Constants.getMonthYear(year, month); updateGrid() }
        updateGrid()
    }

    private fun showHourPickerDialog(date: String) {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(40, 40, 40, 20) }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        val npStart = NumberPicker(this).apply { minValue = 0; maxValue = timeValues.size - 1; displayedValues = timeValues; value = 2; wrapSelectorWheel = false }
        val npEnd = NumberPicker(this).apply { minValue = 0; maxValue = timeValues.size - 1; displayedValues = timeValues; value = 22; wrapSelectorWheel = false }
        row.addView(TextView(this).apply { text = "From "; textSize = 16f }); row.addView(npStart)
        row.addView(TextView(this).apply { text = "  To  "; textSize = 16f }); row.addView(npEnd)
        layout.addView(row)
        AlertDialog.Builder(this).setTitle(Constants.formatDateForDisplay(date))
            .setMessage(getString(R.string.set_working_hours)).setView(layout)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val s = timeValues[npStart.value]; val e = timeValues[npEnd.value]
                if (Constants.timeToMinutes(e) <= Constants.timeToMinutes(s)) Toast.makeText(this, getString(R.string.end_after_start), Toast.LENGTH_SHORT).show()
                else { LocalDataManager.setWorkingDay(this, location, date, s, e); updateGrid(); updateCount(); Toast.makeText(this, "$s - $e ✓", Toast.LENGTH_SHORT).show() }
            }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun showRemoveDialog(date: String) {
        val hours = LocalDataManager.getWorkingHours(this, location, date)
        val info = if (hours != null) "\n${hours.startHour} - ${hours.endHour}" else ""
        AlertDialog.Builder(this).setTitle(Constants.formatDateForDisplay(date)).setMessage(getString(R.string.day_already_set) + info)
            .setPositiveButton(getString(R.string.change_hours)) { _, _ -> showHourPickerDialog(date) }
            .setNegativeButton(getString(R.string.remove_day)) { _, _ ->
                LocalDataManager.removeWorkingDay(this, location, date); updateGrid(); updateCount()
                Toast.makeText(this, getString(R.string.day_removed), Toast.LENGTH_SHORT).show()
            }.setNeutralButton(getString(R.string.cancel), null).show()
    }

    private fun showRecurringDialog() {
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val existing = LocalDataManager.getRecurringRules(this, location)
        val existingDows = existing.map { it.dayOfWeek }.toSet()

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(60, 40, 60, 20) }

        val checkboxes = mutableListOf<CheckBox>()
        for (i in 1..7) {
            val cb = CheckBox(this).apply { text = dayNames[i - 1]; textSize = 16f; isChecked = i in existingDows }
            checkboxes.add(cb); layout.addView(cb)
        }

        layout.addView(TextView(this).apply { text = "\n${getString(R.string.working_hours_for_recurring)}"; textSize = 14f; setPadding(0, 20, 0, 10) })

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        val defaultStart = existing.firstOrNull()?.let { timeValues.indexOf(it.startHour).coerceAtLeast(0) } ?: 2
        val defaultEnd = existing.firstOrNull()?.let { timeValues.indexOf(it.endHour).coerceAtLeast(0) } ?: 22
        val npS = NumberPicker(this).apply { minValue = 0; maxValue = timeValues.size - 1; displayedValues = timeValues; value = defaultStart; wrapSelectorWheel = false }
        val npE = NumberPicker(this).apply { minValue = 0; maxValue = timeValues.size - 1; displayedValues = timeValues; value = defaultEnd; wrapSelectorWheel = false }
        row.addView(npS); row.addView(TextView(this).apply { text = " - "; textSize = 18f; gravity = Gravity.CENTER }); row.addView(npE)
        layout.addView(row)

        AlertDialog.Builder(this).setTitle(getString(R.string.recurring_schedule))
            .setView(layout)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val startH = timeValues[npS.value]; val endH = timeValues[npE.value]
                if (Constants.timeToMinutes(endH) <= Constants.timeToMinutes(startH)) {
                    Toast.makeText(this, getString(R.string.end_after_start), Toast.LENGTH_SHORT).show(); return@setPositiveButton
                }
                val rules = mutableListOf<LocalDataManager.RecurringRule>()
                for (i in checkboxes.indices) {
                    if (checkboxes[i].isChecked) rules.add(LocalDataManager.RecurringRule(i + 1, startH, endH))
                }
                LocalDataManager.setRecurringRules(this, location, rules)
                updateGrid(); updateCount()
                Toast.makeText(this, "${rules.size} ${getString(R.string.recurring_days_saved)}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun updateGrid() {
        val cal = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1) }
        val today = Calendar.getInstance()
        val first = cal.get(Calendar.DAY_OF_WEEK) - 1; val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val manualDays = LocalDataManager.getWorkingDays(this, location)

        val daysList = mutableListOf<CalendarDay>()
        for (i in 0 until first) daysList.add(CalendarDay(0, month, year))
        for (d in 1..max) {
            val ds = Constants.formatDateForStorage(year, month, d)
            val isT = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) == d
            val isWorking = ds in manualDays || LocalDataManager.isWorkingDayWithRecurring(this, location, ds)
            daysList.add(CalendarDay(d, month, year, isT, isWorking))
        }
        calAdapter.updateDays(daysList)
    }

    private fun updateCount() {
        val manualCount = LocalDataManager.getWorkingDays(this, location).size
        val recurringCount = LocalDataManager.getRecurringRules(this, location).size
        val locName = if (location == Constants.LOCATION_THESSALONIKI) getString(R.string.location_thessaloniki) else getString(R.string.location_litochoro)
        binding.tvSelectedCount.text = getString(R.string.working_days_count_full, locName, manualCount, recurringCount)
    }
}
