package com.banksms.forwarder

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

object WhatsAppForwarder {

    data class PendingForward(
        val targetType: String,
        val targetValue: String,
        val messageText: String,
        val whatsappPackage: String,
        val createdAtMs: Long = System.currentTimeMillis(),
        var step: Int = 0
    )

    @Volatile
    var pendingForward: PendingForward? = null

    fun triggerForward(context: Context, sender: String, body: String) {
        val prefs = PrefsManager(context)
        val targetValue = prefs.targetValue
        if (targetValue.isEmpty()) {
            prefs.appendLog("لم يتم الإرسال: أدخل رقم المحاسب أو اسم المجموعة أولاً.")
            return
        }

        val formattedMessage = buildString {
            append("📩 رسالة بنكية جديدة\n")
            append("البنك / المرسل: ").append(sender).append("\n")
            append("━━━━━━━━━━\n")
            append(body)
        }

        val pkg = prefs.whatsappPackage
        val targetType = prefs.targetType

        pendingForward = PendingForward(
            targetType = targetType,
            targetValue = targetValue,
            messageText = formattedMessage,
            whatsappPackage = pkg
        )

        if (targetType == PrefsManager.TARGET_NUMBER) {
            var cleanPhone = targetValue.replace("+", "").replace(" ", "").replace("-", "")
            if (cleanPhone.startsWith("00")) cleanPhone = cleanPhone.substring(2)
            val encoded = URLEncoder.encode(formattedMessage, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            try {
                context.startActivity(intent)
                prefs.appendLog("جاري إرسال رسالة ($sender) إلى الرقم $cleanPhone")
            } catch (e: Exception) {
                pendingForward = null
                prefs.appendLog("تعذر فتح واتساب: تأكد من تثبيت النسخة المختارة.")
            }
        } else {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                prefs.appendLog("جاري إرسال رسالة ($sender) إلى المجموعة ($targetValue)")
            } else {
                pendingForward = null
                prefs.appendLog("تعذر فتح واتساب: النسخة المختارة غير مثبتة.")
            }
        }
    }
}
