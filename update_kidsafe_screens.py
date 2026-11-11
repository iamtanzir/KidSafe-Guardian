import re

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

# Add logic for SetupWizard in KidSafeApp
setup_logic = """@Composable
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
    }"""

content = re.sub(
    r'@Composable\nfun KidSafeApp\(viewModel: KidSafeViewModel\) \{.*?AppTranslator.init\(context\)\n    \}',
    setup_logic,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)
print("Updated KidSafeScreens.kt")
