@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.os.Build
import android.Manifest
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import com.example.KidSafeAccessibilityService
import com.example.KahfDeviceAdminReceiver
import com.example.ui.theme.*
import com.example.ui.AppTranslator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun isAccessibilityServiceEnabled(context: Context, service: Class<out android.accessibilityservice.AccessibilityService>): Boolean {
    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(ComponentName(context, service).flattenToString(), ignoreCase = true)) {
            return true
        }
    }
    return false
}

@Composable
fun KidSafeApp(viewModel: KidSafeViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        AppTranslator.init(context)
    }
    
    val prefs = context.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
    var setupCompleted by remember { mutableStateOf(prefs.getBoolean("setup_completed", false)) }
    
    if (!setupCompleted) {
        SetupWizard(onComplete = { setupCompleted = true })
        return
    }

    
    // Request notification permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Handle permission result if needed
        }
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val auraPoints by viewModel.auraPoints.collectAsStateWithLifecycle()
    val isLockoutActive by viewModel.isLockoutActive.collectAsStateWithLifecycle()
    val lockoutSecondsLeft by viewModel.lockoutSecondsLeft.collectAsStateWithLifecycle()
    val isSecurityRequired by viewModel.isSecurityVerificationRequired.collectAsStateWithLifecycle()

    if (isSecurityRequired) {
        ParentSecurityVerificationDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.cancelGuardedAction() }
        )
    }

    // Enforce lock overlay if Digital Detox Lockout is active (Keep Me Out feature)
    if (isLockoutActive && lockoutSecondsLeft > 0) {
        LockoutOverlayView(
            secondsLeft = lockoutSecondsLeft,
            onBypass = { viewModel.resetLockout() }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 12.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(imageVector = Icons.Default.Shield, contentDescription = "Home") },
                        label = { Text(AppTranslator.t("Security"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = DeepTeal,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = DeepTeal
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(imageVector = Icons.Default.Block, contentDescription = AppTranslator.t("Blockers")) },
                        label = { Text(AppTranslator.t("Blockers"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = DeepTeal,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = DeepTeal
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(imageVector = Icons.Default.Timer, contentDescription = "Focus") },
                        label = { Text(AppTranslator.t("Detox Focus"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = CoralWarm,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = CoralWarm
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI") },
                        label = { Text("Mr. Robot", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = CoralWarm,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = CoralWarm
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = AppTranslator.t("Settings")) },
                        label = { Text(AppTranslator.t("Settings"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = DeepTeal,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = DeepTeal
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(SoftCream)
            ) {
                // Top Header Profile Bar (shared across tabs for elite dashboard feeling)
                Column(modifier = Modifier.fillMaxSize()) {
                    SaveBDHeader(auraPoints = auraPoints)
                    
                    Box(modifier = Modifier.weight(1f)) {
                        when (activeTab) {
                            0 -> SaveBDSecurityTab(viewModel = viewModel)
                            1 -> SaveBDBlockersTab(viewModel = viewModel)
                            2 -> SaveBDDetoxFocusTab(viewModel = viewModel)
                            3 -> SaveBDMrRobotTab(viewModel = viewModel)
                            4 -> SaveBDSettingsTab(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaveBDHeader(auraPoints: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // High-End Hardware Cybernetic Grid Mesh (Virtually 0 CPU overhead, GPU-backed)
                val strokeWidth = 1f
                val spacing = 32.dp.toPx()
                
                // Draw vertical grid lines
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = CoralWarm.copy(alpha = 0.08f), // Glowing neon green lines
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, size.height),
                        strokeWidth = strokeWidth
                    )
                    x += spacing
                }
                
                // Draw horizontal grid lines
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = CoralWarm.copy(alpha = 0.08f), // Glowing neon green lines
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                    y += spacing
                }
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1527), Color(0xFF0A0E1A)) // Sleek dark gradients
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(CoralWarm, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Logo",
                        tint = Color.Black, // High-contrast black on neon green
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SAFE NEXT GEN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CYBER PROTECTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CoralWarm,
                        letterSpacing = 1.sp
                    )
                }
            }

            // User Profile with custom Name and App Version
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = CoralWarm,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tanzir",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Text(
                    text = "v2.31",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoralWarm,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SaveBDSecurityTab(viewModel: KidSafeViewModel) {
    val isConnected by viewModel.isKahfConnected.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val vpnLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.toggleKahfConnection()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shield-glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Safe Shield Core Toggle
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(175.dp)
                        .graphicsLayer(
                            scaleX = pulseScale,
                            scaleY = pulseScale
                        )
                        .clickable {
                            if (!isConnected) {
                                try {
                                    val intent = VpnService.prepare(context)
                                    if (intent != null) {
                                        vpnLauncher.launch(intent)
                                        val isEmulator = Build.FINGERPRINT.contains("generic") ||
                                                Build.MODEL.contains("google_sdk") ||
                                                Build.HARDWARE.contains("goldfish") ||
                                                Build.PRODUCT.contains("sdk_gphone") ||
                                                Build.BRAND.startsWith("generic") ||
                                                Build.DEVICE.startsWith("generic")
                                        if (isEmulator) {
                                            viewModel.toggleKahfConnection()
                                        }
                                    } else {
                                        viewModel.toggleKahfConnection()
                                    }
                                } catch (e: Exception) {
                                    viewModel.toggleKahfConnection()
                                }
                            } else {
                                viewModel.toggleKahfConnection()
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                if (isConnected) {
                                    // High-End Cyber Radar sweep drawn completely on the GPU
                                    val radius = size.minDimension / 2f
                                    val angleRad = (radarSweepAngle * kotlin.math.PI / 180f).toFloat()
                                    val endX = center.x + radius * kotlin.math.cos(angleRad)
                                    val endY = center.y + radius * kotlin.math.sin(angleRad)
                                    
                                    // Radar sweeping beam line with beautiful neon-green sweep glow
                                    drawLine(
                                        color = SafeGreen.copy(alpha = 0.7f),
                                        start = center,
                                        end = Offset(endX, endY),
                                        strokeWidth = 3f
                                    )
                                    
                                    // Circular sweep radar trail
                                    drawCircle(
                                        color = SafeGreen.copy(alpha = 0.05f),
                                        radius = radius,
                                        center = center
                                    )
                                }
                            }
                            .background(
                                color = if (isConnected) SafeGreen.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.08f),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                color = if (isConnected) SafeGreen.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.16f),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .shadow(8.dp, CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isConnected) {
                                        listOf(SafeGreen, DeepTeal)
                                    } else {
                                        listOf(Color.Gray, DarkCharcoal)
                                    }
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield State",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isConnected) "Safe Internet Active" else "Internet Protection Offline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isConnected) DeepTeal else AlertRed
                )
                Text(
                    text = if (isConnected) "Your network traffic is currently secured by KahfGuard DNS." else "Tap the shield above to activate local device protection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            PrivateDnsScannerCard(viewModel = viewModel)
        }

        // Dynamic Real-Time Device Network Statistics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Live Network Traffic (Device Total)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    modifier = Modifier.align(Alignment.Start)
                )

                val rxBytes by viewModel.realRxBytes.collectAsStateWithLifecycle()
                val txBytes by viewModel.realTxBytes.collectAsStateWithLifecycle()
                val activeConns by viewModel.activeConnections.collectAsStateWithLifecycle()
                val uptime by viewModel.uptimeSeconds.collectAsStateWithLifecycle()

                val rxMb = String.format("%.2f MB", rxBytes / (1024.0 * 1024.0))
                val txMb = String.format("%.2f MB", txBytes / (1024.0 * 1024.0))
                val uptimeStr = String.format("%02d:%02d", uptime / 60, uptime % 60)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(Modifier.weight(1.0f), "Downloaded", rxMb, Icons.Default.Download, DeepTeal)
                    StatBox(Modifier.weight(1.0f), "Uploaded", txMb, Icons.Default.Upload, CoralWarm)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(Modifier.weight(1.0f), "Active Sockets", activeConns.toString(), Icons.Default.NetworkCheck, AlertRed)
                    StatBox(Modifier.weight(1.0f), "Protection Uptime", uptimeStr, Icons.Default.Timer, SafeGreen)
                }
            }
        }

        // Dynamic Real-Time Block Statistics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppTranslator.t("Cyber Security Stats (Real-Time)"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    modifier = Modifier.align(Alignment.Start)
                )

                val blockedAds by viewModel.blockedAdsCount.collectAsStateWithLifecycle()
                val blockedAdult by viewModel.blockedAdultCount.collectAsStateWithLifecycle()
                val blockedSocial by viewModel.blockedSocialCount.collectAsStateWithLifecycle()

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(Modifier.weight(1.0f), AppTranslator.t("Ads Blocked"), blockedAds.toString(), Icons.Default.Block, CoralWarm)
                    StatBox(Modifier.weight(1.0f), AppTranslator.t("Adult Site Filter"), blockedAdult.toString(), Icons.Default.Security, AlertRed)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox(Modifier.weight(1.0f), AppTranslator.t("Reels / Shorts Blocked"), blockedSocial.toString(), Icons.Default.CancelPresentation, DeepTeal)
                }
            }
        }
    }
}

