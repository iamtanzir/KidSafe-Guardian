package com.example

import android.content.ComponentCallbacks2
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.KidSafeApp
import com.example.ui.KidSafeViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: KidSafeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                KidSafeApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh Device Admin shield registration state in real-time
        viewModel.checkAdminStatus()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Call HardwareRenderer.trimMemory via reflection on Android Q (API 29)+ as recommended by the system to clean up Skia font and resource caches
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val clazz = Class.forName("android.graphics.HardwareRenderer")
                val method = clazz.getMethod("trimMemory", Int::class.javaPrimitiveType)
                method.invoke(null, level)
            } catch (e: Throwable) {
                // Safeguard against platform or reflection differences
            }
        }
        // Co-operate with Android's modern memory manager to release cached graphics and pinned assets
        if (level >= TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_RUNNING_LOW || level == TRIM_MEMORY_RUNNING_CRITICAL) {
            System.gc() // Reclaim memory and prevent low-level ashmem/pinning deprecation warnings
        }
    }
}
