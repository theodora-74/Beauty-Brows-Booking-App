package com.beautybooking.app.client

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityClientInfoBinding
import com.beautybooking.app.models.Appointment
import com.beautybooking.app.utils.Constants
import com.beautybooking.app.utils.LocalDataManager

class ClientInfoActivity : BaseActivity() {
    private lateinit var binding: ActivityClientInfoBinding
    private var serviceType = ""; private var serviceKey = ""; private var serviceSubType = ""
    private var location = ""; private var date = ""; private var time = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        location = intent.getStringExtra(Constants.EXTRA_LOCATION) ?: ""
        serviceType = intent.getStringExtra(Constants.EXTRA_SERVICE_TYPE) ?: ""
        serviceKey = intent.getStringExtra(Constants.EXTRA_SERVICE_KEY) ?: ""
        serviceSubType = intent.getStringExtra(Constants.EXTRA_SERVICE_SUB_TYPE) ?: ""
        date = intent.getStringExtra(Constants.EXTRA_DATE) ?: ""
        time = intent.getStringExtra(Constants.EXTRA_TIME) ?: ""

        binding.toolbar.setNavigationOnClickListener { finish() }
        val svc = if (serviceSubType.isNotEmpty()) "$serviceType ($serviceSubType)" else serviceType
        binding.tvBookingSummaryService.text = svc
        binding.tvBookingSummaryDateTime.text = "${Constants.formatDateForDisplay(date)} - $time"
        binding.btnConfirm.setOnClickListener { if (validate()) submit() }
    }

    private fun validate(): Boolean {
        var ok = true
        val n = binding.etFullName.text.toString().trim()
        val p = binding.etPhone.text.toString().trim()
        val e = binding.etEmail.text.toString().trim()
        if (n.length < 2) { binding.tilFullName.error = getString(if (n.isEmpty()) R.string.error_required else R.string.error_invalid_name); ok = false } else binding.tilFullName.error = null
        if (p.length < 8) { binding.tilPhone.error = getString(if (p.isEmpty()) R.string.error_required else R.string.error_invalid_phone); ok = false } else binding.tilPhone.error = null
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) { binding.tilEmail.error = getString(if (e.isEmpty()) R.string.error_required else R.string.error_invalid_email); ok = false } else binding.tilEmail.error = null
        return ok
    }

    private fun submit() {
        binding.btnConfirm.isEnabled = false; binding.btnConfirm.text = getString(R.string.submitting)
        val appt = Appointment(
            clientName = binding.etFullName.text.toString().trim(),
            clientPhone = binding.etPhone.text.toString().trim(),
            clientEmail = binding.etEmail.text.toString().trim(),
            serviceType = serviceType, serviceKey = serviceKey, serviceSubType = serviceSubType,
            location = location, date = date, time = time,
            status = Appointment.STATUS_PENDING, createdAt = System.currentTimeMillis()
        )
        val id = LocalDataManager.addAppointment(this, appt)
        val code = LocalDataManager.getBookingCode(this, id)
        val saved = appt.copy(id = id, bookingCode = code)
        startActivity(Intent(this, BookingConfirmationActivity::class.java).apply {
            putExtra(Constants.EXTRA_APPOINTMENT, saved)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
