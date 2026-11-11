with open('app/src/main/java/com/example/DelayActivity.kt', 'r') as f:
    content = f.read()

import_str = "import com.example.ui.AppTranslator\nimport com.example.ui.theme.DeepTeal"
content = content.replace("import com.example.ui.theme.DeepTeal", import_str)

content = content.replace('text = "Awareness Countdown"', 'text = AppTranslator.t("Awareness Countdown")')
content = content.replace('text = "Think for 7 seconds before opening social media. Do you really need it?"', 'text = AppTranslator.t("Think for 7 seconds before opening social media. Do you really need it?")')

with open('app/src/main/java/com/example/DelayActivity.kt', 'w') as f:
    f.write(content)
