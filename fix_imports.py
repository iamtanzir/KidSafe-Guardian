with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
seen_imports = set()
for line in lines:
    if line.startswith('import '):
        if line in seen_imports:
            continue
        seen_imports.add(line)
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.writelines(new_lines)
