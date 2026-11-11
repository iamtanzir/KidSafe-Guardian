import os
import re

bengali_to_english = {
    "${customWhitelist.size} টি ডোমেইন অনুমোদিত আছে": "${customWhitelist.size} Domains Approved",
    "১০০% ফ্রী প্যারেন্টাল কন্ট্রোল": "100% Free Parental Control",
    "৪ ঘন্টার হার্ড-ডিটক্স লক শুরু করুন": "Start 4-Hour Hard Detox Lock",
    "৭-সেকেন্ড স্ক্রল সচেতনতা কাউন্টডাউন": "7-Second Scroll Awareness Countdown",
    "AI NSFW ইমেজ শিল্ড (On-Device Image Blur)": "AI NSFW Image Shield (On-Device Blur)",
    "Facebook Reels, Instagram Reels, YouTube Shorts এবং TikTok এর আসক্তিকর কনটেন্টগুলো ডিটেক্ট করে তাৎক্ষণিক ব্লক করবে।": "Detects and blocks addictive content from Facebook Reels, Instagram Reels, YouTube Shorts, and TikTok.",
    "Keep Me Out (ডিজিটাল ডিটক্স লক)": "Keep Me Out (Digital Detox Lock)",
    "Keep Me Out লক সক্রিয়": "Keep Me Out Lock Active",
    "Pomodoro সেশনে পড়াশোনা করুন অথবা ফোন লক রেখে ডিজিটাল ডিটক্স করুন।": "Study in Pomodoro sessions or detox with the phone locked.",
    "Save BD Next Gen অ্যাপটির প্রটেকশন শক্ত করতে সেটিংস নিয়ন্ত্রণ করুন।": "Configure settings to strengthen Save BD Next Gen protection.",
    "Save BD Next Gen আপনার ডিভাইস নিরাপদে মনিটর করছে।": "Save BD Next Gen is monitoring your device safely.",
    "অনলাইন সময় সচেতনতা": "Online Time Awareness",
    "অনুমতি ছাড়া অ্যাপ ডিলিট করা বন্ধ করে রাখুন": "Prevent app uninstallation without permission",
    "অনুমোদিত ডোমেনটি টাইপ করুন (যেমন: education.gov.bd)": "Enter approved domain (e.g., education.gov.bd)",
    "অনুমোদিত সাইট তালিকা (Whitelist)": "Approved Sites (Whitelist)",
    "অশ্লীল ডিজিটাল কনটেন্ট মানুষের আত্মাকে দুর্বল করে। স্ক্রিন ব্যবহারের সময় সর্বদা সচেতন থাকুন যে পরম করুণাময় আপনার দিকে দৃষ্টি রাখছেন।": "Inappropriate digital content weakens the soul. Stay aware while using screens.",
    "অশ্লীল সাইট ফিল্টার": "Adult Site Filter",
    "আইনগত সতর্কবার্তা ও কপিরাইট অধিকার (LEGAL NOTICE)": "LEGAL NOTICE & COPYRIGHT",
    "আনইনস্টল প্রতিরোধ (Device Admin)": "Uninstall Prevention (Device Admin)",
    "আপনার পুরো বাড়ির ওয়াইফাই নেটওয়ার্ক হালাল উপায়ে সুরক্ষিত রাখতে রাউটারে নিচের আইপিগুলো ডিএনএস হিসেবে সেট করুন:": "Set these IPs in your router as DNS to secure your entire home WiFi network:",
    "ইন্টারনেট সুরক্ষিত নয়": "Internet is NOT Secure",
    "উচ্চ সুরক্ষা": "High Protection",
    "এই সাইট, ডিজাইন ও কন্ট্রোল সিস্টেমের কোনো অংশ অনুমতি ছাড়া ক্লোন বা নকল করা সম্পূর্ণ বেআইনি। পাইরেসি করা হলে সাইবার সুরক্ষা আইন ও কপিরাইট আইনের অধীনে আইনি ব্যবস্থা নেয়া হবে।": "Any unauthorized cloning of this app is strictly illegal under Cyber Security Laws.",
    "একটি সেশন সিলেক্ট করে ফোকাস টাইমার শুরু করুন। এই সময়ে আসক্তিকর সোশ্যাল মিডিয়া অ্যাপস ব্লক থাকবে।": "Select a session and start focus timer. Addictive social media apps will be blocked.",
    "চোখ ও দৃষ্টির হেফাজত": "Vision & Gaze Protection",
    "ডিএনএস ফিল্টারিং লেভেল": "DNS Filtering Level",
    "ডিজিটাল ডিটক্স শেষ হতে সময় বাকি আছে:": "Time remaining for Digital Detox:",
    "ডিজিটাল লাইফ ব্যালেন্স করতে আপনার সার্বক্ষণিক পথপ্রদর্শক": "Your constant guide for a balanced digital life",
    "ডিজিটাল সিপাহী 🛡️": "Digital Soldier 🛡️",
    "ডিটক্স ও স্টাডি ফোকাস মোড": "Detox & Study Focus Mode",
    "ডিটক্স ফোকাস": "Detox Focus",
    "ডিভাইস প্রটেকশন চালু করতে উপরে স্পর্শ করুন": "Tap above to activate device protection",
    "দৈনিক ১ ঘন্টার বেশি অনর্থক সোশ্যাল স্ক্রল এড়িয়ে চলুন। ফোকাস টাইমার ব্যবহার করে পড়াশোনায় মনোযোগ বৃদ্ধি করুন।": "Avoid scrolling for more than 1 hour daily. Use focus timer to study.",
    "নতুন সাইট অনুমোদন করুন": "Approve New Site",
    "নিরাপদ ইন্টারনেট ও রিলস লক সক্রিয়": "Safe Internet & Reels Lock Active",
    "নেক্সট জেন ক্যাডেট 🎖️": "Next Gen Cadet 🎖️",
    "পড়াশোনায় মন দিন এবং নিজেকে ডিজিটাল আসক্তি থেকে মুক্ত করুন।": "Focus on studying and free yourself from digital addiction.",
    "পরবর্তী প্রজন্মের নির্দেশিকা": "Next Generation Guidelines",
    "প্যারেন্টাল ও ডিএনএস সেটিংস": "Parental & DNS Settings",
    "ফোকাস মোড বন্ধ করুন": "Stop Focus Mode",
    "ফোকাস সেশন": "Focus Session",
    "বাতিল": "Cancel",
    "বিজ্ঞাপন ব্লক": "Ads Blocked",
    "ব্রাউজার বা যেকোনো অ্যাপ্লিকেশনে অনুপযুক্ত বা অশ্লীল ছবি সামনে আসলে অন-ডিভাইস AI দ্বারা তাৎক্ষণিকভাবে ব্লার করে দৃষ্টির হেফাজত করবে।": "Inappropriate images in any app will be instantly blurred by on-device AI.",
    "ব্লকার্স": "Blockers",
    "মনোযোগ অত্যন্ত বিক্ষিপ্ত হলে পরবর্তী ৪ ঘন্টার জন্য নিজেকে ফোন লকআউট করে দিন। কোনো বাইপাস ছাড়া এটি ফোন সম্পূর্ণ ব্লক রাখবে।": "Lock yourself out for 4 hours if extremely distracted. No bypass allowed.",
    "মাঝারি": "Medium",
    "✓ মাঝারি মোড: ক্ষতিকর কন্টেন্ট ব্লক থাকবে, সাধারণ ব্রাউজিং সচল থাকবে।": "✓ Medium Mode: Harmful content blocked, normal browsing active.",
    "মাস্টার গার্ডিয়ান 👑": "Master Guardian 👑",
    "মোট ট্রাফিক": "Total Traffic",
    "মৌলিক": "Basic",
    "ম্যালওয়্যার প্রতিরোধ": "Malware Prevented",
    "যেকোনো অতি-আসক্তিকর অ্যাপ চালু করার পূর্বে ৭ সেকেন্ড অপেক্ষা করতে বাধ্য করবে, যা আপনার অবচেতন আসক্তি প্রতিরোধে অত্যন্ত সহায়ক।": "Forces a 7-second wait before opening addictive apps.",
    "যোগ করুন": "Add",
    "রাউটার ম্যানুয়াল ডিএনএস সেটআপ": "Router Manual DNS Setup",
    "রিসেট করুন (Bypass)": "Reset (Bypass)",
    "লাইভ ফিল্টারিং তথ্য": "Live Filtering Data",
    "শর্ট-ফর্ম ভিডিও ব্লকার (Reels/Shorts)": "Short-Form Video Blocker (Reels/Shorts)",
    "শর্টস ও কনটেন্ট ব্লক অপশন": "Shorts & Content Block Options",
    "✓ সাধারণ মোড: ম্যালওয়্যার এবং ফিশিং সাইট ব্লক করে ইন্টারনেট নিরাপদ রাখবে।": "✓ Basic Mode: Blocks malware and phishing sites.",
    "সুরক্ষা": "Security",
    "সেটিংস": "Settings",
    "সোশ্যাল মিডিয়ার ক্ষতিকর শর্ট-ফর্ম আসক্তি এবং নজর হেফাজতের টুলস": "Tools to protect against social media short-form addiction.",
    "📖 স্টাডি ফোকাস টাইমার (Pomodoro)": "📖 Study Focus Timer (Pomodoro)",
    "✓ হাই মোড: অ্যাডাল্ট সাইট, পর্নোগ্রাফি, জুয়া, গেম এবং ক্ষতিকর ইউটিউব কনটেন্ট ১০০% ব্লক।": "✓ High Mode: 100% blocks adult sites, gambling, and harmful content.",
    
    # Delay Activity
    "সচেতনতা কাউন্টডাউন": "Awareness Countdown",
    "সামাজিক যোগাযোগ মাধ্যম খোলার আগে ৭ সেকেন্ড ভাবুন। এটি কি সত্যিই আপনার এখন প্রয়োজন?": "Think for 7 seconds before opening social media. Do you really need it?",
    
    # ViewModel messages
    "৭ সেকেন্ড স্ক্রল কাউন্টডাউন সক্রিয়। যেকোনো ক্ষতিকর অ্যাপ চালু করার আগে আপনাকে সচেতন করবে।": "7-second scroll countdown is active. You will be alerted before opening any harmful apps.",
    "AI NSFW শিল্ড চালু হয়েছে। আপনার দৃষ্টির হেফাজত করতে ছবিগুলো রিয়েল-টাইমে ব্লার করা হবে।": "AI NSFW Shield is active. Images will be blurred in real-time.",
    "আসসালামু আলাইকুম! আজকের স্ক্রিন টাইম এবং নজর হেফাজত নিয়ন্ত্রণে চমৎকার অবদান রাখছ। শুভকামনা রইলো!": "Hello! You are doing great in controlling screen time today. Best wishes!",
    "ডিজিটাল ডিটক্স (Keep Me Out) সক্রিয় করা হয়েছে। পরবর্তী ${hours} ঘন্টা ফোন ব্যবহার লক থাকবে।": "Digital Detox active. Phone locked for next ${hours} hours.",
    "ডিজিটাল ডিটক্স সমাপ্ত হয়েছে। স্বাগতম!": "Digital Detox completed. Welcome back!",
    "দারুণ! রিলস ও শর্টস ব্লক করা হয়েছে। সময় অপচয় বন্ধ করতে এটি আপনাকে সাহায্য করবে।": "Great! Reels and Shorts are blocked. This will save your time.",
    "সতর্কতা: AI NSFW শিল্ড বন্ধ করা হয়েছে। চোখের হেফাজত ঈমানের অংশ।": "Warning: AI NSFW Shield is disabled.",
    "সতর্কতা: রিলস ব্লকার বন্ধ করা হয়েছে। অনর্থক স্ক্রল করা থেকে বিরত থাকুন।": "Warning: Reels blocker is disabled. Avoid useless scrolling.",
    "স্টাডি ফোকাস মোড শুরু হয়েছে! এই সময়ে সব রকমের আসক্তিকর অ্যাপ সম্পূর্ণ লক থাকবে।": "Study Focus Mode started! All addictive apps will be locked.",
    "স্টাডি ফোকাস মোড সমাপ্ত। চমৎকার মনোযোগের জন্য আপনি ২৫টি অরা (Aura) পয়েন্ট অর্জন করেছেন!": "Study Focus Mode finished. You earned 25 Aura points!"
}

for root, _, files in os.walk('app/src/main/java/com/example'):
    for f in files:
        if f.endswith('.kt'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
                
            for bn, en in bengali_to_english.items():
                content = content.replace(bn, en)
                
            with open(path, 'w', encoding='utf-8') as file:
                file.write(content)

print("Translation done.")
