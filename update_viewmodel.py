import re

with open('app/src/main/java/com/example/ui/KidSafeViewModel.kt', 'r') as f:
    content = f.read()

# Replace initialization
old_init = """    private val _isReelsBlockerEnabled = MutableStateFlow(true)
    val isReelsBlockerEnabled: StateFlow<Boolean> = _isReelsBlockerEnabled.asStateFlow()

    private val _isAiNsfwShieldEnabled = MutableStateFlow(true)
    val isAiNsfwShieldEnabled: StateFlow<Boolean> = _isAiNsfwShieldEnabled.asStateFlow()

    private val _isScrollDelayEnabled = MutableStateFlow(false)
    val isScrollDelayEnabled: StateFlow<Boolean> = _isScrollDelayEnabled.asStateFlow()"""

new_init = """    private val prefs = application.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)

    private val _isReelsBlockerEnabled = MutableStateFlow(prefs.getBoolean("reels_blocked", false))
    val isReelsBlockerEnabled: StateFlow<Boolean> = _isReelsBlockerEnabled.asStateFlow()

    private val _isAiNsfwShieldEnabled = MutableStateFlow(true)
    val isAiNsfwShieldEnabled: StateFlow<Boolean> = _isAiNsfwShieldEnabled.asStateFlow()

    private val _isScrollDelayEnabled = MutableStateFlow(prefs.getBoolean("delay_enabled", false))
    val isScrollDelayEnabled: StateFlow<Boolean> = _isScrollDelayEnabled.asStateFlow()"""
content = content.replace(old_init, new_init)

# Replace toggles
old_reels = """    fun toggleReelsBlocker() {
        _isReelsBlockerEnabled.value = !_isReelsBlockerEnabled.value
        if (_isReelsBlockerEnabled.value) {
            _hakariMessage.value = "দারুণ! রিলস ও শর্টস ব্লক করা হয়েছে। সময় অপচয় বন্ধ করতে এটি আপনাকে সাহায্য করবে।"
            _auraPoints.value += 15
        } else {
            _hakariMessage.value = "সতর্কতা: রিলস ব্লকার বন্ধ করা হয়েছে। অনর্থক স্ক্রল করা থেকে বিরত থাকুন।"
        }
    }"""
new_reels = """    fun toggleReelsBlocker() {
        val newState = !_isReelsBlockerEnabled.value
        _isReelsBlockerEnabled.value = newState
        prefs.edit().putBoolean("reels_blocked", newState).apply()
        
        if (newState) {
            _hakariMessage.value = "দারুণ! রিলস ও শর্টস ব্লক করা হয়েছে। সময় অপচয় বন্ধ করতে এটি আপনাকে সাহায্য করবে।"
            _auraPoints.value += 15
        } else {
            _hakariMessage.value = "সতর্কতা: রিলস ব্লকার বন্ধ করা হয়েছে। অনর্থক স্ক্রল করা থেকে বিরত থাকুন।"
        }
    }"""
content = content.replace(old_reels, new_reels)

old_delay = """    fun toggleScrollDelay() {
        _isScrollDelayEnabled.value = !_isScrollDelayEnabled.value
        if (_isScrollDelayEnabled.value) {
            _hakariMessage.value = "৭ সেকেন্ড স্ক্রল কাউন্টডাউন সক্রিয়। যেকোনো ক্ষতিকর অ্যাপ চালু করার আগে আপনাকে সচেতন করবে।"
            _auraPoints.value += 10
        } else {
            _hakariMessage.value = "সতর্কতা: ৭ সেকেন্ড স্ক্রল কাউন্টডাউন বন্ধ করা হয়েছে।"
        }
    }"""
new_delay = """    fun toggleScrollDelay() {
        val newState = !_isScrollDelayEnabled.value
        _isScrollDelayEnabled.value = newState
        prefs.edit().putBoolean("delay_enabled", newState).apply()
        
        if (newState) {
            _hakariMessage.value = "৭ সেকেন্ড স্ক্রল কাউন্টডাউন সক্রিয়। যেকোনো ক্ষতিকর অ্যাপ চালু করার আগে আপনাকে সচেতন করবে।"
            _auraPoints.value += 10
        } else {
            _hakariMessage.value = "সতর্কতা: ৭ সেকেন্ড স্ক্রল কাউন্টডাউন বন্ধ করা হয়েছে।"
        }
    }"""
content = content.replace(old_delay, new_delay)

with open('app/src/main/java/com/example/ui/KidSafeViewModel.kt', 'w') as f:
    f.write(content)
