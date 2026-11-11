import os

code = """package com.example

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*

class KidSafeAccessibilityService : AccessibilityService() {
    private var lastApp: String = ""
    private val addictiveApps = listOf(
        "com.facebook.katana", 
        "com.instagram.android", 
        "com.google.android.youtube", 
        "com.zhiliaoapp.musically"
    )
    private val delayedAppsTracker = mutableMapOf<String, Long>()
    
    // Optimization: Use a dedicated coroutine scope for background processing
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastReelsCheckTime: Long = 0L
    private val REELS_CHECK_THROTTLE_MS = 800L // Only check max once per 800ms to prevent CPU lag

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return
        val prefs = getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
        val isReelsBlocked = prefs.getBoolean("reels_blocked", false)
        val isDelayEnabled = prefs.getBoolean("delay_enabled", false)

        // 7-second delay logic (Lightweight, can run on main thread)
        if (isDelayEnabled && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packageName in addictiveApps && packageName != lastApp) {
                val lastTime = delayedAppsTracker[packageName] ?: 0L
                val now = System.currentTimeMillis()
                // Delay once per 5 minutes per app to avoid annoying loops
                if (now - lastTime > 5 * 60 * 1000) {
                    delayedAppsTracker[packageName] = now
                    val intent = Intent(this, DelayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra("PACKAGE_NAME", packageName)
                    }
                    startActivity(intent)
                }
            }
            if (packageName != "com.example") {
                lastApp = packageName
            }
        }

        // Reels/Shorts Blocker Logic - Heavily optimized for old CPUs
        if (isReelsBlocked && (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
            val now = System.currentTimeMillis()
            if (now - lastReelsCheckTime < REELS_CHECK_THROTTLE_MS) {
                return // Throttle to prevent UI lag on scroll
            }
            
            if (packageName in addictiveApps) {
                val rootNode = rootInActiveWindow ?: return
                lastReelsCheckTime = now
                
                // Offload heavy view-tree traversal to a background thread
                serviceScope.launch {
                    val isReel = checkForReels(rootNode)
                    rootNode.recycle() // Important: Recycle root node
                    
                    if (isReel) {
                        Log.d("KidSafeAccessibility", "Reels/Shorts detected! Going back.")
                        withContext(Dispatchers.Main) {
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        }
                    }
                }
            }
        }
    }

    // Recursive parsing (Now runs in background, avoiding ANRs and UI stutters)
    private fun checkForReels(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        var found = false
        
        try {
            if (node.text != null) {
                val text = node.text.toString().lowercase()
                if (text == "reels" || text == "shorts") return true
            }
            if (node.contentDescription != null) {
                val desc = node.contentDescription.toString().lowercase()
                if (desc == "reels" || desc == "shorts") return true
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    val childFound = checkForReels(child)
                    child.recycle() // CRITICAL: Recycle children to prevent massive memory leaks
                    if (childFound) {
                        found = true
                        break
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore potential view tree changes while parsing
        }
        
        return found
    }

    override fun onInterrupt() {
        Log.d("KidSafeAccessibility", "Service Interrupted")
    }
    
    override fun onDestroy() {
        serviceScope.cancel() // Clean up coroutines
        super.onDestroy()
    }
}
"""

with open('app/src/main/java/com/example/KidSafeAccessibilityService.kt', 'w') as f:
    f.write(code)

print("Accessibility optimized")
