package com.beautybooking.app.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.MainActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityAccessCodeBinding
import com.beautybooking.app.utils.LocalDataManager

class AccessCodeActivity : BaseActivity() {
    private lateinit var binding: ActivityAccessCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (LocalDataManager.isClientAuthenticated(this)) {
            goToMain(); return
        }

        binding = ActivityAccessCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnter.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (code.length != 4) {
                binding.tvError.text = getString(R.string.access_code_invalid)
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (LocalDataManager.verifyAccessCode(this, code)) {
                LocalDataManager.setClientAuthenticated(this, true)
                goToMain()
            } else {
                binding.tvError.text = getString(R.string.access_code_wrong)
                binding.tvError.visibility = View.VISIBLE
                binding.etCode.text?.clear()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
