package com.example.data

import kotlinx.coroutines.flow.Flow

class KidSafeRepository(private val dao: KidSafeDao) {
    val allProfiles: Flow<List<ChildProfile>> = dao.getAllProfiles()
    val allLogs: Flow<List<ActivityLog>> = dao.getAllLogs()
    val allRequests: Flow<List<TimeRequest>> = dao.getAllRequests()

    fun getProfileById(id: Long): Flow<ChildProfile?> = dao.getProfileById(id)
    fun getLogsByChild(childId: Long): Flow<List<ActivityLog>> = dao.getLogsByChildId(childId)

    suspend fun insertProfile(profile: ChildProfile): Long = dao.insertProfile(profile)
    suspend fun updateProfile(profile: ChildProfile) = dao.updateProfile(profile)

    suspend fun insertLog(log: ActivityLog) = dao.insertLog(log)
    suspend fun insertRequest(request: TimeRequest) = dao.insertRequest(request)
    suspend fun updateRequest(request: TimeRequest) = dao.updateRequest(request)

    suspend fun clearLogs() = dao.clearLogs()
    suspend fun clearRequests() = dao.clearRequests()
}
