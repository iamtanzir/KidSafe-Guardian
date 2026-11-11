with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    content = f.read()

# Fix 1: The broken DeepTeal else SoftCream
content = content.replace(
    '''colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected),
    shape = RoundedCornerShape(percent = 50),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp) DeepTeal else SoftCream
                                ),''',
    '''colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) DeepTeal else SoftCream
                                ),'''
)

# Fix 2: Remove the duplicated parameters
bad_str = ''',
    shape = RoundedCornerShape(percent = 50),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)'''

content = content.replace(bad_str, '')

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(content)

