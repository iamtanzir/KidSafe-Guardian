package com.example

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

class KahfDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Anti-Tamper Admin Shield Activated!", Toast.LENGTH_SHORT).show()
        
        // Enforce storage encryption policy upon activation
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(context, KahfDeviceAdminReceiver::class.java)
        if (dpm != null) {
            try {
                dpm.setStorageEncryption(adminComponent, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        // Intercepting disable requests to warn and prevent casual deactivation / uninstallation
        return "WARNING: Deactivating the Shield will expose the device. All network activity encryption keys, secure DNS bindings, and parent-configured PIN restrictions will be purged. Unauthorized deactivation attempts are logged securely."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Warning: Security Shield deactivated. Encryption keys removed.", Toast.LENGTH_LONG).show()
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(context, KahfDeviceAdminReceiver::class.java)
        if (dpm != null) {
            try {
                val attempts = dpm.getCurrentFailedPasswordAttempts()
                // Force an immediate device lock if someone makes multiple incorrect attempts (anti brute-force / forensic lock)
                if (attempts >= 3) {
                    dpm.lockNow()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

