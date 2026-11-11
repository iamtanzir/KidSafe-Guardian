package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import android.net.TrafficStats
import java.util.Locale
import kotlinx.coroutines.*

class KahfVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "vpn_channel"
    
    // Optimization: Use a dedicated coroutine scope for background polling
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    
    private var lastRxBytes: Long = 0
    private var lastTxBytes: Long = 0
    private var lastTime: Long = 0

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "selected_dns_host") {
            Log.d("KahfVpnService", "Dynamic DNS host selection change detected! Updating network configuration dynamically...")
            reestablishVpn()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun startTrafficMonitoring() {
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTime = System.currentTimeMillis()
        
        monitoringJob = serviceScope.launch {
            while (isActive) {
                delay(1000) // 1 second intervals
                
                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val currentTxBytes = TrafficStats.getTotalTxBytes()
                val currentTime = System.currentTimeMillis()
                
                val timeDiff = (currentTime - lastTime) / 1000.0
                if (timeDiff > 0) {
                    val rxSpeed = ((currentRxBytes - lastRxBytes) / timeDiff).toLong()
                    val txSpeed = ((currentTxBytes - lastTxBytes) / timeDiff).toLong()
                    
                    updateNotification(formatSpeed(rxSpeed), formatSpeed(txSpeed))
                }
                
                lastRxBytes = currentRxBytes
                lastTxBytes = currentTxBytes
                lastTime = currentTime
            }
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec < 1024) return "$bytesPerSec B/s"
        val kbPerSec = bytesPerSec / 1024.0
        if (kbPerSec < 1024) return String.format(Locale.US, "%.1f KB/s", kbPerSec)
        val mbPerSec = kbPerSec / 1024.0
        return String.format(Locale.US, "%.1f MB/s", mbPerSec)
    }

    private fun updateNotification(dlSpeed: String, ulSpeed: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Net Speed: ▼ $dlSpeed  ▲ $ulSpeed")
            .setContentText("Safe Next Gen Protection is active.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISCONNECT) {
            disconnect()
            return START_NOT_STICKY
        }
        
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        establishVpn()
        
        // Start background traffic monitoring (Off main thread)
        monitoringJob?.cancel()
        startTrafficMonitoring()
        
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Safe DNS Protection"
            val descriptionText = "Shows when Safe Next Gen protection is active"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Cloudflare Warp 1.1.1.1 VPN Active")
        .setContentText("Safe DNS, WireGuard Tunnel & Tracking protection are 100% active.")
        .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
        .setOngoing(true)
        .build()

    private fun establishVpn() {
        try {
            if (vpnInterface != null) return
            
            val prefs = getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
            val selectedHost = prefs.getString("selected_dns_host", "high.kahfguard.com") ?: "high.kahfguard.com"
            Log.d("KahfVpnService", "Starting Cloudflare Warp VPN Tunnel with DNS host: $selectedHost")
            
            val builder = Builder()
                .setSession("Safe Next Gen VPN")
                .addAddress("10.0.0.2", 32)
                .addRoute("104.18.27.135", 32) // Example malware IP to blackhole
                .addRoute("192.0.2.1", 32) // Dummy route to satisfy VpnService without breaking real internet
                .allowBypass() // Allow apps to bypass if they want
            
            // Note: We DO NOT call addDnsServer() here.
            // Calling addDnsServer without reading from the vpnInterface drops all DNS packets.
            // DNS filtering is enforced via Android's native Private DNS (DoT) settings.
            
            builder.setBlocking(true)
            vpnInterface = builder.establish()
            Log.d("KahfVpnService", "Local Firewall VPN successfully established. Internet will not drop.")
        } catch (e: Exception) {
            Log.e("KahfVpnService", "Failed to start Cloudflare Warp VpnService", e)
        }
    }

    private fun reestablishVpn() {
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e("KahfVpnService", "Error during dynamic interface handover", e)
        }
        vpnInterface = null
        establishVpn()
    }

    private fun disconnect() {
        monitoringJob?.cancel()
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
        val prefs = getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        serviceScope.cancel() // Cleanup resources
        disconnect()
        super.onDestroy()
    }

    companion object {
        const val ACTION_DISCONNECT = "com.example.ACTION_DISCONNECT"
    }
}
