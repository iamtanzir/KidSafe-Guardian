with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

sig = """@Composable
fun KidSafeApp(viewModel: KidSafeViewModel) {
    var activeTab by remember { mutableStateOf(0) }"""

new_sig = """@Composable
fun KidSafeApp(viewModel: KidSafeViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AppTranslator.init(context)
    }
"""

content = content.replace(sig, new_sig)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)
