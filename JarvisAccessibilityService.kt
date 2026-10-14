package com.jarvis.assistant.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        var instance: JarvisAccessibilityService? = null
        var pendingWhatsAppMessage: String? = null
        var shouldAutoSendWhatsApp: Boolean = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("JarvisAccessibility", "JARVIS Accessibility Service Connected!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        // WhatsApp Automation Logic
        if (packageName == "com.whatsapp" && shouldAutoSendWhatsApp && pendingWhatsAppMessage != null) {
            handleWhatsAppAutomation()
        }
    }

    private fun handleWhatsAppAutomation() {
        val rootNode = rootInActiveWindow ?: return
        try {
            // Find WhatsApp chat message input box (entry box)
            val inputNodes = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry")
            if (inputNodes.isNotEmpty()) {
                val inputNode = inputNodes[0]
                val arguments = Bundle()
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pendingWhatsAppMessage)
                inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                Thread.sleep(300)

                // Find WhatsApp Send Button and Click
                val sendNodes = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
                if (sendNodes.isNotEmpty()) {
                    sendNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    shouldAutoSendWhatsApp = false
                    pendingWhatsAppMessage = null
                    Log.d("JarvisAccessibility", "WhatsApp Message Auto-Sent Successfully!")
                }
            }
        } catch (e: Exception) {
            Log.e("JarvisAccessibility", "Error in WhatsApp automation: ${e.message}")
        }
    }

    // Phone Controls via Global Actions
    fun pressHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun pressBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun openNotifications() {
        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    fun openQuickSettings() {
        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }

    override fun onInterrupt() {
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
