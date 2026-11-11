with open('app/src/main/java/com/example/ui/AppTranslator.kt', 'r') as f:
    content = f.read()

# Fix the hours string
content = content.replace('"Digital Detox active. Phone locked for next \\${hours} hours."', '"Digital Detox active. Phone locked."')
content = content.replace('"Digital Detox active. Phone locked for next \${hours} hours."', '"Digital Detox active. Phone locked."')

with open('app/src/main/java/com/example/ui/AppTranslator.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/ui/KidSafeViewModel.kt', 'r') as f:
    vm = f.read()

vm = vm.replace('"Digital Detox active. Phone locked for next ${hours} hours."', '"Digital Detox active. Phone locked."')

with open('app/src/main/java/com/example/ui/KidSafeViewModel.kt', 'w') as f:
    f.write(vm)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'r') as f:
    screens = f.read()

# Fix the State access
old_state = "val lang = AppTranslator.currentLang.collectAsState().value"
new_state = "val lang = AppTranslator.currentLang.value"
screens = screens.replace(old_state, new_state)

with open('app/src/main/java/com/example/ui/KidSafeScreens.kt', 'w') as f:
    f.write(screens)

