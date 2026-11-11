import re

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

import_str = """import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
"""
if 'import android.os.Build' not in content:
    content = content.replace('import android.provider.Settings', import_str + 'import android.provider.Settings')

perm_request_str = """@Composable
fun KidSafeApp(viewModel: KidSafeViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    
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
"""

content = re.sub(r'@Composable\nfun KidSafeApp\(viewModel: KidSafeViewModel\) \{\n    var activeTab by remember \{ mutableStateOf\(0\) \}', perm_request_str, content)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)

