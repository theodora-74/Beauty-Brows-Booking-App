package com.beautybooking.app

import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.beautybooking.app.admin.AdminLoginActivity
import com.beautybooking.app.client.CancelBookingActivity
import com.beautybooking.app.client.LocationSelectionActivity
import com.beautybooking.app.databinding.ActivityMainBinding
import com.beautybooking.app.utils.LocalDataManager
import com.beautybooking.app.utils.LocaleHelper

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateLanguageButton()
        updateThemeIcon()

        binding.logoArea.alpha = 0f; binding.logoArea.translationY = -40f
        binding.logoArea.animate().alpha(1f).translationY(0f).setDuration(800).setInterpolator(DecelerateInterpolator()).start()
        binding.buttonArea.alpha = 0f; binding.buttonArea.translationY = 60f
        binding.buttonArea.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(300).setInterpolator(DecelerateInterpolator()).start()

        binding.btnBookAppointment.setOnClickListener {
            startActivity(Intent(this, LocationSelectionActivity::class.java))
        }
        binding.btnCancelBooking.setOnClickListener {
            startActivity(Intent(this, CancelBookingActivity::class.java))
        }
        binding.btnAdminPanel.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
        binding.btnLanguage.setOnClickListener { showLanguagePicker() }
        binding.btnTheme.setOnClickListener { toggleTheme() }
    }

    private fun updateLanguageButton() {
        val lang = LocaleHelper.getCurrentLanguage(this)
        binding.btnLanguage.text = "\uD83C\uDF10 " + lang.code.uppercase()
        binding.btnLanguage.contentDescription = getString(R.string.a11y_language_button, lang.displayName)
    }

    private fun updateThemeIcon() {
        val isDark = LocalDataManager.isDarkMode(this)
        binding.btnTheme.setImageResource(if (isDark) R.drawable.ic_sun else R.drawable.ic_moon)
    }

    private fun toggleTheme() {
        val isDark = LocalDataManager.isDarkMode(this)
        LocalDataManager.setDarkMode(this, !isDark)
        AppCompatDelegate.setDefaultNightMode(
            if (!isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun showLanguagePicker() {
        val languages = LocaleHelper.SUPPORTED_LANGUAGES
        val names = languages.map { "${it.displayName}  (${it.englishName})" }.toTypedArray()
        val current = languages.indexOfFirst { it.code == LocaleHelper.getLanguage(this) }.coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_language))
            .setSingleChoiceItems(names, current) { dialog, which ->
                if (languages[which].code != LocaleHelper.getLanguage(this)) {
                    LocaleHelper.changeLanguage(this, languages[which].code)
                    dialog.dismiss(); recreate()
                } else dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null).show()
    }
}
