package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: Long,
    val appName: String,
    val type: String, // "Web", "Social", "Gaming", "Education", "Flagged"
    val durationMinutes: Int,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFlagged: Boolean = false
)
