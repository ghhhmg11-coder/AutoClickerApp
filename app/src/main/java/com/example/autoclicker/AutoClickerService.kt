package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class AutoClickerService : AccessibilityService() {

    companion object {
        const val TAG = "AutoClickerService"
        const val ACTION_SERVICE_CONNECTED    = "com.example.autoclicker.SERVICE_CONNECTED"
        const val ACTION_SERVICE_DISCONNECTED = "com.example.autoclicker.SERVICE_DISCONNECTED"

        var isRunning = false
            private set

        val targetKeywords = mutableListOf(
            "قبول العرض",
            "قبول",
            "يأتي",
            "مشوار",
            "SAR"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.d(TAG, "Accessibility Service Connected")
        sendBroadcast(Intent(ACTION_SERVICE_CONNECTED))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        checkAndClick(rootNode, targetKeywords)
    }

    private fun checkAndClick(node: AccessibilityNodeInfo, keywords: List<String>) {
        val nodeText = node.text?.toString()

        if (!nodeText.isNullOrBlank()) {
            for (keyword in keywords) {
                if (nodeText.contains(keyword, ignoreCase = true)) {
                    Log.d(TAG, "Keyword detected: \"$keyword\" in \"$nodeText\"")

                    if (node.isClickable) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Log.d(TAG, "Clicked node directly")
                        return
                    }

                    var parent = node.parent
                    while (parent != null) {
                        if (parent.isClickable) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Log.d(TAG, "Clicked parent node")
                            return
                        }
                        parent = parent.parent
                    }
                }
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            checkAndClick(child, keywords)
        }
    }

    override fun onInterrupt() {
        Log.e(TAG, "Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d(TAG, "Service Destroyed")
        sendBroadcast(Intent(ACTION_SERVICE_DISCONNECTED))
    }
}
