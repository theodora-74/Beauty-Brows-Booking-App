package com.beautybooking.app.client

import android.content.Intent
import android.os.Bundle
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityNailsOptionsBinding
import com.beautybooking.app.utils.Constants

class NailsOptionsActivity : BaseActivity() {
    private lateinit var binding: ActivityNailsOptionsBinding
    private var area: String? = null
    private var type: String? = null
    private var location = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNailsOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        location = intent.getStringExtra(Constants.EXTRA_LOCATION) ?: ""

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.navigationContentDescription = getString(R.string.a11y_back)

        binding.rgNailArea.setOnCheckedChangeListener { _, id ->
            area = when (id) { R.id.rbHands -> getString(R.string.nails_hands); R.id.rbFeet -> getString(R.string.nails_feet); else -> null }
            binding.btnNext.isEnabled = area != null && type != null
        }
        binding.rgNailType.setOnCheckedChangeListener { _, id ->
            type = when (id) { R.id.rbAcrygel -> getString(R.string.nails_acrygel); R.id.rbBasicColor -> getString(R.string.nails_basic_color); R.id.rbCleaning -> getString(R.string.nails_cleaning); else -> null }
            binding.btnNext.isEnabled = area != null && type != null
        }
        binding.btnNext.setOnClickListener {
            if (area != null && type != null) {
                startActivity(Intent(this, DateTimeActivity::class.java).apply {
                    putExtra(Constants.EXTRA_LOCATION, location)
                    putExtra(Constants.EXTRA_SERVICE_TYPE, getString(R.string.service_nails))
                    putExtra(Constants.EXTRA_SERVICE_KEY, "nails")
                    putExtra(Constants.EXTRA_SERVICE_SUB_TYPE, "$area - $type")
                })
            }
        }
    }
}
