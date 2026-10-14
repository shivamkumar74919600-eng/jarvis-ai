package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jarvis.assistant.device.DeviceManager
import com.jarvis.assistant.reports.ReportRepository
import com.jarvis.assistant.voice.VoiceController

class MainActivity : Activity() {

    private lateinit var voiceController: VoiceController
    private lateinit var deviceManager: DeviceManager
    private val reportRepository = ReportRepository()

    private lateinit var tvStatus: TextView
    private lateinit var tvResponse: TextView
    private lateinit var btnMic: Button
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvResponse = findViewById(R.id.tvResponse)
        btnMic = findViewById(R.id.btnMic)
        etInput = findViewById(R.id.etInput)
        btnSend = findViewById(R.id.btnSend)

        deviceManager = DeviceManager(this)

        // Request Permissions
        checkPermissions()

        // Init Voice Engine
        voiceController = VoiceController(
            context = this,
            onSpeechResult = { text -> processCommand(text) },
            onStatusChange = { status -> tvStatus.text = status }
        )

        btnMic.setOnClickListener {
            voiceController.startListening()
        }

        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                etInput.setText("")
                processCommand(text)
            }
        }

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "JARVIS Assistant ko enable karein", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 101)
        }
    }

    private fun processCommand(input: String) {
        val lower = input.lowercase()
        tvResponse.text = "Aapne kaha: \"$input\""

        when {
            // 1. WhatsApp Command
            lower.contains("whatsapp") || lower.contains("message bhejo") -> {
                val cleanMsg = input.replace("whatsapp", "", ignoreCase = true)
                    .replace("pe message bhejo", "", ignoreCase = true)
                    .trim().ifEmpty { "Hello from JARVIS!" }
                deviceManager.sendWhatsAppMessage(null, cleanMsg)
                val reply = "WhatsApp par message taiyar hai: $cleanMsg"
                tvResponse.text = reply
                voiceController.speak(reply)
            }

            // 2. Battery Status
            lower.contains("battery") -> {
                val battery = deviceManager.getBatteryLevel()
                val reply = "Aapke phone ki battery $battery percent hai."
                tvResponse.text = reply
                voiceController.speak(reply)
            }

            // 3. Reports
            lower.contains("report") && (lower.contains("dikhao") || lower.contains("batao")) -> {
                val reports = reportRepository.getAllReports()
                val reply = "Aapke paas ${reports.size} reports hain. Sabse naya: ${reports.firstOrNull()?.title}"
                tvResponse.text = reply
                voiceController.speak(reply)
            }

            lower.contains("report") && lower.contains("banao") -> {
                val title = input.replace("report", "", ignoreCase = true).replace("banao", "", ignoreCase = true).trim().ifEmpty { "New Voice Report" }
                reportRepository.addReport(title, "Work Project", "Voice report added: $input")
                val reply = "Naya report '$title' save ho gaya hai!"
                tvResponse.text = reply
                voiceController.speak(reply)
            }

            // 4. Flashlight
            lower.contains("torch") || lower.contains("flashlight") -> {
                val state = deviceManager.toggleFlashlight()
                val reply = if (state) "Torch on ho gayi hai." else "Torch band ho gayi hai."
                tvResponse.text = reply
                voiceController.speak(reply)
            }

            // 5. General Chat
            else -> {
                val reply = "Namaste! Main hoon Jarvis. Main aapka task process kar raha hoon: $input"
                tvResponse.text = reply
                voiceController.speak(reply)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceController.destroy()
    }
}
