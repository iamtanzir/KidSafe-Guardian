import re

# Fix AppTranslator.kt
with open('app/src/main/java/com/example/ui/AppTranslator.kt', 'r') as f:
    content = f.read()

content = content.replace('${hours}', '\\${hours}')

with open('app/src/main/java/com/example/ui/AppTranslator.kt', 'w') as f:
    f.write(content)

# Fix KidSafeScreens.kt
with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    screens = f.read()

old_lang_code = """                    val currentLangName = when(AppTranslator.currentLang.collectAsStateWithLifecycle().value) {"""

new_lang_code = """                    val lang = AppTranslator.currentLang.collectAsState().value
                    val currentLangName = when(lang) {"""

screens = screens.replace(old_lang_code, new_lang_code)

if 'import androidx.compose.runtime.collectAsState' not in screens:
    screens = screens.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport androidx.compose.runtime.collectAsState')

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(screens)

