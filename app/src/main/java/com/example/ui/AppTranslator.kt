package com.example.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import android.content.Context

object AppTranslator {
    var currentLang = mutableStateOf("en")
    
    fun init(context: Context) {
        val prefs = context.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
        currentLang.value = prefs.getString("app_lang", "en") ?: "en"
    }

    fun setLang(context: Context, lang: String) {
        currentLang.value = lang
        context.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)
            .edit().putString("app_lang", lang).apply()
    }
    
    private val dict = mapOf(
        "bn" to mapOf(
            "Security" to "সুরক্ষা",
            "Settings" to "সেটিংস",
            "Blockers" to "ব্লকার্স",
            "Safe Internet & Reels Lock Active" to "নিরাপদ ইন্টারনেট ও রিলস লক সক্রিয়",
            "Next Generation Guidelines" to "পরবর্তী প্রজন্মের নির্দেশিকা",
            "Safe Next Gen is monitoring your device safely." to "Safe Next Gen আপনার ডিভাইস নিরাপদে মনিটর করছে।",
            "Tap above to activate device protection" to "ডিভাইস প্রটেকশন চালু করতে উপরে স্পর্শ করুন",
            "Configure settings to strengthen Safe Next Gen protection." to "Safe Next Gen অ্যাপটির প্রটেকশন শক্ত করতে সেটিংস নিয়ন্ত্রণ করুন।",
            "Change Parental PIN" to "প্যারেন্টাল সিকিউরিটি পিন পরিবর্তন",
            "Verify/update your 4-digit security PIN to prevent unauthorized changes" to "অননুমোদিত পরিবর্তন রোধ করতে আপনার ৪-ডিজিট সিকিউরিটি পিন আপডেট করুন",
            "Save PIN" to "সংরক্ষণ করুন",
            "PIN updated successfully!" to "পিন সফলভাবে আপডেট হয়েছে!",
            "Invalid PIN format!" to "ভুল পিন ফরম্যাট!",
            "4-digit PIN required!" to "৪-ডিজিটের পিন আবশ্যক!",
            "Live Filtering Data" to "লাইভ ফিল্টারিং তথ্য",
            "Total Traffic" to "মোট ট্রাফিক",
            "Ads Blocked" to "বিজ্ঞাপন ও ট্র্যাকার ব্লক",
            "Adult Site Filter" to "অশ্লীল সাইট ফিল্টার",
            "Reels / Shorts Blocked" to "রিলস ও শর্টস ব্লক সংখ্যা",
            "Cyber Security Stats (Real-Time)" to "সাইবার সিকিউরিটি পরিসংখ্যান (রিয়েল-টাইম)",
            "Malware Prevented" to "ম্যালওয়্যার প্রতিরোধ",
            "Shorts & Content Block Options" to "শর্টস ও কনটেন্ট ব্লক অপশন",
            "Short-Form Video Blocker (Reels/Shorts)" to "শর্ট-ফর্ম ভিডিও ব্লকার (Reels/Shorts)",
            "Detects and blocks addictive content from Facebook Reels, Instagram Reels, YouTube Shorts, and TikTok." to "Facebook Reels, Instagram Reels, YouTube Shorts এবং TikTok এর আসক্তিকর কনটেন্টগুলো ডিটেক্ট করে তাৎক্ষণিক ব্লক করবে।",
            "Vision & Gaze Protection" to "চোখ ও দৃষ্টির হেফাজত",
            "AI NSFW Image Shield (On-Device Blur)" to "AI NSFW ইমেজ শিল্ড (On-Device Image Blur)",
            "Inappropriate images in any app will be instantly blurred by on-device AI." to "ব্রাউজার বা যেকোনো অ্যাপ্লিকেশনে অনুপযুক্ত বা অশ্লীল ছবি সামনে আসলে অন-ডিভাইস AI দ্বারা তাৎক্ষণিকভাবে ব্লার করে দৃষ্টির হেফাজত করবে।",
            "Online Time Awareness" to "অনলাইন সময় সচেতনতা",
            "7-Second Scroll Awareness Countdown" to "৭-সেকেন্ড স্ক্রল সচেতনতা কাউন্টডাউন",
            "Forces a 7-second wait before opening addictive apps." to "যেকোনো অতি-আসক্তিকর অ্যাপ চালু করার পূর্বে ৭ সেকেন্ড অপেক্ষা করতে বাধ্য করবে, যা আপনার অবচেতন আসক্তি প্রতিরোধে অত্যন্ত সহায়ক।",
            "Detox Focus" to "ডিটক্স ফোকাস",
            "Detox & Study Focus Mode" to "ডিটক্স ও স্টাডি ফোকাস মোড",
            "📖 Study Focus Timer (Pomodoro)" to "📖 স্টাডি ফোকাস টাইমার (Pomodoro)",
            "Select a session and start focus timer. Addictive social media apps will be blocked." to "একটি সেশন সিলেক্ট করে ফোকাস টাইমার শুরু করুন। এই সময়ে আসক্তিকর সোশ্যাল মিডিয়া অ্যাপস ব্লক থাকবে।",
            "Keep Me Out (Digital Detox Lock)" to "Keep Me Out (ডিজিটাল ডিটক্স লক)",
            "Lock yourself out for 4 hours if extremely distracted. No bypass allowed." to "মনোযোগ অত্যন্ত বিক্ষিপ্ত হলে পরবর্তী ৪ ঘন্টার জন্য নিজেকে ফোন লকআউট করে দিন। কোনো বাইপাস ছাড়া এটি ফোন সম্পূর্ণ ব্লক রাখবে।",
            "Start 4-Hour Hard Detox Lock" to "৪ ঘন্টার হার্ড-ডিটক্স লক শুরু করুন",
            "Parental & DNS Settings" to "প্যারেন্টাল ও ডিএনএস সেটিংস",
            "DNS Filtering Level" to "ডিএনএস ফিল্টারিং লেভেল",
            "High Protection" to "উচ্চ সুরক্ষা",
            "Medium" to "মাঝারি",
            "Basic" to "মৌলিক",
            "✓ High Mode: 100% blocks adult sites, gambling, and harmful content." to "✓ হাই মোড: অ্যাডাল্ট সাইট, পর্নোগ্রাফি, জুয়া, গেম এবং ক্ষতিকর ইউটিউব কনটেন্ট ১০০% ব্লক।",
            "✓ Medium Mode: Harmful content blocked, normal browsing active." to "✓ মাঝারি মোড: ক্ষতিকর কন্টেন্ট ব্লক থাকবে, সাধারণ ব্রাউজিং সচল থাকবে।",
            "✓ Basic Mode: Blocks malware and phishing sites." to "✓ সাধারণ মোড: ম্যালওয়্যার এবং ফিশিং সাইট ব্লক করে ইন্টারনেট নিরাপদ রাখবে।",
            "Uninstall Prevention (Device Admin)" to "আনইনস্টল প্রতিরোধ (Device Admin)",
            "Prevent app uninstallation without permission" to "অনুমতি ছাড়া অ্যাপ ডিলিট করা বন্ধ করে রাখুন",
            "Approved Sites (Whitelist)" to "অনুমোদিত সাইট তালিকা (Whitelist)",
            "Router Manual DNS Setup" to "রাউটার ম্যানুয়াল ডিএনএস সেটআপ",
            "Set these IPs in your router as DNS to secure your entire home WiFi network:" to "আপনার পুরো বাড়ির ওয়াইফাই নেটওয়ার্ক হালাল উপায়ে সুরক্ষিত রাখতে রাউটারে নিচের আইপিগুলো ডিএনএস হিসেবে সেট করুন:",
            "Approve New Site" to "নতুন সাইট অনুমোদন করুন",
            "Enter approved domain (e.g., education.gov.bd)" to "অনুমোদিত ডোমেনটি টাইপ করুন (যেমন: education.gov.bd)",
            "Add" to "যোগ করুন",
            "Cancel" to "বাতিল",
            "LEGAL NOTICE & COPYRIGHT" to "আইনগত সতর্কবার্তা ও কপিরাইট অধিকার (LEGAL NOTICE)",
            "Any unauthorized cloning of this app is strictly illegal under Cyber Security Laws." to "এই সাইট, ডিজাইন ও কন্ট্রোল সিস্টেমের কোনো অংশ অনুমতি ছাড়া ক্লোন বা নকল করা সম্পূর্ণ বেআইনি। পাইরেসি করা হলে সাইবার সুরক্ষা আইন ও কপিরাইট আইনের অধীনে আইনি ব্যবস্থা নেয়া হবে।",
            "100% Free Parental Control" to "১০০% ফ্রী প্যারেন্টাল কন্ট্রোল",
            "Internet is NOT Secure" to "ইন্টারনেট সুরক্ষিত নয়",
            "Your constant guide for a balanced digital life" to "ডিজিটাল লাইফ ব্যালেন্স করতে আপনার সার্বক্ষণিক পথপ্রদর্শক",
            "Tools to protect against social media short-form addiction." to "সোশ্যাল মিডিয়ার ক্ষতিকর শর্ট-ফর্ম আসক্তি এবং নজর হেফাজতের টুলস",
            "Awareness Countdown" to "সচেতনতা কাউন্টডাউন",
            "Think for 7 seconds before opening social media. Do you really need it?" to "সামাজিক যোগাযোগ মাধ্যম খোলার আগে ৭ সেকেন্ড ভাবুন। এটি কি সত্যিই আপনার এখন প্রয়োজন?",
            "7-second scroll countdown is active. You will be alerted before opening any harmful apps." to "৭ সেকেন্ড স্ক্রল কাউন্টডাউন সক্রিয়। যেকোনো ক্ষতিকর অ্যাপ চালু করার আগে আপনাকে সচেতন করবে।",
            "AI NSFW Shield is active. Images will be blurred in real-time." to "AI NSFW শিল্ড চালু হয়েছে। আপনার দৃষ্টির হেফাজত করতে ছবিগুলো রিয়েল-টাইমে ব্লার করা হবে।",
            "Great! Reels and Shorts are blocked. This will save your time." to "দারুণ! রিলস ও শর্টস ব্লক করা হয়েছে। সময় অপচয় বন্ধ করতে এটি আপনাকে সাহায্য করবে।",
            "Warning: AI NSFW Shield is disabled." to "সতর্কতা: AI NSFW শিল্ড বন্ধ করা হয়েছে। চোখের হেফাজত ঈমানের অংশ।",
            "Warning: Reels blocker is disabled. Avoid useless scrolling." to "সতর্কতা: রিলস ব্লকার বন্ধ করা হয়েছে। অনর্থক স্ক্রল করা থেকে বিরত থাকুন।",
            "Study Focus Mode started! All addictive apps will be locked." to "স্টাডি ফোকাস মোড শুরু হয়েছে! এই সময়ে সব রকমের আসক্তিকর অ্যাপ সম্পূর্ণ লক থাকবে।",
            "Digital Detox active. Phone locked." to "ডিজিটাল ডিটক্স (Keep Me Out) সক্রিয় করা হয়েছে। পরবর্তী %s ঘন্টা ফোন ব্যবহার লক থাকবে।",
            "Digital Detox completed. Welcome back!" to "ডিজিটাল ডিটক্স সমাপ্ত হয়েছে। স্বাগতম!",
            "Digital Soldier 🛡️" to "ডিজিটাল সিপাহী 🛡️",
            "Next Gen Cadet 🎖️" to "নেক্সট জেন ক্যাডেট 🎖️",
            "Master Guardian 👑" to "মাস্টার গার্ডিয়ান 👑",
            "Focus on studying and free yourself from digital addiction." to "পড়াশোনায় মন দিন এবং নিজেকে ডিজিটাল আসক্তি থেকে মুক্ত করুন।",
            "Avoid scrolling for more than 1 hour daily. Use focus timer to study." to "দৈনিক ১ ঘন্টার বেশি অনর্থক সোশ্যাল স্ক্রল এড়িয়ে চলুন। ফোকাস টাইমার ব্যবহার করে পড়াশোনায় মনোযোগ বৃদ্ধি করুন।",
            "Inappropriate digital content weakens the soul. Stay aware while using screens." to "অশ্লীল ডিজিটাল কনটেন্ট মানুষের আত্মাকে দুর্বল করে। স্ক্রিন ব্যবহারের সময় সর্বদা সচেতন থাকুন যে পরম করুণাময় আপনার দিকে দৃষ্টি রাখছেন।",
            "Language" to "ভাষা",
                        "Welcome to Safe Next Gen" to "Safe Next Gen-এ স্বাগতম",
            "Let's set up your device for a safe digital experience." to "নিরাপদ ডিজিটাল অভিজ্ঞতার জন্য আপনার ডিভাইসটি সেটআপ করুন।",
            "Next" to "পরবর্তী",
            "Step 1: Network Protection" to "ধাপ ১: নেটওয়ার্ক সুরক্ষা",
            "We need to set up a secure local VPN to filter out adult content, malware, and ads." to "অ্যাডাল্ট সাইট, ম্যালওয়্যার এবং বিজ্ঞাপন ফিল্টার করতে আমাদের একটি নিরাপদ লোকাল ভিপিএন সেটআপ করতে হবে।",
            "Step 2: Reels & Shorts Blocker" to "ধাপ ২: রিলস ও শর্টস ব্লকার",
            "To detect and block addictive short videos, we need Accessibility permission. Please find 'Safe Next Gen' in the downloaded apps list and enable it." to "আসক্তিকর শর্ট ভিডিও শনাক্ত ও ব্লক করতে অ্যাক্সেসিবিলিটি পারমিশন প্রয়োজন। দয়া করে ডাউনলোড করা অ্যাপের তালিকায় 'Safe Next Gen' খুঁজে বের করে চালু করুন।",
            "Grant Permission" to "পারমিশন দিন",
            "Setup Complete" to "সেটআপ সম্পন্ন হয়েছে",
            "Finish Setup" to "সেটআপ শেষ করুন",
            "Skip for now" to "আপাতত স্কিপ করুন",
            "Select Language" to "ভাষা নির্বাচন করুন"
        ),
        "zh" to mapOf(
            "Security" to "安全",
            "Settings" to "设置",
            "Blockers" to "拦截器",
            "Safe Internet & Reels Lock Active" to "安全上网和短视频锁定已激活",
            "Next Generation Guidelines" to "下一代指南",
            "Safe Next Gen is monitoring your device safely." to "Safe Next Gen 正在安全监控您的设备。",
            "Tap above to activate device protection" to "点击上方激活设备保护",
            "Configure settings to strengthen Safe Next Gen protection." to "配置设置以加强保护。",
            "Live Filtering Data" to "实时过滤数据",
            "Total Traffic" to "总流量",
            "Ads Blocked" to "已拦截广告",
            "Adult Site Filter" to "成人网站过滤器",
            "Malware Prevented" to "防止恶意软件",
            "Shorts & Content Block Options" to "短视频和内容拦截选项",
            "Short-Form Video Blocker (Reels/Shorts)" to "短视频拦截器 (Reels/Shorts)",
            "Detects and blocks addictive content from Facebook Reels, Instagram Reels, YouTube Shorts, and TikTok." to "检测并拦截让人上瘾的短视频内容。",
            "Vision & Gaze Protection" to "视力保护",
            "AI NSFW Image Shield (On-Device Blur)" to "AI 图像模糊保护",
            "Inappropriate images in any app will be instantly blurred by on-device AI." to "应用中的不良图片会被 AI 实时模糊。",
            "Online Time Awareness" to "在线时间意识",
            "7-Second Scroll Awareness Countdown" to "7秒防沉迷倒计时",
            "Forces a 7-second wait before opening addictive apps." to "打开容易上瘾的应用前强制等待 7 秒。",
            "Detox Focus" to "专注模式",
            "Detox & Study Focus Mode" to "排毒与学习专注模式",
            "📖 Study Focus Timer (Pomodoro)" to "📖 学习专注计时器 (番茄钟)",
            "Select a session and start focus timer. Addictive social media apps will be blocked." to "选择时间并开始专注。社交应用将被锁定。",
            "Keep Me Out (Digital Detox Lock)" to "保持专注 (数字排毒锁定)",
            "Lock yourself out for 4 hours if extremely distracted. No bypass allowed." to "如果极度分心，请锁定手机 4 小时。无法绕过。",
            "Start 4-Hour Hard Detox Lock" to "开始 4 小时强制锁定",
            "Parental & DNS Settings" to "家长控制与 DNS 设置",
            "DNS Filtering Level" to "DNS 过滤级别",
            "High Protection" to "高级保护",
            "Medium" to "中级",
            "Basic" to "基础",
            "✓ High Mode: 100% blocks adult sites, gambling, and harmful content." to "✓ 高级模式：100% 拦截不良内容。",
            "Uninstall Prevention (Device Admin)" to "防止卸载 (设备管理员)",
            "Prevent app uninstallation without permission" to "防止在未经许可的情况下卸载应用",
            "Approved Sites (Whitelist)" to "白名单",
            "Language" to "语言",
            "Select Language" to "选择语言"
        ),
        "de" to mapOf(
            "Security" to "Sicherheit",
            "Settings" to "Einstellungen",
            "Blockers" to "Blocker",
            "Safe Internet & Reels Lock Active" to "Sicheres Internet & Reels-Sperre aktiv",
            "Next Generation Guidelines" to "Richtlinien der nächsten Generation",
            "Safe Next Gen is monitoring your device safely." to "Ihr Gerät wird sicher überwacht.",
            "Tap above to activate device protection" to "Tippen Sie hier, um den Schutz zu aktivieren",
            "Configure settings to strengthen Safe Next Gen protection." to "Einstellungen konfigurieren, um den Schutz zu verstärken.",
            "Live Filtering Data" to "Live-Filterdaten",
            "Total Traffic" to "Gesamter Datenverkehr",
            "Ads Blocked" to "Blockierte Werbung",
            "Adult Site Filter" to "Filter für nicht jugendfreie Websites",
            "Malware Prevented" to "Malware verhindert",
            "Language" to "Sprache",
            "Select Language" to "Sprache auswählen"
        ),
        "ru" to mapOf(
            "Security" to "Безопасность",
            "Settings" to "Настройки",
            "Blockers" to "Блокировщики",
            "Safe Internet & Reels Lock Active" to "Безопасный интернет активен",
            "Language" to "Язык",
            "Select Language" to "Выберите язык"
        ),
        "ko" to mapOf(
            "Security" to "보안",
            "Settings" to "설정",
            "Blockers" to "차단기",
            "Language" to "언어",
            "Select Language" to "언어 선택"
        )
    )

    fun t(key: String): String {
        val lang = currentLang.value
        if (lang == "en") return key
        return dict[lang]?.get(key) ?: key
    }
}
