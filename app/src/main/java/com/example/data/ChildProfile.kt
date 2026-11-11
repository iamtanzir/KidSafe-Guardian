package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profiles")
data class ChildProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val screenTimeLimitMinutes: Int = 120,
    val currentUsedMinutes: Int = 0,
    val parentPin: String = "1234",
    val isActive: Boolean = true,
    val appBlockList: String = "TikTok,Instagram", // Comma-separated blocked apps
    val isLockedNow: Boolean = false,
    val webProtectionEnabled: Boolean = true
)