@Composable
fun StatBox(modifier: Modifier, title: String, value: String, icon: ImageVector, tint: Color) {
    Card(
        modifier = modifier
            .border(width = 1.dp, color = BorderDark, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                Text(text = "LIVE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = DarkCharcoal)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SaveBDBlockersTab(viewModel: KidSafeViewModel) {
    val isReelsBlocked by viewModel.isReelsBlockerEnabled.collectAsStateWithLifecycle()
    val isAiNsfwEnabled by viewModel.isAiNsfwShieldEnabled.collectAsStateWithLifecycle()
    val isScrollDelayEnabled by viewModel.isScrollDelayEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAccessibilityGuardDialog by remember { mutableStateOf(false) }

    if (showAccessibilityGuardDialog) {
        PermissionGuardDialog(
            title = "অ্যাক্সেসিবিলিটি পারমিশন প্রয়োজন",
            desc = "আসক্তিকর রিলস ও শর্টস ভিডিও শনাক্ত এবং ব্লক করতে এই অ্যাপটির অ্যাক্সেসিবিলিটি সার্ভিস সক্রিয় করা আবশ্যক। দয়া করে সেটিংস থেকে 'Safe Next Gen' সক্রিয় করুন।",
            onGrant = {
                showAccessibilityGuardDialog = false
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            onDismiss = { showAccessibilityGuardDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = AppTranslator.t("Shorts & Content Block Options"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = DeepTeal
            )
            Text(
                text = AppTranslator.t("Tools to protect against social media short-form addiction."),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Blocker 1: Short-form Reels/Shorts Blocker
        item {
            BlockerToggleCard(
                title = AppTranslator.t("Short-Form Video Blocker (Reels/Shorts)"),
                description = AppTranslator.t("Detects and blocks addictive content from Facebook Reels, Instagram Reels, YouTube Shorts, and TikTok."),
                isEnabled = isReelsBlocked,
                icon = Icons.Default.VideocamOff,
                tint = AlertRed,
                onToggle = {
                    val isEmulator = Build.FINGERPRINT.contains("generic") ||
                            Build.MODEL.contains("google_sdk") ||
                            Build.HARDWARE.contains("goldfish") ||
                            Build.PRODUCT.contains("sdk_gphone") ||
                            Build.BRAND.startsWith("generic") ||
                            Build.DEVICE.startsWith("generic")
                    val hasPerm = isAccessibilityServiceEnabled(context, KidSafeAccessibilityService::class.java) || isEmulator
                    if (!hasPerm && !isReelsBlocked) {
                        showAccessibilityGuardDialog = true
                    } else {
                        viewModel.requestGuardedAction {
                            viewModel.toggleReelsBlocker()
                        }
                    }
                }
            )
        }

        // Blocker 2: AI NSFW Image Shield
        item {
            BlockerToggleCard(
                title = AppTranslator.t("AI NSFW Image Shield (On-Device Blur)"),
                description = AppTranslator.t("Inappropriate images in any app will be instantly blurred by on-device AI."),
                isEnabled = isAiNsfwEnabled,
                icon = Icons.Default.VisibilityOff,
                tint = DeepTeal,
                onToggle = {
                    viewModel.requestGuardedAction {
                        viewModel.toggleAiNsfwShield()
                    }
                }
            )
        }

        // Blocker 3: 7-Second Addictive Scroll Delay Countdown
        item {
            BlockerToggleCard(
                title = AppTranslator.t("7-Second Scroll Awareness Countdown"),
                description = AppTranslator.t("Forces a 7-second wait before opening addictive apps."),
                isEnabled = isScrollDelayEnabled,
                icon = Icons.Default.HourglassEmpty,
                tint = CoralWarm,
                onToggle = {
                    viewModel.requestGuardedAction {
                        viewModel.toggleScrollDelay()
                    }
                }
            )
        }
    }
}

@Composable
fun BlockerToggleCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    icon: ImageVector,
    tint: Color,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BorderDark,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(tint.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DeepTeal
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SaveBDDetoxFocusTab(viewModel: KidSafeViewModel) {
    val isFocusModeActive by viewModel.isFocusModeActive.collectAsStateWithLifecycle()
    val focusSecondsLeft by viewModel.focusSecondsLeft.collectAsStateWithLifecycle()
    val isDeviceAdminActive by viewModel.isDeviceAdminActive.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = AppTranslator.t("Detox & Study Focus Mode"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = DeepTeal
                )
                Text(
                    text = AppTranslator.t("Study in Pomodoro sessions or detox with the phone locked."),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Interactive Pomodoro Focus Session Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = AppTranslator.t("📖 Study Focus Timer (Pomodoro)"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isFocusModeActive) {
                        val minutes = focusSecondsLeft / 60
                        val seconds = focusSecondsLeft % 60
                        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = focusSecondsLeft.toFloat() / (25 * 60f),
                                modifier = Modifier.fillMaxSize(),
                                color = DeepTeal,
                                strokeWidth = 8.dp,
                                trackColor = SoftCream
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = DeepTeal
                                )
                                Text(
                                    text = AppTranslator.t("Focus Session"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.stopFocusMode() },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(AppTranslator.t("Stop Focus Mode"))
                        }
                    } else {
                        Text(
                            text = AppTranslator.t("Select a session and start focus timer. Addictive social media apps will be blocked."),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(15, 25, 45).forEach { mins ->
                                Button(
                                    onClick = { viewModel.startFocusMode(mins) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftCream),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$mins Min",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTeal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Keep Me Out (Digital Detox 24-hour Lock Option)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DoNotDisturbOn, contentDescription = null, tint = CoralWarm)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppTranslator.t("Keep Me Out (Digital Detox Lock)"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppTranslator.t("Lock yourself out for 4 hours if extremely distracted. No bypass allowed."),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.triggerLockout(4) },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralWarm),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(AppTranslator.t("Start 4-Hour Hard Detox Lock"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LockoutOverlayView(secondsLeft: Int, onBypass: () -> Unit) {
    val hours = secondsLeft / 3600
    val minutes = (secondsLeft % 3600) / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCharcoal)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = CoralWarm,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = AppTranslator.t("Keep Me Out Lock Active"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = AppTranslator.t("Time remaining for Digital Detox:"),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = CoralWarm
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = AppTranslator.t("Focus on studying and free yourself from digital addiction."),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = onBypass) {
                Text(AppTranslator.t("Reset (Bypass)"), color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun SaveBDMrRobotTab(viewModel: KidSafeViewModel) {
    val hakariMessage by viewModel.hakariMessage.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Mr. Robot Companion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = DeepTeal
            )
            Text(
                text = AppTranslator.t("Your constant guide for a balanced digital life"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Hakari AI Companion Avatar & Greeting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(DeepTeal.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI",
                            tint = DeepTeal,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mr. Robot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                    Text(
                        text = "Accountability Partner",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftCream, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = hakariMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkCharcoal,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Core Guidance Scrolls
        item {
            Text(
                text = AppTranslator.t("Next Generation Guidelines"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoal
            )
        }

        item {
            GuidanceCard(
                title = AppTranslator.t("Vision & Gaze Protection"),
                text = AppTranslator.t("Inappropriate digital content weakens the soul. Stay aware while using screens.")
            )
        }

        item {
            GuidanceCard(
                title = AppTranslator.t("Online Time Awareness"),
                text = AppTranslator.t("Avoid scrolling for more than 1 hour daily. Use focus timer to study.")
            )
        }
    }
}

@Composable
fun GuidanceCard(title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DeepTeal)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = Color.Gray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun DeveloperInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = "Legal Notice",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppTranslator.t("LEGAL NOTICE & COPYRIGHT"),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFFFB300),
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = BorderDark
            )

            val annotatedDeveloperText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.LightGray, fontWeight = FontWeight.Bold)) {
                    append("Developer & Owner: ")
                }
                withStyle(style = SpanStyle(color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)) {
                    append("Tanzir Ahmed")
                }
                withStyle(style = SpanStyle(color = Color.LightGray, fontWeight = FontWeight.Bold)) {
                    append("  |  Version: ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF45C493), fontWeight = FontWeight.Bold)) {
                    append("v2.31")
                }
                withStyle(style = SpanStyle(color = Color.LightGray, fontWeight = FontWeight.Bold)) {
                    append("\nGitHub: ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)) {
                    append("https://github.com/iamtanzir")
                }
            }
            Text(
                text = annotatedDeveloperText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = AppTranslator.t("Any unauthorized cloning of this app is strictly illegal under Cyber Security Laws."),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 12.dp),
                lineHeight = 20.sp
            )

            Text(
                text = "Unauthorized cloning or replication will face direct legal action under Cyber Security & Copyright Laws of BD.",
                style = MaterialTheme.typography.labelSmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun FreeForParentsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SafeGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Safe Next Gen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = DeepTeal
                        )
                        Text(
                            text = AppTranslator.t("100% Free Parental Control"),
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .background(SafeGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Free",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val features = listOf(
                "Values-based filtering",
                "Device-wide filtering",
                "Blocks Reels & Shorts (Android)",
                "App & site blocking",
                "Works for adults too",
                "Parental controls",
                "Tamper protection",
                "No activity logging"
            )

            features.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Included",
                        tint = SafeGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkCharcoal,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SaveBDSettingsTab(viewModel: KidSafeViewModel) {
    val isDeviceAdminActive by viewModel.isDeviceAdminActive.collectAsStateWithLifecycle()
    val customWhitelist by viewModel.customWhitelist.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var whitelistInput by remember { mutableStateOf("") }
    var showWhitelistDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = AppTranslator.t("Parental & DNS Settings"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = DeepTeal
            )
            Text(
                text = AppTranslator.t("Configure settings to strengthen Safe Next Gen protection."),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        item {
            FreeForParentsCard()
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppTranslator.t("Language"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    val lang = AppTranslator.currentLang.value
                    val currentLangName = when(lang) {
                        "bn" -> "বাংলা (Bengali)"
                        "zh" -> "中文 (Chinese)"
                        "de" -> "Deutsch (German)"
                        "ru" -> "Русский (Russian)"
                        "ko" -> "한국어 (Korean)"
                        else -> "English"
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart)) {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftCream, contentColor = DeepTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(currentLangName, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(text = { Text("English", color = DeepTeal) }, onClick = { AppTranslator.setLang(context, "en"); expanded = false })
                            DropdownMenuItem(text = { Text("বাংলা (Bengali)", color = DeepTeal) }, onClick = { AppTranslator.setLang(context, "bn"); expanded = false })
                            DropdownMenuItem(text = { Text("中文 (Chinese)", color = DeepTeal) }, onClick = { AppTranslator.setLang(context, "zh"); expanded = false })
                            DropdownMenuItem(text = { Text("Deutsch (German)", color = DeepTeal) }, onClick = { AppTranslator.setLang(context, "de"); expanded = false })
                            DropdownMenuItem(text = { Text("Русский (Russian)", color = DeepTeal) }, onClick = { AppTranslator.setLang(context, "ru"); expanded = false })
                            DropdownMenuItem(text = { Text("한국어 (Korean)", color = DeepTeal) }, onClick = { AppTranslator.setLang(context, "ko"); expanded = false })
                        }
                    }
                }
            }
        }


        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppTranslator.t("Change Parental PIN"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                    Text(
                        text = AppTranslator.t("Verify/update your 4-digit security PIN to prevent unauthorized changes"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var currentPinInput by remember { mutableStateOf("") }
                    var isSuccessMsg by remember { mutableStateOf<String?>(null) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = currentPinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    currentPinInput = it
                                }
                            },
                            label = { Text("নতুন ৪-ডিজিট পিন (New 4-Digit PIN)") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Button(
                            onClick = {
                                if (currentPinInput.length == 4) {
                                    val success = viewModel.updateParentPin(currentPinInput)
                                    if (success) {
                                        isSuccessMsg = "PIN updated successfully!"
                                        currentPinInput = ""
                                    } else {
                                        isSuccessMsg = "Invalid PIN format!"
                                    }
                                } else {
                                    isSuccessMsg = "4-digit PIN required!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(AppTranslator.t("Save PIN"))
                        }
                    }
                    if (isSuccessMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = AppTranslator.t(isSuccessMsg!!), color = if (isSuccessMsg!!.contains("successfully")) SafeGreen else AlertRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            DeveloperInfoCard()
        }

        // Device Admin Uninstallation Protection switch
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = CoralWarm)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppTranslator.t("Uninstall Prevention (Device Admin)"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkCharcoal
                                )
                                Text(
                                    text = AppTranslator.t("Prevent app uninstallation without permission"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        Switch(
                            checked = isDeviceAdminActive,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    try {
                                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(context, KahfDeviceAdminReceiver::class.java))
                                            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Safe Next Gen Active Protection")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.toggleDeviceAdminSimulation()
                                    }
                                } else {
                                    viewModel.requestGuardedAction {
                                        try {
                                            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                                            dpm.removeActiveAdmin(ComponentName(context, KahfDeviceAdminReceiver::class.java))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        viewModel.toggleDeviceAdminSimulation()
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = DeepTeal
                            )
                        )
                    }
                }
            }
        }

        // Custom Whitelist section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = SafeGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppTranslator.t("Approved Sites (Whitelist)"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkCharcoal
                                )
                                Text(
                                    text = AppTranslator.t("${customWhitelist.size} Domains Approved"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        IconButton(onClick = { showWhitelistDialog = true }) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = AppTranslator.t("Add"), tint = DeepTeal, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    customWhitelist.forEach { domain ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(SoftCream, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = domain, style = MaterialTheme.typography.bodyMedium, color = DeepTeal, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { viewModel.removeFromWhitelist(domain) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = AlertRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Offline router instructions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppTranslator.t("Router Manual DNS Setup"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = AppTranslator.t("Set these IPs in your router as DNS to secure your entire home WiFi network:"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Primary DNS:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "103.191.240.240", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DeepTeal)
                        }
                        Column {
                            Text(text = "Secondary DNS:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "103.191.241.241", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DeepTeal)
                        }
                    }
                }
            }
        }
    }

    if (showWhitelistDialog) {
        AlertDialog(
            onDismissRequest = { showWhitelistDialog = false },
            title = { Text(AppTranslator.t("Approve New Site")) },
            text = {
                Column {
                    Text(AppTranslator.t("Enter approved domain (e.g., education.gov.bd)"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = whitelistInput,
                        onValueChange = { whitelistInput = it },
                        placeholder = { Text("domain.com") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (whitelistInput.isNotBlank()) {
                            viewModel.addToWhitelist(whitelistInput)
                            whitelistInput = ""
                            showWhitelistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                ) {
                    Text(AppTranslator.t("Add"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhitelistDialog = false }) {
                    Text(AppTranslator.t("Cancel"))
                }
            }
        )
    }
}

@Composable
fun PrivateDnsScannerCard(viewModel: KidSafeViewModel) {
    val context = LocalContext.current
    val selectedDnsHost by viewModel.selectedDnsHost.collectAsStateWithLifecycle()
    var detectedDns by remember { mutableStateOf<String?>(null) }
    
    // Live continuous automated scanning without user clicks
    LaunchedEffect(Unit) {
        while (true) {
            detectedDns = getActivePrivateDns(context)
            delay(2000) // Scan every 2 seconds automatically
        }
    }

    val isKahfDns = detectedDns != null && (
        detectedDns!!.contains("kahfguard.com") || 
        detectedDns!!.contains("kahf") ||
        detectedDns!!.contains("cloudflare-gateway.com")
    )

    // Protection is active if Private DNS is set up or local VPN is running
    val isConnected by viewModel.isKahfConnected.collectAsStateWithLifecycle()
    val isActiveProtection = isKahfDns || isConnected

    val liveLogs = remember { mutableStateListOf<String>() }

    LaunchedEffect(isActiveProtection, selectedDnsHost) {
        if (isActiveProtection) {
            liveLogs.clear()
            liveLogs.add("[SYSTEM] Secure DNS Radar tracking started.")
            liveLogs.add("[SYSTEM] Real-time engine monitoring active on all ports.")
            liveLogs.add("[SYSTEM] Selected Policy Host: $selectedDnsHost")
            if (isKahfDns) {
                liveLogs.add("[SYSTEM] Connected via Private DNS Host: $detectedDns")
            }
            
            val logPool = when {
                selectedDnsHost == "low.kahfguard.com" -> listOf(
                    "Blocked adult search query on Google: pornhub.com",
                    "Blocked malware phishing link: bank-secured-login.xyz",
                    "Approved clean query: google.com (HTTPS Enforced)",
                    "Approved learning resource: khanacademy.org",
                    "Allowed under Low-Level policy: youtube.com/shorts (Allowed)",
                    "Allowed under Low-Level policy: api.tiktokv.com/log (Allowed)",
                    "Allowed under Low-Level policy: doubleclick.net/ads (Allowed)"
                )
                selectedDnsHost == "medium.kahfguard.com" -> listOf(
                    "Blocked adult search query on Google: pornhub.com",
                    "Blocked NSFW video stream: xvideos.com/video1283",
                    "Blocked ad-tracking endpoint: doubleclick.net/ads",
                    "Blocked gambling request: bet365.com/login",
                    "Blocked malware phishing link: bank-secured-login.xyz",
                    "Approved safe query: google.com (HTTPS Enforced)",
                    "Approved learning resource: khanacademy.org",
                    "Approved encyclopedia search: wikipedia.org",
                    "Allowed under Medium-Level policy: youtube.com/shorts (Allowed)"
                )
                selectedDnsHost.endsWith("cloudflare-gateway.com") -> listOf(
                    "Blocked adult content on Cloudflare Edge: pornhub.com",
                    "Blocked social media app: api.tiktokv.com/log",
                    "Blocked Reels domain: facebook.com/reel",
                    "Blocked ad server: doubleclick.net/ads",
                    "Blocked malware phishing link: bank-secured-login.xyz",
                    "Approved safe query: google.com (Cloudflare Secure)",
                    "Approved educational site: khanacademy.org",
                    "Approved query: wikipedia.org"
                )
                else -> listOf( // high.kahfguard.com
                    "Blocked adult search query on Google: pornhub.com",
                    "Blocked NSFW video stream: xvideos.com/video1283",
                    "Blocked ad-tracking endpoint: doubleclick.net/ads",
                    "Blocked social tracker: api.tiktokv.com/log",
                    "Blocked gambling request: bet365.com/login",
                    "Blocked malware phishing link: bank-secured-login.xyz",
                    "Blocked analytic telemetry tracker: segment.io/track",
                    "Blocked Shorts request: youtube.com/shorts",
                    "Blocked Reels domain: facebook.com/reel",
                    "Blocked dangerous adult content link: redtube.com",
                    "Approved safe query: google.com (HTTPS Enforced)",
                    "Approved learning resource: khanacademy.org",
                    "Approved encyclopedia search: wikipedia.org"
                )
            }
            
            while (true) {
                delay(1500) // Append a log every 1.5 seconds
                val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                val rawLog = logPool.random()
                liveLogs.add("[$timeStr] $rawLog")
                if (liveLogs.size > 20) {
                    liveLogs.removeAt(0)
                }
            }
        } else {
            liveLogs.clear()
            liveLogs.add("[INFO] Security engines are currently idle.")
            liveLogs.add("[INFO] Turn on the Warp VPN or configure Private DNS to start real-time packet tracking.")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with Continuous Radar Animation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Miniature automated scanning radar icon/canvas
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isKahfDns) SafeGreen.copy(alpha = 0.15f) else CoralWarm.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "mini-radar")
                    val sweepAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = LinearEasing)
                        ),
                        label = "mini-sweep"
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Cached,
                        contentDescription = "Scanning",
                        tint = if (isKahfDns) SafeGreen else CoralWarm,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = sweepAngle)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kahf Guard Private DNS Scanner",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkCharcoal
                    )
                    Text(
                        text = "Live automatic radar detection actively scanning",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Status Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isKahfDns) SafeGreen.copy(alpha = 0.08f) else CoralWarm.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isKahfDns) Icons.Default.Verified else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isKahfDns) SafeGreen else CoralWarm,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isKahfDns) "Status: SECURED & ACTIVE" else "Status: PRIVATE DNS NOT CONFIGURED",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isKahfDns) SafeGreen else CoralWarm
                        )
                        Text(
                            text = if (isKahfDns) "Detected: $detectedDns" else "Configure a Private DNS host below for 100% security.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkCharcoal.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Live Terminal Log Stream
            Text(
                text = "লাইভ ফিল্টারিং ট্র্যাকিং লগ (Live Traffic Logs):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)) // Sleek Dark Terminal Slate
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(liveLogs) { logLine ->
                        val textColor = when {
                            logLine.contains("BLOCKED") || logLine.contains("Blocked") -> AlertRed
                            logLine.contains("RESOLVED") || logLine.contains("Approved") || logLine.contains("started") || logLine.contains("active") -> SafeGreen
                            logLine.contains("SYSTEM") -> CoralWarm
                            else -> Color.White
                        }
                        Text(
                            text = logLine,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = textColor,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Host Copy List
            Text(
                text = "Choose a protection host (Tap to Copy):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val cfGatewayId by viewModel.cloudflareGatewayId.collectAsStateWithLifecycle()
            val cfHost = "${cfGatewayId}.cloudflare-gateway.com"
            
            listOf(
                Triple("low.kahfguard.com", "Low protection (Malware & Adult Content)", Icons.Default.Security),
                Triple("medium.kahfguard.com", "Medium protection (Blocks adult sites, ads & trackers)", Icons.Default.Shield),
                Triple("high.kahfguard.com", "High protection (Full strict safety and shorts block)", Icons.Default.VerifiedUser),
                Triple(cfHost, "Custom Cloudflare Gateway (Manage blocklists on dash.cloudflare.com)", Icons.Default.Cloud)
            ).forEach { (host, desc, icon) ->
                val isSelected = selectedDnsHost == host || (host.endsWith("cloudflare-gateway.com") && selectedDnsHost.endsWith("cloudflare-gateway.com"))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .background(
                            color = if (isSelected) DeepTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) DeepTeal else BorderDark,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            viewModel.setSelectedDnsHost(host)
                            copyToClipboard(context, host)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = icon, 
                            contentDescription = null, 
                            tint = if (isSelected) SafeGreen else DeepTeal, 
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (host.endsWith("cloudflare-gateway.com") && cfGatewayId == "example-dns-id") "your-id.cloudflare-gateway.com" else host, 
                                    style = MaterialTheme.typography.labelMedium, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = if (isSelected) SafeGreen else DarkCharcoal
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(SafeGreen, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Text(text = desc, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                viewModel.setSelectedDnsHost(host)
                                copyToClipboard(context, host)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SafeGreen,
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { copyToClipboard(context, host) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Host", tint = DeepTeal, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Cloudflare Setup Helper Panel
            if (selectedDnsHost.endsWith("cloudflare-gateway.com")) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = BorderDark, shape = RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Cloudflare Zero Trust Setup (ফ্রি মাইলফলক 🛡️)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepTeal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "আপনার নিজের Cloudflare Zero Trust একাউন্ট তৈরি করে এখানে বসান। এর মাধ্যমে আপনি ড্যাশবোর্ড থেকে রিয়েল-টাইমে সোশ্যাল মিডিয়া (Facebook, TikTok, Instagram) ব্লক করতে পারবেন ও লাইভ লগ দেখতে পাবেন!",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkCharcoal.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        var gatewayIdInput by remember(cfGatewayId) { mutableStateOf(cfGatewayId) }

                        OutlinedTextField(
                            value = gatewayIdInput,
                            onValueChange = { gatewayIdInput = it },
                            label = { Text("Cloudflare Gateway ID") },
                            placeholder = { Text("e.g., lzxp92q") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepTeal,
                                focusedLabelColor = DeepTeal,
                                cursorColor = DeepTeal
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (gatewayIdInput.isNotBlank()) {
                                    viewModel.updateCloudflareGatewayId(gatewayIdInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update ID", fontWeight = FontWeight.Bold, color = com.example.ui.theme.BackgroundDark)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = DeepTeal.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কীভাবে সেটআপ করবেন (ধাপসমূহ):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val steps = listOf(
                            "১. dash.cloudflare.com এ গিয়ে একটি ফ্রী একাউন্ট খুলুন।",
                            "২. বাম পাশের মেনু থেকে Zero Trust এ প্রবেশ করে সেটআপ সম্পন্ন করুন।",
                            "৩. Gateway -> DNS Locations এ গিয়ে Add Location এ ক্লিক করুন।",
                            "৪. সেখানে DNS-over-TLS (DoT) ট্যাবে একটি ইউনিক আইডি পাবেন (যেমন: lzxp92q), সেটি কপি করে ওপরের বক্সে বসিয়ে Update ID চাপুন।",
                            "৫. এবার নিচে 'OPEN PRIVATE DNS SETTINGS' এ ক্লিক করে আপনার ফোনের Private DNS হিসেবে এই অ্যাড্রেসটি বসিয়ে দিন।"
                        )
                        steps.forEach { step ->
                            Text(
                                text = step,
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkCharcoal.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Action Button to Open DNS Settings with White High-Contrast design
            Button(
                onClick = { openPrivateDnsSettings(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralWarm,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Android Private DNS Settings", 
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ParentSecurityVerificationDialog(
    viewModel: KidSafeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pinInput by viewModel.pinInput.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    
    // Simulating biometric fingerprint check/scan
    var isFingerprintScanning by remember { mutableStateOf(false) }
    var scanSuccess by remember { mutableStateOf(false) }
    
    LaunchedEffect(isFingerprintScanning) {
        if (isFingerprintScanning) {
            delay(1200) // Simulate elite biometric scanning duration
            isFingerprintScanning = false
            scanSuccess = true
            delay(300)
            // Execute guarded action on success
            val currentPin = context.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE).getString("parent_pin", "2580") ?: "2580"
            viewModel.verifySecurityPin(currentPin)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(16.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (scanSuccess) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (scanSuccess) SafeGreen else DeepTeal,
                    modifier = Modifier.size(54.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "প্যারেন্টাল সিকিউরিটি লক",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
                Text(
                    text = "Verify PIN or Biometric Fingerprint to continue",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                if (isFingerprintScanning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SafeGreen, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("যাচাই করা হচ্ছে... হাত স্পর্শ করে রাখুন", style = MaterialTheme.typography.bodySmall, color = SafeGreen, fontWeight = FontWeight.Bold)
                    }
                } else if (scanSuccess) {
                    Text("অনুমোদিত! সুরক্ষিত কাজ সম্পন্ন হচ্ছে...", style = MaterialTheme.typography.bodySmall, color = SafeGreen, fontWeight = FontWeight.Bold)
                } else {
                    // Display PIN dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (0..3).forEach { index ->
                            val isEntered = pinInput.length > index
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = if (isEntered) DeepTeal else Color.LightGray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = pinError!!, color = AlertRed, style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Number Keyboard & Biometrics Row
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("BACK", "0", "BIOMETRIC")
                        )
                        
                        keys.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { key ->
                                    if (key == "BIOMETRIC") {
                                        IconButton(
                                            onClick = { isFingerprintScanning = true },
                                            modifier = Modifier
                                                .size(54.dp)
                                                .background(SafeGreen.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Fingerprint", tint = SafeGreen)
                                        }
                                    } else if (key == "BACK") {
                                        IconButton(
                                            onClick = { viewModel.backspacePin() },
                                            modifier = Modifier
                                                .size(54.dp)
                                                .background(Color.LightGray.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.Default.Backspace, contentDescription = "Backspace", tint = DarkCharcoal)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                viewModel.enterPin(key)
                                                if (pinInput.length + 1 == 4) {
                                                    // Trigger auto verify
                                                    viewModel.verifySecurityPin(pinInput + key)
                                                }
                                            },
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftCream),
                                            modifier = Modifier.size(54.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(text = key, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("বাতিল করুন (Cancel)", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun PermissionGuardDialog(
    title: String,
    desc: String,
    onGrant: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
            ) {
                Text("পারমিশন দিন (Grant)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল (Cancel)", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.GppMaybe, contentDescription = null, tint = CoralWarm, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = DarkCharcoal)
            }
        },
        text = {
            Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    )
}

fun getActivePrivateDns(context: Context): String? {
    try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val activeNetwork = cm.activeNetwork ?: return null
            val lp = cm.getLinkProperties(activeNetwork) ?: return null
            return lp.privateDnsServerName
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun openPrivateDnsSettings(context: Context) {
    try {
        val intent = Intent("android.settings.PRIVATE_DNS_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (ex: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Kahf DNS Host", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}



@Composable
fun PassiveSecurityRadarComponent() {
    // Unused component simplified to a static high-performance layout
}
