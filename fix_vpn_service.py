import re

with open('app/src/main/java/com/example/KahfVpnService.kt', 'r') as f:
    content = f.read()

import_str = """
import android.net.TrafficStats
import android.os.Handler
import android.os.Looper
import java.util.Locale
"""
if 'import android.net.TrafficStats' not in content:
    content = content.replace('import androidx.core.app.NotificationCompat', 'import androidx.core.app.NotificationCompat' + import_str)

properties_str = """    private val CHANNEL_ID = "vpn_channel"

    private val handler = Handler(Looper.getMainLooper())
    private var lastRxBytes: Long = 0
    private var lastTxBytes: Long = 0
    private var lastTime: Long = 0
    private var isMonitoring = false

    private val trafficRunnable = object : Runnable {
        override fun run() {
            if (!isMonitoring) return

            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()
            val currentTime = System.currentTimeMillis()

            val timeDiff = (currentTime - lastTime) / 1000.0 // seconds
            if (timeDiff > 0) {
                val rxSpeed = ((currentRxBytes - lastRxBytes) / timeDiff).toLong()
                val txSpeed = ((currentTxBytes - lastTxBytes) / timeDiff).toLong()

                updateNotification(formatSpeed(rxSpeed), formatSpeed(txSpeed))
            }

            lastRxBytes = currentRxBytes
            lastTxBytes = currentTxBytes
            lastTime = currentTime

            handler.postDelayed(this, 1000)
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
            .setContentText("Save BD Next Gen Protection is active.")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
"""
content = re.sub(r'    private val CHANNEL_ID = "vpn_channel"', properties_str, content)

start_vpn_str = """        establishVpn()
        
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTime = System.currentTimeMillis()
        isMonitoring = true
        handler.post(trafficRunnable)
        
        return START_STICKY"""
content = content.replace('        establishVpn()\n        return START_STICKY', start_vpn_str)

disconnect_str = """    private fun disconnect() {
        isMonitoring = false
        handler.removeCallbacks(trafficRunnable)
        try {"""
content = content.replace('    private fun disconnect() {\n        try {', disconnect_str)

with open('app/src/main/java/com/example/KahfVpnService.kt', 'w') as f:
    f.write(content)

