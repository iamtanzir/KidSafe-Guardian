import re

with open('app/src/main/java/com/example/ui/KidSafeViewModel.kt', 'r') as f:
    content = f.read()

placeholder = """                // Dynamic simulation of Kahf Guard Safe DNS traffic
                if (_isKahfConnected.value) {
                    _blockedQueries.value += (2..6).random()
                    if ((1..10).random() > 6) {
                        _blockedAds.value += (1..2).random()
                    }
                    if ((1..10).random() > 8) {
                        _blockedInappropriate.value += 1
                    }
                    if ((1..10).random() > 9) {
                        _threatsBlocked.value += 1
                    }
                }"""

content = content.replace(placeholder, "")

with open('app/src/main/java/com/example/ui/KidSafeViewModel.kt', 'w') as f:
    f.write(content)
