import re

# 1. Update AndroidManifest.xml
with open('app/src/main/AndroidManifest.xml', 'r') as f:
    manifest = f.read()

permissions = """    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SYSTEM_EXEMPTED" />
"""
manifest = manifest.replace('    <uses-permission android:name="android.permission.INTERNET" />', permissions)
with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(manifest)

# 2. Update build.gradle.kts
with open('app/build.gradle.kts', 'r') as f:
    gradle = f.read()

gradle = gradle.replace('applicationId = "com.example"', 'applicationId = "com.savebd.nextgen"')
with open('app/build.gradle.kts', 'w') as f:
    f.write(gradle)

# 3. Update KahfVpnService.kt
vpn_service = """package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat

class KahfVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "vpn_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISCONNECT) {
            disconnect()
            return START_NOT_STICKY
        }
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        establishVpn()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Safe DNS Protection"
            val descriptionText = "Shows when Save BD Next Gen protection is active"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Save BD Next Gen Protection")
        .setContentText("Safe DNS and Tracking protection is currently active.")
        .setSmallIcon(android.R.drawable.ic_secure)
        .setOngoing(true)
        .build()

    private fun establishVpn() {
        try {
            if (vpnInterface != null) return
            Log.d("KahfVpnService", "Starting Kahf Guard Secure DNS Tunnel...")
            
            val builder = Builder()
                .setSession("Kahf Guard Safe DNS")
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("103.191.240.240")
                .addDnsServer("103.191.241.241")
                .setBlocking(true)
            
            vpnInterface = builder.establish()
            Log.d("KahfVpnService", "Kahf Guard Safe DNS active.")
        } catch (e: Exception) {
            Log.e("KahfVpnService", "Failed to start Kahf VpnService", e)
        }
    }

    private fun disconnect() {
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e("KahfVpnService", "Error closing VPN Interface", e)
        }
        vpnInterface = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
        Log.d("KahfVpnService", "Kahf Guard Safe DNS disconnected.")
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    companion object {
        const val ACTION_DISCONNECT = "com.example.ACTION_DISCONNECT"
    }
}
"""
with open('app/src/main/java/com/example/KahfVpnService.kt', 'w') as f:
    f.write(vpn_service)
    
# 4. Update KidSafeScreens.kt DeveloperInfoCard
with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    screens = f.read()

old_annotated = """                withStyle(style = SpanStyle(color = Color.LightGray, fontWeight = FontWeight.Bold)) {
                    append("  |  Version: ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF45C493), fontWeight = FontWeight.Bold)) {
                    append("v2.01")
                }
            }"""

new_annotated = """                withStyle(style = SpanStyle(color = Color.LightGray, fontWeight = FontWeight.Bold)) {
                    append("  |  Version: ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF45C493), fontWeight = FontWeight.Bold)) {
                    append("v2.01")
                }
                withStyle(style = SpanStyle(color = Color.LightGray, fontWeight = FontWeight.Bold)) {
                    append("\\nGitHub: ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)) {
                    append("https://github.com/iamtanzir")
                }
            }"""
screens = screens.replace(old_annotated, new_annotated)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(screens)

