import re
import os

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

# Add AppTranslator import
if 'import com.example.ui.AppTranslator' not in content:
    content = content.replace('import com.example.ui.theme.*', 'import com.example.ui.theme.*\nimport com.example.ui.AppTranslator')

# Replace known strings in KidSafeScreens
known_strings = [
    "${customWhitelist.size} Domains Approved",
    "100% Free Parental Control",
    "Start 4-Hour Hard Detox Lock",
    "7-Second Scroll Awareness Countdown",
    "AI NSFW Image Shield (On-Device Blur)",
    "Detects and blocks addictive content from Facebook Reels, Instagram Reels, YouTube Shorts, and TikTok.",
    "Keep Me Out (Digital Detox Lock)",
    "Keep Me Out Lock Active",
    "Study in Pomodoro sessions or detox with the phone locked.",
    "Configure settings to strengthen Save BD Next Gen protection.",
    "Save BD Next Gen is monitoring your device safely.",
    "Online Time Awareness",
    "Prevent app uninstallation without permission",
    "Enter approved domain (e.g., education.gov.bd)",
    "Approved Sites (Whitelist)",
    "Inappropriate digital content weakens the soul. Stay aware while using screens.",
    "Adult Site Filter",
    "LEGAL NOTICE & COPYRIGHT",
    "Uninstall Prevention (Device Admin)",
    "Set these IPs in your router as DNS to secure your entire home WiFi network:",
    "Internet is NOT Secure",
    "High Protection",
    "Any unauthorized cloning of this app is strictly illegal under Cyber Security Laws.",
    "Select a session and start focus timer. Addictive social media apps will be blocked.",
    "Vision & Gaze Protection",
    "DNS Filtering Level",
    "Time remaining for Digital Detox:",
    "Your constant guide for a balanced digital life",
    "Digital Soldier 🛡️",
    "Detox & Study Focus Mode",
    "Detox Focus",
    "Tap above to activate device protection",
    "Avoid scrolling for more than 1 hour daily. Use focus timer to study.",
    "Approve New Site",
    "Safe Internet & Reels Lock Active",
    "Next Gen Cadet 🎖️",
    "Focus on studying and free yourself from digital addiction.",
    "Next Generation Guidelines",
    "Parental & DNS Settings",
    "Stop Focus Mode",
    "Focus Session",
    "Cancel",
    "Ads Blocked",
    "Inappropriate images in any app will be instantly blurred by on-device AI.",
    "Blockers",
    "Lock yourself out for 4 hours if extremely distracted. No bypass allowed.",
    "Medium",
    "✓ Medium Mode: Harmful content blocked, normal browsing active.",
    "Master Guardian 👑",
    "Total Traffic",
    "Basic",
    "Malware Prevented",
    "Forces a 7-second wait before opening addictive apps.",
    "Add",
    "Router Manual DNS Setup",
    "Reset (Bypass)",
    "Live Filtering Data",
    "Short-Form Video Blocker (Reels/Shorts)",
    "Shorts & Content Block Options",
    "✓ Basic Mode: Blocks malware and phishing sites.",
    "Security",
    "Settings",
    "Tools to protect against social media short-form addiction.",
    "📖 Study Focus Timer (Pomodoro)",
    "✓ High Mode: 100% blocks adult sites, gambling, and harmful content."
]

# We need to use re.sub carefully for exact strings inside quotes
for s in known_strings:
    # Escape special characters
    safe_s = re.escape(s)
    # Replace `"string"` with `AppTranslator.t("string")`
    # Make sure we don't double wrap if already wrapped
    content = re.sub(f'"{safe_s}"', f'AppTranslator.t("{s}")', content)

# Inject LanguageCard into Settings Tab
language_card_ui = """
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
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
                    val currentLangName = when(AppTranslator.currentLang.collectAsStateWithLifecycle().value) {
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
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                        ) {
                            Text(currentLangName)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("English") }, onClick = { AppTranslator.setLang(context, "en"); expanded = false })
                            DropdownMenuItem(text = { Text("বাংলা (Bengali)") }, onClick = { AppTranslator.setLang(context, "bn"); expanded = false })
                            DropdownMenuItem(text = { Text("中文 (Chinese)") }, onClick = { AppTranslator.setLang(context, "zh"); expanded = false })
                            DropdownMenuItem(text = { Text("Deutsch (German)") }, onClick = { AppTranslator.setLang(context, "de"); expanded = false })
                            DropdownMenuItem(text = { Text("Русский (Russian)") }, onClick = { AppTranslator.setLang(context, "ru"); expanded = false })
                            DropdownMenuItem(text = { Text("한국어 (Korean)") }, onClick = { AppTranslator.setLang(context, "ko"); expanded = false })
                        }
                    }
                }
            }
        }
"""
content = content.replace('item {\n            FreeForParentsCard()\n        }', 'item {\n            FreeForParentsCard()\n        }\n' + language_card_ui)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)

print("Translations injected.")
