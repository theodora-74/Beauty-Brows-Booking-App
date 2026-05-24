package com.beautybooking.app.client

import android.content.Intent
import android.os.Bundle
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityLocationSelectionBinding
import com.beautybooking.app.utils.Constants

class LocationSelectionActivity : BaseActivity() {
    private lateinit var binding: ActivityLocationSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.navigationContentDescription = getString(R.string.a11y_back)

        binding.cardThessaloniki.setOnClickListener { goToServices(Constants.LOCATION_THESSALONIKI) }
        binding.cardLitochoro.setOnClickListener { goToServices(Constants.LOCATION_LITOCHORO) }
    }

    private fun goToServices(location: String) {
        startActivity(Intent(this, ServiceSelectionActivity::class.java).apply {
            putExtra(Constants.EXTRA_LOCATION, location)
        })
    }
}
