with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

import re
content = re.sub(r'shape\s*=\s*RoundedCornerShape\(percent\s*=\s*50\)', 'shape = RoundedCornerShape(24.dp)', content)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)
