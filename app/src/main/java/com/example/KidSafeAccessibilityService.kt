package com.example

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class KidSafeAccessibilityService : AccessibilityService() {
    private var lastApp: String = ""
    private val addictiveApps = listOf(
        "com.facebook.katana", 
        "com.instagram.android", 
        "com.google.android.youtube", 
        "com.zhiliaoapp.musically"
    )
    private val delayedAppsTracker = mutableMapOf<String, Long>()
    private var lastReelsCheckTime: Long = 0L
    private val REELS_CHECK_THROTTLE_MS = 1000L // Throttled to 1 second per check on the main thread to prevent CPU lag completely

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return
        val prefs = getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
        val isReelsBlocked = prefs.getBoolean("reels_blocked", false)
        val isDelayEnabled = prefs.getBoolean("delay_enabled", false)

        // 7-second delay logic (Lightweight, runs on main thread)
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
            if (packageName != "com.example" && packageName != "com.savebd.nextgen") {
                lastApp = packageName
            }
        }

        // Reels/Shorts Blocker Logic - Optimized to run safely on Main Thread with strict exception catching
        if (isReelsBlocked && (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
            val className = event.className?.toString()?.lowercase() ?: ""
            if (className.contains("reelwatchactivity") || 
                className.contains("shortsvideo") || 
                className.contains("clipsviewer")) {
                Log.d("KidSafeAccessibility", "Reels/Shorts Activity detected! Going back.")
                val current = prefs.getInt("blocked_social_count", 0)
                prefs.edit().putInt("blocked_social_count", current + 1).apply()
                performGlobalAction(GLOBAL_ACTION_HOME)
                return
            }

            val now = System.currentTimeMillis()
            if (now - lastReelsCheckTime < REELS_CHECK_THROTTLE_MS) {
                return // Throttle checks to prevent UI lag
            }
            
            if (packageName in addictiveApps) {
                val rootNode = rootInActiveWindow ?: return
                lastReelsCheckTime = now
                
                try {
                    val isReel = checkForReels(rootNode)
                    if (isReel) {
                        Log.d("KidSafeAccessibility", "Reels/Shorts View detected! Going back.")
                        val current = prefs.getInt("blocked_social_count", 0)
                        prefs.edit().putInt("blocked_social_count", current + 1).apply()
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                } catch (e: Throwable) {
                    Log.e("KidSafeAccessibility", "Error checking for reels", e)
                } finally {
                    try {
                        rootNode.recycle()
                    } catch (t: Throwable) {
                        // Safe ignore
                    }
                }
            }
        }
    }

    // Recursive parsing (Safely runs on the main thread with depth/iteration limits and catch-all protection)
    private fun checkForReels(node: AccessibilityNodeInfo?, depth: Int = 0): Boolean {
        if (node == null || depth > 25) return false // Prevent deep recursion or stack overflows
        
        try {
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            
            // Highly accurate View ID matching for actual immersive players (not just tab buttons)
            if (viewId.contains("reel_recycler") || // YouTube
                viewId.contains("reel_viewer") || // YouTube / Instagram
                viewId.contains("clips_video_container") || // Instagram
                viewId.contains("shorts_player") || // YouTube
                viewId.contains("fb_shorts") // Facebook
            ) {
                return true
            }
            
            // Fallback: Check if there's a full-screen vertical pager (often used for reels)
            // with a description indicating it's a short video
            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""
            
            // Only flag text if it's explicitly a "Swipe" instruction usually seen in shorts
            if ((text.contains("swipe up for next") || desc.contains("swipe down to return")) && 
                (desc.contains("reel") || desc.contains("short"))) {
                return true
            }
            
            val childCount = node.childCount
            for (i in 0 until childCount) {
                val child = node.getChild(i) ?: continue
                val found = checkForReels(child, depth + 1)
                try {
                    child.recycle()
                } catch (t: Throwable) {
                    // Safe ignore
                }
                if (found) {
                    return true
                }
            }
        } catch (e: Throwable) {
            // Safe ignore view tree modifications during traversal
        }
        
        return false
    }

    override fun onInterrupt() {
        Log.d("KidSafeAccessibility", "Service Interrupted")
    }
}
