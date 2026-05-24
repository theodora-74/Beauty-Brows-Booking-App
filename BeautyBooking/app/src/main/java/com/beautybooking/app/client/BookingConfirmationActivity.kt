package com.beautybooking.app.client

import android.content.Intent
import android.os.Bundle
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.MainActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityBookingConfirmationBinding
import com.beautybooking.app.models.Appointment
import com.beautybooking.app.utils.Constants

class BookingConfirmationActivity : BaseActivity() {
    private lateinit var binding: ActivityBookingConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val appt = intent.getSerializableExtra(Constants.EXTRA_APPOINTMENT) as? Appointment
        appt?.let {
            binding.tvConfirmService.text = it.getDisplayService()
            binding.tvConfirmDateTime.text = "${it.getLocationDisplay()} \u2022 ${Constants.formatDateForDisplay(it.date)} - ${it.time}"
            binding.tvConfirmName.text = it.clientName
            binding.tvConfirmStatus.text = getString(R.string.pending_approval)
            if (it.bookingCode.isNotEmpty()) {
                binding.tvBookingCode.text = it.bookingCode
                binding.tvBookingCodeLabel.text = getString(R.string.your_booking_code)
            }
        }

        binding.root.post { binding.root.announceForAccessibility(getString(R.string.a11y_booking_success)) }

        binding.btnBackHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
