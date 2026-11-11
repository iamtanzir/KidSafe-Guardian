import re

with open('app/src/main/java/com/example/ui/AppTranslator.kt', 'r') as f:
    content = f.read()

new_bn = """            "Welcome to Save BD Next Gen" to "Save BD Next Gen-এ স্বাগতম",
            "Let's set up your device for a safe digital experience." to "নিরাপদ ডিজিটাল অভিজ্ঞতার জন্য আপনার ডিভাইসটি সেটআপ করুন।",
            "Next" to "পরবর্তী",
            "Step 1: Network Protection" to "ধাপ ১: নেটওয়ার্ক সুরক্ষা",
            "We need to set up a secure local VPN to filter out adult content, malware, and ads." to "অ্যাডাল্ট সাইট, ম্যালওয়্যার এবং বিজ্ঞাপন ফিল্টার করতে আমাদের একটি নিরাপদ লোকাল ভিপিএন সেটআপ করতে হবে।",
            "Step 2: Reels & Shorts Blocker" to "ধাপ ২: রিলস ও শর্টস ব্লকার",
            "To detect and block addictive short videos, we need Accessibility permission. Please find 'Save BD Next Gen' in the downloaded apps list and enable it." to "আসক্তিকর শর্ট ভিডিও শনাক্ত ও ব্লক করতে অ্যাক্সেসিবিলিটি পারমিশন প্রয়োজন। দয়া করে ডাউনলোড করা অ্যাপের তালিকায় 'Save BD Next Gen' খুঁজে বের করে চালু করুন।",
            "Grant Permission" to "পারমিশন দিন",
            "Setup Complete" to "সেটআপ সম্পন্ন হয়েছে",
            "Finish Setup" to "সেটআপ শেষ করুন",
            "Skip for now" to "আপাতত স্কিপ করুন",
            "Select Language" to "ভাষা নির্বাচন করুন\""""

content = content.replace('"Select Language" to "ভাষা নির্বাচন করুন"', new_bn)

with open('app/src/main/java/com/example/ui/AppTranslator.kt', 'w') as f:
    f.write(content)
print("Updated AppTranslator.kt")
