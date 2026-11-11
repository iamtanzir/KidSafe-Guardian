import re

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

# For buttons with colors =, we can append shape and elevation.
def add_button_styles(match):
    prefix = match.group(1)
    return prefix + ',\n    shape = RoundedCornerShape(percent = 50),\n    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)'

# Search for colors = ButtonDefaults.buttonColors(...) and add after it
content = re.sub(r'(colors\s*=\s*ButtonDefaults\.buttonColors\([^)]*\))', add_button_styles, content)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)

