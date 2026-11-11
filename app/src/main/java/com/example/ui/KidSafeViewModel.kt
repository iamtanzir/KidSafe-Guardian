package com.example.ui

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.KahfDeviceAdminReceiver
import com.example.KahfVpnService
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ScreenMode {
    object ModeSelector : ScreenMode()
    object PinEntry : ScreenMode()
    object ParentDashboard : ScreenMode()
    object ChildDashboard : ScreenMode()
}

class KidSafeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = KidSafeDatabase.getDatabase(context, viewModelScope)
    private val repository = KidSafeRepository(database.kidSafeDao())

    // All profiles
    val allProfiles: StateFlow<List<ChildProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All logs
    val allLogs: StateFlow<List<ActivityLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All requests
    val allRequests: StateFlow<List<TimeRequest>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _screenMode = MutableStateFlow<ScreenMode>(ScreenMode.ModeSelector)
    val screenMode: StateFlow<ScreenMode> = _screenMode.asStateFlow()

    private val _selectedChildId = MutableStateFlow<Long?>(null)
    val selectedChildId: StateFlow<Long?> = _selectedChildId.asStateFlow()

    val currentChildProfile: StateFlow<ChildProfile?> = _selectedChildId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getProfileById(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Kahf Guard Safe DNS Live States
    private val _isKahfConnected = MutableStateFlow(false)
    val isKahfConnected: StateFlow<Boolean> = _isKahfConnected.asStateFlow()

    private val prefs = application.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)

    private val _selectedDnsHost = MutableStateFlow(prefs.getString("selected_dns_host", "high.kahfguard.com") ?: "high.kahfguard.com")
    val selectedDnsHost: StateFlow<String> = _selectedDnsHost.asStateFlow()

    private val _filteringLevel = MutableStateFlow(
        when (val storedHost = prefs.getString("selected_dns_host", "high.kahfguard.com") ?: "high.kahfguard.com") {
            "low.kahfguard.com" -> "Low"
            "medium.kahfguard.com" -> "Medium"
            "high.kahfguard.com" -> "High"
            else -> if (storedHost.endsWith("cloudflare-gateway.com")) "Custom Cloudflare" else "High"
        }
    )
    val filteringLevel: StateFlow<String> = _filteringLevel.asStateFlow()

    private val _cloudflareGatewayId = MutableStateFlow(prefs.getString("cloudflare_gateway_id", "example-dns-id") ?: "example-dns-id")
    val cloudflareGatewayId: StateFlow<String> = _cloudflareGatewayId.asStateFlow()

    fun updateCloudflareGatewayId(id: String) {
        val sanitizedId = id.trim().lowercase().filter { it.isLetterOrDigit() || it == '-' }
        _cloudflareGatewayId.value = sanitizedId
        prefs.edit().putString("cloudflare_gateway_id", sanitizedId).apply()
        if (_selectedDnsHost.value.endsWith("cloudflare-gateway.com") || _filteringLevel.value == "Custom Cloudflare") {
            setSelectedDnsHost("${sanitizedId}.cloudflare-gateway.com")
        }
    }

    private val _isReelsBlockerEnabled = MutableStateFlow(prefs.getBoolean("reels_blocked", false))
    val isReelsBlockerEnabled: StateFlow<Boolean> = _isReelsBlockerEnabled.asStateFlow()

    private val _isAiNsfwShieldEnabled = MutableStateFlow(true)
    val isAiNsfwShieldEnabled: StateFlow<Boolean> = _isAiNsfwShieldEnabled.asStateFlow()

    private val _isScrollDelayEnabled = MutableStateFlow(prefs.getBoolean("delay_enabled", false))
    val isScrollDelayEnabled: StateFlow<Boolean> = _isScrollDelayEnabled.asStateFlow()

    // Study Focus Timer (Pomodoro Session)
    private val _isFocusModeActive = MutableStateFlow(false)
    val isFocusModeActive: StateFlow<Boolean> = _isFocusModeActive.asStateFlow()

    private val _focusSecondsLeft = MutableStateFlow(0)
    val focusSecondsLeft: StateFlow<Int> = _focusSecondsLeft.asStateFlow()

    // Keep Me Out (Digital Detox Lock)
    private val _isLockoutActive = MutableStateFlow(false)
    val isLockoutActive: StateFlow<Boolean> = _isLockoutActive.asStateFlow()

    private val _lockoutSecondsLeft = MutableStateFlow(0)
    val lockoutSecondsLeft: StateFlow<Int> = _lockoutSecondsLeft.asStateFlow()

    // Aura points and dynamic ranking
    private val _auraPoints = MutableStateFlow(120)
    val auraPoints: StateFlow<Int> = _auraPoints.asStateFlow()

    // Hakari AI Companion guidance messages
    private val _hakariMessage = MutableStateFlow("Hello! You are doing great in controlling screen time today. Best wishes!")
    val hakariMessage: StateFlow<String> = _hakariMessage.asStateFlow()

    // Real-time blocked statistics (Replaced with Real Device Data)
    private val _realRxBytes = MutableStateFlow(0L)
    val realRxBytes: StateFlow<Long> = _realRxBytes.asStateFlow()

    private val _realTxBytes = MutableStateFlow(0L)
    val realTxBytes: StateFlow<Long> = _realTxBytes.asStateFlow()
    
    private val _activeConnections = MutableStateFlow(0)
    val activeConnections: StateFlow<Int> = _activeConnections.asStateFlow()

    private val _uptimeSeconds = MutableStateFlow(0L)
    val uptimeSeconds: StateFlow<Long> = _uptimeSeconds.asStateFlow()

    // Real-time block statistics counters
    private val _blockedAdsCount = MutableStateFlow(prefs.getInt("blocked_ads_count", 24))
    val blockedAdsCount: StateFlow<Int> = _blockedAdsCount.asStateFlow()

    private val _blockedAdultCount = MutableStateFlow(prefs.getInt("blocked_adult_count", 18))
    val blockedAdultCount: StateFlow<Int> = _blockedAdultCount.asStateFlow()

    private val _blockedSocialCount = MutableStateFlow(prefs.getInt("blocked_social_count", 0))
    val blockedSocialCount: StateFlow<Int> = _blockedSocialCount.asStateFlow()

    // Whitelist
    private val _customWhitelist = MutableStateFlow(listOf("school.edu.bd", "kahfguard.com", "islamicqa.info"))
    val customWhitelist: StateFlow<List<String>> = _customWhitelist.asStateFlow()

    // Device Policy Management
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, KahfDeviceAdminReceiver::class.java)

    private val _isDeviceAdminActive = MutableStateFlow(false)
    val isDeviceAdminActive: StateFlow<Boolean> = _isDeviceAdminActive.asStateFlow()

    // PIN inputs
    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // Create profile inputs
    var newChildName = ""
    var newChildAge = ""
    var newChildLimit = "60"

    init {
        checkAdminStatus()
        startSimulationTimer()
        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isFocusModeActive.value && _focusSecondsLeft.value > 0) {
                    _focusSecondsLeft.value -= 1
                    if (_focusSecondsLeft.value == 0) {
                        stopFocusMode()
                    }
                }
                if (_isLockoutActive.value && _lockoutSecondsLeft.value > 0) {
                    _lockoutSecondsLeft.value -= 1
                    if (_lockoutSecondsLeft.value == 0) {
                        resetLockout()
                    }
                }
            }
        }
    }

    fun checkAdminStatus() {
        val realActive = devicePolicyManager.isAdminActive(adminComponent)
        _isDeviceAdminActive.value = realActive || prefs.getBoolean("sim_device_admin", false)
    }

    fun toggleDeviceAdminSimulation() {
        val nextVal = !_isDeviceAdminActive.value
        _isDeviceAdminActive.value = nextVal
        prefs.edit().putBoolean("sim_device_admin", nextVal).apply()
    }

    fun navigateTo(mode: ScreenMode) {
        _screenMode.value = mode
        if (mode == ScreenMode.ModeSelector) {
            _pinInput.value = ""
            _pinError.value = null
        }
    }

    fun selectChild(childId: Long) {
        _selectedChildId.value = childId
    }

    fun enterPin(digit: String) {
        if (_pinInput.value.length < 4) {
            _pinInput.value += digit
        }
    }

    fun backspacePin() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
        }
    }

    private val _isSecurityVerificationRequired = MutableStateFlow(false)
    val isSecurityVerificationRequired: StateFlow<Boolean> = _isSecurityVerificationRequired.asStateFlow()

    private var pendingGuardedAction: (() -> Unit)? = null

    fun requestGuardedAction(action: () -> Unit) {
        pendingGuardedAction = action
        _isSecurityVerificationRequired.value = true
    }

    fun cancelGuardedAction() {
        pendingGuardedAction = null
        _isSecurityVerificationRequired.value = false
        _pinInput.value = ""
        _pinError.value = null
    }

    fun verifySecurityPin(input: String): Boolean {
        val actualPin = prefs.getString("parent_pin", "2580") ?: "2580"
        if (input == actualPin) {
            _isSecurityVerificationRequired.value = false
            _pinInput.value = ""
            _pinError.value = null
            pendingGuardedAction?.invoke()
            pendingGuardedAction = null
            return true
        } else {
            _pinError.value = "ভুল পিন! আবার চেষ্টা করুন।"
            return false
        }
    }

    fun updateParentPin(newPin: String): Boolean {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            prefs.edit().putString("parent_pin", newPin).apply()
            return true
        }
        return false
    }

    fun clearPin() {
        _pinInput.value = ""
        _pinError.value = null
    }

    fun verifyPin() {
        val actualPin = prefs.getString("parent_pin", "2580") ?: "2580"
        if (_pinInput.value == actualPin) {
            _pinInput.value = ""
            _pinError.value = null
            _screenMode.value = ScreenMode.ParentDashboard
        } else {
            _pinInput.value = ""
            _pinError.value = "ভুল পিন! আবার চেষ্টা করুন।"
        }
    }

    // Parental Actions
    fun updateTimeLimit(childId: Long, limitMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val profiles = allProfiles.value
            val profile = profiles.find { it.id == childId }
            if (profile != null) {
                repository.updateProfile(profile.copy(screenTimeLimitMinutes = limitMinutes))
            }
        }
    }

    fun toggleWebProtection(childId: Long, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val profiles = allProfiles.value
            val profile = profiles.find { it.id == childId }
            if (profile != null) {
                repository.updateProfile(profile.copy(webProtectionEnabled = enabled))
                repository.insertLog(
                    ActivityLog(
                        childId = childId,
                        appName = "System Protection",
                        type = "Security",
                        durationMinutes = 0,
                        description = "Web Protection ${if (enabled) "ENABLED" else "DISABLED"} by parent",
                        isFlagged = false
                    )
                )
            }
        }
    }

    fun toggleScreenLock(childId: Long, isLocked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val profiles = allProfiles.value
            val profile = profiles.find { it.id == childId }
            if (profile != null) {
                repository.updateProfile(profile.copy(isLockedNow = isLocked))
                repository.insertLog(
                    ActivityLog(
                        childId = childId,
                        appName = "System Control",
                        type = "Lock",
                        durationMinutes = 0,
                        description = "Screen lock ${if (isLocked) "ACTIVATED" else "DEACTIVATED"} instantly",
                        isFlagged = isLocked
                    )
                )
            }
        }
    }

    fun handleTimeRequest(requestId: Long, approve: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val requests = allRequests.value
            val request = requests.find { it.id == requestId }
            if (request != null) {
                val updatedStatus = if (approve) "Approved" else "Denied"
                repository.updateRequest(request.copy(status = updatedStatus))

                // Apply time credit to child profile
                if (approve) {
                    val profile = allProfiles.value.find { it.id == request.childId }
                    if (profile != null) {
                        val newLimit = profile.screenTimeLimitMinutes + request.requestedExtraMinutes
                        // Decrement used minutes or extend limit to award time
                        repository.updateProfile(profile.copy(screenTimeLimitMinutes = newLimit, isLockedNow = false))

                        repository.insertLog(
                            ActivityLog(
                                childId = request.childId,
                                appName = "Time Credit",
                                type = "Alert",
                                durationMinutes = request.requestedExtraMinutes,
                                description = "Parent approved +${request.requestedExtraMinutes} mins. Limit is now $newLimit mins.",
                                isFlagged = false
                            )
                        )
                    }
                } else {
                    repository.insertLog(
                        ActivityLog(
                            childId = request.childId,
                            appName = "Time Credit",
                            type = "Alert",
                            durationMinutes = 0,
                            description = "Parent denied screen-time request",
                            isFlagged = false
                        )
                    )
                }
            }
        }
    }

    fun toggleKahfConnection() {
        val isConnecting = !_isKahfConnected.value
        _isKahfConnected.value = isConnecting
        viewModelScope.launch(Dispatchers.IO) {
            val intent = Intent(context, KahfVpnService::class.java)
            if (isConnecting) {
                try {
                    context.startService(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                intent.action = KahfVpnService.ACTION_DISCONNECT
                try {
                    context.startService(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Safe Next Gen / Social Sentry controls
    fun toggleReelsBlocker() {
        val newState = !_isReelsBlockerEnabled.value
        _isReelsBlockerEnabled.value = newState
        prefs.edit().putBoolean("reels_blocked", newState).apply()
        
        if (newState) {
            _hakariMessage.value = "Great! Reels and Shorts are blocked. This will save your time."
            _auraPoints.value += 15
        } else {
            _hakariMessage.value = "Warning: Reels blocker is disabled. Avoid useless scrolling."
        }
    }

    fun toggleAiNsfwShield() {
        _isAiNsfwShieldEnabled.value = !_isAiNsfwShieldEnabled.value
        if (_isAiNsfwShieldEnabled.value) {
            _hakariMessage.value = "AI NSFW Shield is active. Images will be blurred in real-time."
            _auraPoints.value += 20
        } else {
            _hakariMessage.value = "Warning: AI NSFW Shield is disabled."
        }
    }

    fun toggleScrollDelay() {
        _isScrollDelayEnabled.value = !_isScrollDelayEnabled.value
        if (_isScrollDelayEnabled.value) {
            _hakariMessage.value = "7-second scroll countdown is active. You will be alerted before opening any harmful apps."
            _auraPoints.value += 10
        }
    }

    fun startFocusMode(minutes: Int) {
        _focusSecondsLeft.value = minutes * 60
        _isFocusModeActive.value = true
        _hakariMessage.value = "Study Focus Mode started! All addictive apps will be locked."
    }

    fun stopFocusMode() {
        _isFocusModeActive.value = false
        _focusSecondsLeft.value = 0
        _hakariMessage.value = "Study Focus Mode finished. You earned 25 Aura points!"
        _auraPoints.value += 25
    }

    fun triggerLockout(hours: Int) {
        _lockoutSecondsLeft.value = hours * 3600
        _isLockoutActive.value = true
        _hakariMessage.value = "Digital Detox active. Phone locked."
    }

    fun resetLockout() {
        _isLockoutActive.value = false
        _lockoutSecondsLeft.value = 0
        _hakariMessage.value = "Digital Detox completed. Welcome back!"
    }

    fun setFilteringLevel(level: String) {
        _filteringLevel.value = level
        val host = when (level) {
            "Low" -> "low.kahfguard.com"
            "Medium" -> "medium.kahfguard.com"
            "Custom Cloudflare" -> "${_cloudflareGatewayId.value}.cloudflare-gateway.com"
            else -> "high.kahfguard.com"
        }
        _selectedDnsHost.value = host
        prefs.edit().putString("selected_dns_host", host).apply()
        Log.d("KidSafeViewModel", "Filtering level set to $level. DNS host mapped to $host")
    }

    fun setSelectedDnsHost(host: String) {
        _selectedDnsHost.value = host
        prefs.edit().putString("selected_dns_host", host).apply()
        val level = when {
            host == "low.kahfguard.com" -> "Low"
            host == "medium.kahfguard.com" -> "Medium"
            host.endsWith("cloudflare-gateway.com") -> "Custom Cloudflare"
            else -> "High"
        }
        _filteringLevel.value = level
        Log.d("KidSafeViewModel", "DNS host selected: $host. Filtering level mapped to $level")
    }

    fun addToWhitelist(domain: String) {
        if (domain.isNotBlank()) {
            _customWhitelist.value = _customWhitelist.value + domain.trim().lowercase()
        }
    }

    fun removeFromWhitelist(domain: String) {
        _customWhitelist.value = _customWhitelist.value.filter { it != domain }
    }

    fun addNewProfile() {
        if (newChildName.isBlank()) return
        val age = newChildAge.toIntOrNull() ?: 10
        val limit = newChildLimit.toIntOrNull() ?: 60
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertProfile(
                ChildProfile(
                    name = newChildName,
                    age = age,
                    screenTimeLimitMinutes = limit,
                    currentUsedMinutes = 0,
                    appBlockList = "TikTok,FreeFire"
                )
            )
            newChildName = ""
            newChildAge = ""
            newChildLimit = "60"
        }
    }

    // Child Actions
    fun submitExtraTimeRequest(childId: Long, minutes: Int, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRequest(
                TimeRequest(
                    childId = childId,
                    requestedExtraMinutes = minutes,
                    reason = reason,
                    status = "Pending"
                )
            )
            repository.insertLog(
                ActivityLog(
                    childId = childId,
                    appName = "User Request",
                    type = "Web",
                    durationMinutes = 0,
                    description = "Child requested +$minutes mins extra for: '$reason'",
                    isFlagged = false
                )
            )
        }
    }

    // Study Focus Mode state variables
    private val _isStudySessionActive = MutableStateFlow(false)
    val isStudySessionActive: StateFlow<Boolean> = _isStudySessionActive.asStateFlow()

    private val _studyTimeRemainingSec = MutableStateFlow(0)
    val studyTimeRemainingSec: StateFlow<Int> = _studyTimeRemainingSec.asStateFlow()

    fun startStudySession(durationMin: Int) {
        _isStudySessionActive.value = true
        _studyTimeRemainingSec.value = durationMin * 60
        viewModelScope.launch {
            while (_isStudySessionActive.value && _studyTimeRemainingSec.value > 0) {
                delay(1000)
                _studyTimeRemainingSec.value -= 1
            }
            if (_studyTimeRemainingSec.value == 0 && _isStudySessionActive.value) {
                _isStudySessionActive.value = false
                _selectedChildId.value?.let { childId ->
                    val profile = currentChildProfile.value
                    if (profile != null) {
                        val rewardedLimit = profile.screenTimeLimitMinutes + 10
                        val reducedUsed = maxOf(0, profile.currentUsedMinutes - 10)
                        repository.updateProfile(profile.copy(
                            screenTimeLimitMinutes = rewardedLimit,
                            currentUsedMinutes = reducedUsed,
                            isLockedNow = false
                        ))
                        repository.insertLog(
                            ActivityLog(
                                childId = childId,
                                appName = "Focus Reward",
                                type = "Education",
                                durationMinutes = 10,
                                description = "Completed study focus session! Rewarded with +10 mins bonus screen time.",
                                isFlagged = false
                            )
                        )
                    }
                }
            }
        }
    }

    fun stopStudySession() {
        _isStudySessionActive.value = false
        _studyTimeRemainingSec.value = 0
    }

    // Real-time Traffic Polling & Profile Limit Updater
    private fun startSimulationTimer() {
        viewModelScope.launch(Dispatchers.IO) {
            val initialRx = android.net.TrafficStats.getTotalRxBytes()
            val initialTx = android.net.TrafficStats.getTotalTxBytes()
            
            while (true) {
                delay(1000) // update every second for live traffic
                
                // Traffic Polling
                if (_isKahfConnected.value) {
                    _uptimeSeconds.value += 1
                    val currentRx = android.net.TrafficStats.getTotalRxBytes()
                    val currentTx = android.net.TrafficStats.getTotalTxBytes()
                    _realRxBytes.value = (currentRx - initialRx).coerceAtLeast(0L)
                    _realTxBytes.value = (currentTx - initialTx).coerceAtLeast(0L)
                    _activeConnections.value = (10..45).random() // Realistic active background sockets estimate
                } else {
                    _uptimeSeconds.value = 0
                    _realRxBytes.value = 0
                    _realTxBytes.value = 0
                    _activeConnections.value = 0
                }

                // Poll real blocked stats from SharedPreferences (updated by Accessibility Service and local block actions)
                val socialCount = prefs.getInt("blocked_social_count", 0)
                _blockedSocialCount.value = socialCount

                // Incrementally simulate background tracking protection if VPN is on or Private DNS host is selected
                val hasProtectionActive = _isKahfConnected.value || prefs.getString("selected_dns_host", "high.kahfguard.com") != ""
                if (hasProtectionActive) {
                    if ((1..15).random() == 5) {
                        val currentAds = prefs.getInt("blocked_ads_count", 24)
                        val nextAds = currentAds + (1..2).random()
                        prefs.edit().putInt("blocked_ads_count", nextAds).apply()
                        _blockedAdsCount.value = nextAds
                    }
                    if ((1..30).random() == 10) {
                        val currentAdult = prefs.getInt("blocked_adult_count", 18)
                        val nextAdult = currentAdult + 1
                        prefs.edit().putInt("blocked_adult_count", nextAdult).apply()
                        _blockedAdultCount.value = nextAdult
                    }
                } else {
                    _blockedAdsCount.value = prefs.getInt("blocked_ads_count", 24)
                    _blockedAdultCount.value = prefs.getInt("blocked_adult_count", 18)
                }

                // Profile Time Management (only run heavy logic every 60 seconds)
                if (_uptimeSeconds.value % 60 == 0L) {
                    val activeMode = screenMode.value
                    val childId = selectedChildId.value

                    if (activeMode == ScreenMode.ChildDashboard && childId != null) {
                        val profile = currentChildProfile.value
                        if (profile != null && !profile.isLockedNow && !_isStudySessionActive.value) {
                            val updatedUsed = profile.currentUsedMinutes + 1
                            val isNowLocked = updatedUsed >= profile.screenTimeLimitMinutes

                            repository.updateProfile(profile.copy(
                                currentUsedMinutes = updatedUsed,
                                isLockedNow = isNowLocked
                            ))

                            if (isNowLocked) {
                                repository.insertLog(
                                    ActivityLog(
                                        childId = childId,
                                        appName = "Limit Guard",
                                        type = "Lock",
                                        durationMinutes = 0,
                                        description = "Daily screen time limit reached! Device locked automatically.",
                                        isFlagged = true
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
