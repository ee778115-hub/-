package com.banksms.forwarder

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val task = WhatsAppForwarder.pendingForward ?: return

        if (System.currentTimeMillis() - task.createdAtMs > 20_000L) {
            WhatsAppForwarder.pendingForward = null
            return
        }

        val root = rootInActiveWindow ?: return
        val pkg = task.whatsappPackage

        if (task.targetType == PrefsManager.TARGET_NUMBER) {
            val sendBtn = findNodeById(root, "$pkg:id/send")
            if (sendBtn != null && sendBtn.isClickable) {
                sendBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                WhatsAppForwarder.pendingForward = null
                PrefsManager(this).appendLog("تم إرسال رسالة البنك بنجاح إلى رقم المحاسب.")
                handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_HOME) }, 800)
            }
            return
        }

        when (task.step) {
            0 -> {
                val searchBtn = findNodeById(root, "$pkg:id/menuitem_search")
                    ?: findNodeById(root, "$pkg:id/search_icon")
                if (searchBtn != null) {
                    clickNodeOrParent(searchBtn)
                    task.step = 1
                }
            }
            1 -> {
                val searchInput = findNodeById(root, "$pkg:id/search_src_text")
                    ?: findEditableNode(root)
                if (searchInput != null) {
                    setNodeText(searchInput, task.targetValue)
                    task.step = 2
                }
            }
            2 -> {
                val matches = root.findAccessibilityNodeInfosByText(task.targetValue)
                val targetChat = matches?.firstOrNull { node ->
                    node.className?.toString()?.contains("TextView") == true
                }
                if (targetChat != null) {
                    clickNodeOrParent(targetChat)
                    task.step = 3
                }
            }
            3 -> {
                val messageBox = findNodeById(root, "$pkg:id/entry")
                if (messageBox != null) {
                    setNodeText(messageBox, task.messageText)
                    task.step = 4
                    handler.postDelayed({
                        val refreshedRoot = rootInActiveWindow ?: return@postDelayed
                        val sendBtn = findNodeById(refreshedRoot, "$pkg:id/send")
                        if (sendBtn != null && sendBtn.isClickable) {
                            sendBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            WhatsAppForwarder.pendingForward = null
                            PrefsManager(this).appendLog("تم إرسال رسالة البنك بنجاح إلى المجموعة (${task.targetValue}).")
                            handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_HOME) }, 800)
                        }
                    }, 450)
                }
            }
        }
    }

    override fun onInterrupt() {}

    private fun findNodeById(root: AccessibilityNodeInfo, fullId: String): AccessibilityNodeInfo? {
        return root.findAccessibilityNodeInfosByViewId(fullId)?.firstOrNull()
    }

    private fun findEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val found = findEditableNode(node.getChild(i))
            if (found != null) return found
        }
        return null
    }

    private fun setNodeText(node: AccessibilityNodeInfo, text: String) {
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo) {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
            current = current.parent
        }
    }
}
