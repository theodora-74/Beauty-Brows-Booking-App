package com.beautybooking.app.client

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.R
import com.beautybooking.app.admin.CalendarAdapter
import com.beautybooking.app.admin.CalendarDay
import com.beautybooking.app.databinding.ActivityDateTimeBinding
import com.beautybooking.app.utils.Constants
import com.beautybooking.app.utils.LocalDataManager
import java.util.Calendar

class DateTimeActivity : BaseActivity() {
    private lateinit var binding: ActivityDateTimeBinding
    private lateinit var calAdapter: CalendarAdapter

    private var serviceType = ""; private var serviceKey = ""; private var serviceSubType = ""; private var location = ""
    private var year = Calendar.getInstance().get(Calendar.YEAR)
    private var month = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedDate: String? = null; private var selectedTime: String? = null
    private var availableTimes = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        location = intent.getStringExtra(Constants.EXTRA_LOCATION) ?: ""
        serviceType = intent.getStringExtra(Constants.EXTRA_SERVICE_TYPE) ?: ""
        serviceKey = intent.getStringExtra(Constants.EXTRA_SERVICE_KEY) ?: ""
        serviceSubType = intent.getStringExtra(Constants.EXTRA_SERVICE_SUB_TYPE) ?: ""

        binding.toolbar.setNavigationOnClickListener { finish() }
        val display = if (serviceSubType.isNotEmpty()) "$serviceType ($serviceSubType)" else serviceType
        binding.tvServiceSummary.text = display

        setupCalendar(); setupTimePicker()

        binding.btnNext.setOnClickListener {
            if (selectedDate != null && selectedTime != null) {
                startActivity(Intent(this, ClientInfoActivity::class.java).apply {
                    putExtra(Constants.EXTRA_LOCATION, location); putExtra(Constants.EXTRA_SERVICE_TYPE, serviceType)
                    putExtra(Constants.EXTRA_SERVICE_KEY, serviceKey); putExtra(Constants.EXTRA_SERVICE_SUB_TYPE, serviceSubType)
                    putExtra(Constants.EXTRA_DATE, selectedDate); putExtra(Constants.EXTRA_TIME, selectedTime)
                })
            }
        }
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
            val dateStr = Constants.formatDateForStorage(day.year, day.month, day.day)
            if (LocalDataManager.isWorkingDayWithRecurring(this, location, dateStr)) {
                selectedDate = dateStr; selectedTime = null; binding.btnNext.isEnabled = false
                updateGrid(); loadAvailableTimes()
            }
        }
        binding.rvCalendar.layoutManager = GridLayoutManager(this, 7); binding.rvCalendar.adapter = calAdapter
        binding.tvMonth.text = Constants.getMonthYear(year, month)
        binding.btnPrevMonth.setOnClickListener { if (month == 0) { month = 11; year-- } else month--; binding.tvMonth.text = Constants.getMonthYear(year, month); updateGrid() }
        binding.btnNextMonth.setOnClickListener { if (month == 11) { month = 0; year++ } else month++; binding.tvMonth.text = Constants.getMonthYear(year, month); updateGrid() }
        updateGrid()
    }

    private fun updateGrid() {
        val cal = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1) }
        val today = Calendar.getInstance()
        val first = cal.get(Calendar.DAY_OF_WEEK) - 1; val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysList = mutableListOf<CalendarDay>()
        for (i in 0 until first) daysList.add(CalendarDay(0, month, year))
        for (d in 1..max) {
            val ds = Constants.formatDateForStorage(year, month, d)
            val isT = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) == d
            val isWorking = LocalDataManager.isWorkingDayWithRecurring(this, location, ds)
            val isPast = cal.clone().let { c -> (c as Calendar).set(Calendar.DAY_OF_MONTH, d); c.before(today) && !isT }
            daysList.add(CalendarDay(d, month, year, isT, isWorking && !isPast))
        }
        calAdapter.updateDays(daysList)
    }

    private fun setupTimePicker() {
        binding.npTimePicker.visibility = View.GONE; binding.tvNoSlots.visibility = View.VISIBLE
        binding.tvNoSlots.text = getString(R.string.select_date_first)
        binding.npTimePicker.wrapSelectorWheel = true
        binding.npTimePicker.setOnValueChangedListener { _, _, newVal ->
            if (availableTimes.isNotEmpty() && newVal < availableTimes.size) {
                selectedTime = availableTimes[newVal]; binding.btnNext.isEnabled = true
            }
        }
    }

    private fun loadAvailableTimes() {
        val date = selectedDate ?: return
        availableTimes = LocalDataManager.getAvailableTimeSlots(this, date, location, serviceKey)
        if (availableTimes.isEmpty()) {
            binding.npTimePicker.visibility = View.GONE; binding.tvNoSlots.visibility = View.VISIBLE
            binding.tvNoSlots.text = getString(R.string.no_slots_available)
        } else {
            binding.npTimePicker.visibility = View.VISIBLE; binding.tvNoSlots.visibility = View.GONE
            binding.npTimePicker.displayedValues = null
            binding.npTimePicker.minValue = 0; binding.npTimePicker.maxValue = availableTimes.size - 1
            binding.npTimePicker.displayedValues = availableTimes.toTypedArray(); binding.npTimePicker.value = 0
            selectedTime = availableTimes[0]; binding.btnNext.isEnabled = true
        }
    }
}
