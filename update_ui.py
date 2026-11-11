import re

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

# Make corners rounder
content = content.replace('RoundedCornerShape(16.dp)', 'RoundedCornerShape(24.dp)')
content = content.replace('RoundedCornerShape(12.dp)', 'RoundedCornerShape(20.dp)')
content = content.replace('RoundedCornerShape(8.dp)', 'RoundedCornerShape(16.dp)')

# Increase elevation to look like Tailwind shadow-lg
content = content.replace('defaultElevation = 0.dp', 'defaultElevation = 2.dp')
content = content.replace('defaultElevation = 2.dp', 'defaultElevation = 8.dp')
content = content.replace('defaultElevation = 4.dp', 'defaultElevation = 10.dp')

# Add subtle glassmorphism or white background
# Instead of Color.White for Card container color, we can use a soft shade.
# But White is fine for light themes.
# Let's adjust button corners
content = re.sub(r'shape\s*=\s*RoundedCornerShape\(\d+\.dp\)', r'shape = RoundedCornerShape(percent = 50)', content, count=0)
# Wait, changing all shapes to percent 50 might break Cards!
# I shouldn't replace all `shape = RoundedCornerShape`.

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)

