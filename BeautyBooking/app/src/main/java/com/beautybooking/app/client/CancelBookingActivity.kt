package com.beautybooking.app.client

import android.os.Bundle
import android.view.View
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityCancelBookingBinding
import com.beautybooking.app.utils.LocalDataManager

class CancelBookingActivity : BaseActivity() {
    private lateinit var binding: ActivityCancelBookingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancelBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnCancel.setOnClickListener {
            val code = binding.etCode.text.toString().trim().uppercase()
            if (code.length != 6) {
                binding.tilCode.error = getString(R.string.invalid_code)
                return@setOnClickListener
            }
            binding.tilCode.error = null
            val success = LocalDataManager.cancelByCode(this, code)
            binding.tvResult.visibility = View.VISIBLE
            if (success) {
                binding.tvResult.text = getString(R.string.booking_cancelled_success)
                binding.tvResult.setTextColor(getColor(R.color.status_approved))
                binding.btnCancel.isEnabled = false
            } else {
                binding.tvResult.text = getString(R.string.booking_not_found)
                binding.tvResult.setTextColor(getColor(R.color.status_rejected))
            }
        }
    }
}
