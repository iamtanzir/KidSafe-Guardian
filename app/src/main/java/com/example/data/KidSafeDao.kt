package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KidSafeDao {
    @Query("SELECT * FROM child_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<ChildProfile>>

    @Query("SELECT * FROM child_profiles WHERE id = :id")
    fun getProfileById(id: Long): Flow<ChildProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ChildProfile): Long

    @Update
    suspend fun updateProfile(profile: ChildProfile)

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE childId = :childId ORDER BY timestamp DESC")
    fun getLogsByChildId(childId: Long): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("SELECT * FROM time_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<TimeRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: TimeRequest)

    @Update
    suspend fun updateRequest(request: TimeRequest)

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()

    @Query("DELETE FROM time_requests")
    suspend fun clearRequests()
}
