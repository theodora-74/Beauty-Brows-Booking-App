package com.beautybooking.app.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    private const val PREFS = "language_prefs"
    private const val KEY = "language"

    data class Language(val code: String, val displayName: String, val englishName: String)

    val SUPPORTED_LANGUAGES = listOf(
        Language("en", "English", "English"),
        Language("el", "Ελληνικά", "Greek"),
        Language("sq", "Shqip", "Albanian"),
        Language("ru", "Русский", "Russian")
    )

    fun getLanguage(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "en") ?: "en"

    fun getCurrentLanguage(context: Context): Language {
        val code = getLanguage(context)
        return SUPPORTED_LANGUAGES.firstOrNull { it.code == code } ?: SUPPORTED_LANGUAGES[0]
    }

    fun changeLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, languageCode).apply()
    }

    fun applyLocale(context: Context): Context {
        val lang = getLanguage(context)
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        return context.createConfigurationContext(config)
    }
}
