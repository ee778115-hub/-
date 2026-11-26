package com.banksms.forwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = PrefsManager(context)
        if (!prefs.isEnabled) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Supports alphanumeric Sender IDs (bank names without phone numbers)
        val sender = messages[0].displayOriginatingAddress ?: "غير معروف"
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }

        val filters = prefs.getBankFilters()
        if (filters.isEmpty()) return

        val normalizedSender = PrefsManager.normalizeText(sender)
        val normalizedBody = PrefsManager.normalizeText(fullBody)

        val matched = filters.any { keyword ->
            normalizedSender.contains(keyword) || normalizedBody.contains(keyword)
        }

        if (matched) {
            WhatsAppForwarder.triggerForward(context.applicationContext, sender, fullBody)
        }
    }
}
