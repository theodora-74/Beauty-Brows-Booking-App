package com.beautybooking.app.client

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityServiceSelectionBinding
import com.beautybooking.app.models.Service
import com.beautybooking.app.utils.Constants

class ServiceSelectionActivity : BaseActivity() {
    private lateinit var binding: ActivityServiceSelectionBinding
    private var selectedService: Service? = null
    private var location = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        location = intent.getStringExtra(Constants.EXTRA_LOCATION) ?: ""

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.navigationContentDescription = getString(R.string.a11y_back)

        val services = Service.getAllServices()
        binding.rvServices.layoutManager = GridLayoutManager(this, 2)
        binding.rvServices.adapter = ServiceAdapter(services) { service, _ ->
            selectedService = service
            binding.btnNext.isEnabled = true
        }

        binding.btnNext.setOnClickListener {
            selectedService?.let { s ->
                if (s.hasSubOptions) {
                    startActivity(Intent(this, NailsOptionsActivity::class.java).apply {
                        putExtra(Constants.EXTRA_LOCATION, location)
                    })
                } else {
                    startActivity(Intent(this, DateTimeActivity::class.java).apply {
                        putExtra(Constants.EXTRA_LOCATION, location)
                        putExtra(Constants.EXTRA_SERVICE_TYPE, s.getLocalizedName(this@ServiceSelectionActivity))
                        putExtra(Constants.EXTRA_SERVICE_KEY, s.key)
                        putExtra(Constants.EXTRA_SERVICE_SUB_TYPE, "")
                    })
                }
            }
        }
    }
}
