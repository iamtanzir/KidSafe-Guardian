import re

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

# Make buttons fully rounded like rounded-full in Tailwind
content = re.sub(r'Button\(\s*onClick\s*=\s*(.*?),\s*modifier\s*=\s*(.*?),\s*colors\s*=\s*(.*?)\s*\)', 
                 r'Button(onClick = \1, modifier = \2, colors = \3, shape = RoundedCornerShape(percent = 50), elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp))', 
                 content)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)

