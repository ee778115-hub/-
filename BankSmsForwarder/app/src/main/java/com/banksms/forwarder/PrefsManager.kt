package com.banksms.forwarder

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences("bank_sms_forwarder_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var bankSendersRaw: String
        get() = prefs.getString(KEY_BANK_SENDERS, "Alkuraimi, CACBank, الكريمي, النجم, الامتياز, حوالة") ?: ""
        set(value) = prefs.edit().putString(KEY_BANK_SENDERS, value).apply()

    var targetType: String
        get() = prefs.getString(KEY_TARGET_TYPE, TARGET_NUMBER) ?: TARGET_NUMBER
        set(value) = prefs.edit().putString(KEY_TARGET_TYPE, value).apply()

    var targetValue: String
        get() = prefs.getString(KEY_TARGET_VALUE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TARGET_VALUE, value.trim()).apply()

    var whatsappPackage: String
        get() = prefs.getString(KEY_WHATSAPP_PKG, PKG_WHATSAPP) ?: PKG_WHATSAPP
        set(value) = prefs.edit().putString(KEY_WHATSAPP_PKG, value).apply()

    var lastLog: String
        get() = prefs.getString(KEY_LAST_LOG, "جاهز لاستقبال رسائل البنوك.") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_LOG, value).apply()

    fun getBankFilters(): List<String> {
        return bankSendersRaw
            .split(",", "،", "\n")
            .map { normalizeText(it.trim()) }
            .filter { it.isNotEmpty() }
    }

    fun appendLog(entry: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastLog = "[$time] $entry"
    }

    companion object {
        const val TARGET_NUMBER = "NUMBER"
        const val TARGET_GROUP = "GROUP"
        const val PKG_WHATSAPP = "com.whatsapp"
        const val PKG_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

        private const val KEY_ENABLED = "enabled"
        private const val KEY_BANK_SENDERS = "bank_senders"
        private const val KEY_TARGET_TYPE = "target_type"
        private const val KEY_TARGET_VALUE = "target_value"
        private const val KEY_WHATSAPP_PKG = "whatsapp_pkg"
        private const val KEY_LAST_LOG = "last_log"

        fun normalizeText(input: String): String {
            return input.lowercase(Locale.ROOT)
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ة", "ه")
                .replace("ى", "ي")
                .trim()
        }
    }
}
