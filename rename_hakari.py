import re

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

# Replace "হাকারি AI" and "হাকারি AI কম্প্যানিয়ন"
content = content.replace('হাকারি AI কম্প্যানিয়ন', 'Mr. Robot Companion')
content = content.replace('হাকারি AI', 'Mr. Robot')
content = content.replace('Hakari Ai', 'Mr. Robot')

# Change version display in DeveloperInfoCard
old_annotated = '''                withStyle(style = SpanStyle(color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)) {
                    append("Tanzir Ahmed")
                }'''

new_annotated = '''                withStyle(style = SpanStyle(color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)) {
                    append("Tanzir Ahmed")
                }
                withStyle(style = SpanStyle(color = Color.LightGray, fontWeight = FontWeight.Bold)) {
                    append("  |  Version: ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF45C493), fontWeight = FontWeight.Bold)) {
                    append("v2.01")
                }'''
content = content.replace(old_annotated, new_annotated)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)

