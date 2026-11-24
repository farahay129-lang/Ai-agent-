package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "JarvisA11yService"

class HandsFreeAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceActive.value = true
        activeServiceInstance = this
        Log.d(TAG, "JARVIS Accessibility Service connected and ready.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        _lastActivePackage.value = packageName
        refreshScreenSnapshot()
    }

    override fun onInterrupt() {
        Log.w(TAG, "JARVIS Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceActive.value = false
        if (activeServiceInstance == this) {
            activeServiceInstance = null
        }
    }

    private fun refreshScreenSnapshot() {
        val root = rootInActiveWindow ?: return
        val buttons = mutableListOf<String>()
        val texts = mutableListOf<String>()
        collectSafeNodes(root, buttons, texts, depth = 0)
        _visibleButtons.value = buttons.distinct().take(15)
        _visibleScreenText.value = texts.distinct().take(20).joinToString(" | ")
    }

    private fun collectSafeNodes(
        node: AccessibilityNodeInfo?,
        buttons: MutableList<String>,
        texts: MutableList<String>,
        depth: Int
    ) {
        if (node == null || depth > 14) return

        // SECURITY: Never read password fields or sensitive PIN/OTP nodes
        if (node.isPassword) return

        val label = node.text?.toString()?.trim()
            ?: node.contentDescription?.toString()?.trim()

        if (!label.isNullOrBlank() && label.length <= 120) {
            val sanitized = redactSensitiveTokens(label)
            if (node.isClickable) {
                buttons.add(sanitized)
            } else {
                texts.add(sanitized)
            }
        }

        for (i in 0 until node.childCount) {
            collectSafeNodes(node.getChild(i), buttons, texts, depth + 1)
        }
    }

    private fun redactSensitiveTokens(input: String): String {
        val lower = input.lowercase()
        if (lower.contains("otp") || lower.contains("pin") || lower.contains("password") || lower.contains("cvv")) {
            return "[PROTECTED FIELD]"
        }
        return input
    }

    companion object {
        private var activeServiceInstance: HandsFreeAccessibilityService? = null

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _lastActivePackage = MutableStateFlow("")
        val lastActivePackage: StateFlow<String> = _lastActivePackage.asStateFlow()

        private val _visibleButtons = MutableStateFlow<List<String>>(emptyList())
        val visibleButtons: StateFlow<List<String>> = _visibleButtons.asStateFlow()

        private val _visibleScreenText = MutableStateFlow("")
        val visibleScreenText: StateFlow<String> = _visibleScreenText.asStateFlow()

        fun performGoBack(): Boolean {
            return activeServiceInstance?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        }

        fun performGoHome(): Boolean {
            return activeServiceInstance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }

        fun performOpenRecents(): Boolean {
            return activeServiceInstance?.performGlobalAction(GLOBAL_ACTION_RECENTS) ?: false
        }

        fun performScroll(forward: Boolean): Boolean {
            val root = activeServiceInstance?.rootInActiveWindow ?: return false
            val scrollable = findFirstScrollable(root) ?: return false
            val action = if (forward) {
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            } else {
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            }
            return scrollable.performAction(action)
        }

        fun findAndClickNodeByText(targetText: String): Boolean {
            val root = activeServiceInstance?.rootInActiveWindow ?: return false
            val query = targetText.trim()
            if (query.isEmpty()) return false

            val matches = root.findAccessibilityNodeInfosByText(query)
            for (node in matches) {
                if (node.isPassword) continue
                if (clickNodeOrParent(node)) return true
            }
            // Fallback search by contentDescription
            val descNode = findByContentDescription(root, query, 0)
            if (descNode != null && !descNode.isPassword) {
                return clickNodeOrParent(descNode)
            }
            return false
        }

        fun typeTextIntoFocusedField(text: String): Boolean {
            val root = activeServiceInstance?.rootInActiveWindow ?: return false
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?: findFirstEditable(root, 0)
                ?: return false

            // SECURITY: Never type into or tamper with password fields automatically
            if (focused.isPassword) return false

            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        fun readCurrentScreenSummary(): String {
            activeServiceInstance?.refreshScreenSnapshot()
            val text = _visibleScreenText.value
            val buttons = _visibleButtons.value
            return buildString {
                if (buttons.isNotEmpty()) {
                    append("Buttons: ${buttons.take(6).joinToString(", ")}. ")
                }
                if (text.isNotBlank()) {
                    append("Content: ${text.take(280)}")
                }
            }.ifBlank { "No non-sensitive screen content currently visible." }
        }

        private fun clickNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
            var current = node
            var hops = 0
            while (current != null && hops < 5) {
                if (current.isClickable) {
                    return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                current = current.parent
                hops++
            }
            return false
        }

        private fun findFirstScrollable(node: AccessibilityNodeInfo?, depth: Int = 0): AccessibilityNodeInfo? {
            if (node == null || depth > 14) return null
            if (node.isScrollable) return node
            for (i in 0 until node.childCount) {
                val found = findFirstScrollable(node.getChild(i), depth + 1)
                if (found != null) return found
            }
            return null
        }

        private fun findFirstEditable(node: AccessibilityNodeInfo?, depth: Int): AccessibilityNodeInfo? {
            if (node == null || depth > 14) return null
            if (node.isEditable && !node.isPassword) return node
            for (i in 0 until node.childCount) {
                val found = findFirstEditable(node.getChild(i), depth + 1)
                if (found != null) return found
            }
            return null
        }

        private fun findByContentDescription(node: AccessibilityNodeInfo?, query: String, depth: Int): AccessibilityNodeInfo? {
            if (node == null || depth > 14) return null
            val desc = node.contentDescription?.toString()
            if (!desc.isNullOrBlank() && desc.contains(query, ignoreCase = true)) {
                return node
            }
            for (i in 0 until node.childCount) {
                val found = findByContentDescription(node.getChild(i), query, depth + 1)
                if (found != null) return found
            }
            return null
        }
    }
}
