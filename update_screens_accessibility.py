import re

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

# Add import for Settings and ComponentName
imports = """import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import com.example.KidSafeAccessibilityService
"""
content = content.replace('import com.example.KahfDeviceAdminReceiver', imports + 'import com.example.KahfDeviceAdminReceiver')

# Add helper function for checking accessibility service
helper_fn = """
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

"""
content = content.replace('@Composable\nfun KidSafeApp(viewModel: KidSafeViewModel) {', helper_fn + '@Composable\nfun KidSafeApp(viewModel: KidSafeViewModel) {')

# Find the calls to viewModel.toggleReelsBlocker() and viewModel.toggleScrollDelay() inside SaveBDBlockersTab
old_reels_call = """                        Switch(
                            checked = isReelsBlocked,
                            onCheckedChange = { viewModel.toggleReelsBlocker() },"""
new_reels_call = """                        Switch(
                            checked = isReelsBlocked,
                            onCheckedChange = { 
                                if (!isReelsBlocked && !isAccessibilityServiceEnabled(context, KidSafeAccessibilityService::class.java)) {
                                    Toast.makeText(context, "Please enable Mr. Robot Accessibility Service", Toast.LENGTH_LONG).show()
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }
                                viewModel.toggleReelsBlocker() 
                            },"""
content = content.replace(old_reels_call, new_reels_call)

old_delay_call = """                        Switch(
                            checked = isScrollDelayEnabled,
                            onCheckedChange = { viewModel.toggleScrollDelay() },"""
new_delay_call = """                        Switch(
                            checked = isScrollDelayEnabled,
                            onCheckedChange = { 
                                if (!isScrollDelayEnabled && !isAccessibilityServiceEnabled(context, KidSafeAccessibilityService::class.java)) {
                                    Toast.makeText(context, "Please enable Mr. Robot Accessibility Service", Toast.LENGTH_LONG).show()
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }
                                viewModel.toggleScrollDelay() 
                            },"""
content = content.replace(old_delay_call, new_delay_call)

# Ensure context is available in SaveBDBlockersTab
if 'val context = LocalContext.current' not in content.split('fun SaveBDBlockersTab')[1].split('LazyColumn')[0]:
    content = content.replace('val isScrollDelayEnabled by viewModel.isScrollDelayEnabled.collectAsStateWithLifecycle()', 'val isScrollDelayEnabled by viewModel.isScrollDelayEnabled.collectAsStateWithLifecycle()\n    val context = LocalContext.current')

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)
