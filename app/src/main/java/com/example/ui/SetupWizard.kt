package com.example.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.KidSafeAccessibilityService
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.CoralWarm

@Composable
fun SetupWizard(onComplete: () -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A)), // Cyber Obsidian Dark Background
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)), // Cyber Slate Card
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WelcomeStep {
                    val prefs = context.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("setup_completed", true).apply()
                    onComplete()
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Icon(
        imageVector = Icons.Default.Security, 
        contentDescription = null, 
        tint = CoralWarm, // Neon Green
        modifier = Modifier.size(64.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = AppTranslator.t("Safe Next Gen"), 
        fontSize = 22.sp, 
        fontWeight = FontWeight.Bold, 
        color = Color.White, 
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = AppTranslator.t("ডিজিটাল আসক্তি মুক্তি ও ১০০% নিরাপদ ইন্টারনেট ফিল্টারিং প্ল্যাটফর্মে আপনাকে স্বাগতম।"), 
        color = Color.Gray, 
        textAlign = TextAlign.Center,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onNext, 
        colors = ButtonDefaults.buttonColors(containerColor = CoralWarm, contentColor = Color.Black), 
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(AppTranslator.t("শুরু করুন / Start Dashboard"), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VpnStep(context: Context, onNext: () -> Unit) {
    val vpnLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onNext()
    }

    Icon(Icons.Default.VpnKey, contentDescription = null, tint = DeepTeal, modifier = Modifier.size(64.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text(AppTranslator.t("Step 1: Network Protection"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepTeal, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(8.dp))
    Text(AppTranslator.t("We need to set up a secure local VPN to filter out adult content, malware, and ads."), color = Color.Gray, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = {
            val intent = VpnService.prepare(context)
            if (intent != null) {
                vpnLauncher.launch(intent)
            } else {
                onNext() // Already prepared
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(AppTranslator.t("Grant Permission"))
    }
}

@Composable
fun AccessibilityStep(context: Context, isEnabled: Boolean, onComplete: () -> Unit) {
    Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = if (isEnabled) SafeGreen else DeepTeal, modifier = Modifier.size(64.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text(AppTranslator.t("Step 2: Reels & Shorts Blocker"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepTeal, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(8.dp))
    Text(AppTranslator.t("To detect and block addictive short videos, we need Accessibility permission. Please find 'Safe Next Gen' in the downloaded apps list and enable it."), color = Color.Gray, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(24.dp))
    
    if (isEnabled) {
        Text(AppTranslator.t("Setup Complete"), color = SafeGreen, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onComplete, colors = ButtonDefaults.buttonColors(containerColor = SafeGreen), modifier = Modifier.fillMaxWidth()) {
            Text(AppTranslator.t("Finish Setup"))
        }
    } else {
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(AppTranslator.t("Grant Permission"))
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onComplete) {
            Text(AppTranslator.t("Skip for now"), color = Color.Gray)
        }
    }
}

fun checkAccessibility(context: Context): Boolean {
    val service = ComponentName(context, KidSafeAccessibilityService::class.java).flattenToString()
    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    if (enabledServices == null) return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(service, ignoreCase = true)) {
            return true
        }
    }
    return false
}
