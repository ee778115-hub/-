package com.banksms.forwarder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var switchEnabled: SwitchMaterial
    private lateinit var etBankSenders: EditText
    private lateinit var rgTargetType: RadioGroup
    private lateinit var rbNumber: RadioButton
    private lateinit var rbGroup: RadioButton
    private lateinit var etTargetValue: EditText
    private lateinit var rgWhatsAppType: RadioGroup
    private lateinit var rbWhatsAppNormal: RadioButton
    private lateinit var rbWhatsAppBusiness: RadioButton
    private lateinit var tvStatusLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PrefsManager(this)

        switchEnabled = findViewById(R.id.switchEnabled)
        etBankSenders = findViewById(R.id.etBankSenders)
        rgTargetType = findViewById(R.id.rgTargetType)
        rbNumber = findViewById(R.id.rbNumber)
        rbGroup = findViewById(R.id.rbGroup)
        etTargetValue = findViewById(R.id.etTargetValue)
        rgWhatsAppType = findViewById(R.id.rgWhatsAppType)
        rbWhatsAppNormal = findViewById(R.id.rbWhatsAppNormal)
        rbWhatsAppBusiness = findViewById(R.id.rbWhatsAppBusiness)
        tvStatusLog = findViewById(R.id.tvStatusLog)

        loadSettings()

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveSettings()
            Toast.makeText(this, "تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnTest).setOnClickListener {
            saveSettings()
            WhatsAppForwarder.triggerForward(
                this,
                "Alkuraimi",
                "رسالة تجريبية: تم استلام حوالة بنكية بنجاح."
            )
            tvStatusLog.text = "السجل: ${prefs.lastLog}"
        }

        findViewById<Button>(R.id.btnSmsPermission).setOnClickListener {
            requestSmsPermissions()
        }

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(
                this,
                "فعّل خدمة: تحويل الرسائل البنكية إلى واتساب",
                Toast.LENGTH_LONG
            ).show()
        }

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(this, "فعّل الظهور في الأعلى لتطبيق تحويل الرسائل البنكية", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "صلاحية الظهور في الأعلى مفعلة بالفعل", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnBattery).setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        tvStatusLog.text = "السجل: ${prefs.lastLog}"
    }

    private fun loadSettings() {
        switchEnabled.isChecked = prefs.isEnabled
        etBankSenders.setText(prefs.bankSendersRaw)
        if (prefs.targetType == PrefsManager.TARGET_GROUP) {
            rbGroup.isChecked = true
        } else {
            rbNumber.isChecked = true
        }
        etTargetValue.setText(prefs.targetValue)
        if (prefs.whatsappPackage == PrefsManager.PKG_WHATSAPP_BUSINESS) {
            rbWhatsAppBusiness.isChecked = true
        } else {
            rbWhatsAppNormal.isChecked = true
        }
        tvStatusLog.text = "السجل: ${prefs.lastLog}"
    }

    private fun saveSettings() {
        prefs.isEnabled = switchEnabled.isChecked
        prefs.bankSendersRaw = etBankSenders.text.toString()
        prefs.targetType = if (rbGroup.isChecked) PrefsManager.TARGET_GROUP else PrefsManager.TARGET_NUMBER
        prefs.targetValue = etTargetValue.text.toString().trim()
        prefs.whatsappPackage = if (rbWhatsAppBusiness.isChecked) {
            PrefsManager.PKG_WHATSAPP_BUSINESS
        } else {
            PrefsManager.PKG_WHATSAPP
        }
    }

    private fun requestSmsPermissions() {
        val needed = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isEmpty()) {
            Toast.makeText(this, "صلاحية الرسائل ممنوحة بالفعل", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 101)
        }
    }
}
