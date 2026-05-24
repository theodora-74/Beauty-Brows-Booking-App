package com.beautybooking.app.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.beautybooking.app.BaseActivity
import com.beautybooking.app.R
import com.beautybooking.app.databinding.ActivityAdminLoginBinding
import com.beautybooking.app.utils.LocalDataManager

class AdminLoginActivity : BaseActivity() {
    private lateinit var binding: ActivityAdminLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) { binding.tilEmail.error = getString(R.string.error_required); return@setOnClickListener }
            if (pass.isEmpty()) { binding.tilPassword.error = getString(R.string.error_required); return@setOnClickListener }
            binding.tilEmail.error = null
            binding.tilPassword.error = null

            binding.btnLogin.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.tvError.visibility = View.GONE

            if (LocalDataManager.authenticate(email, pass)) {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.tvError.text = getString(R.string.login_failed)
                binding.tvError.visibility = View.VISIBLE
            }
        }
    }
}
