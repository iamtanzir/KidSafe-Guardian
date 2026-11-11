package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_requests")
data class TimeRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: Long,
    val requestedExtraMinutes: Int,
    val reason: String,
    val status: String = "Pending", // "Pending", "Approved", "Denied"
    val timestamp: Long = System.currentTimeMillis()
)
