package com.example.calendario.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CampusRepository {
    val syncState: StateFlow<SyncState>
    val isFirebaseConfigured: Boolean
    val currentAuthorId: String?
    fun observeCache(): Flow<CampusCache>
    suspend fun saveProfile(name: String, email: String)
    suspend fun saveAlert(alert: CampusAlert): SaveOutcome
    suspend fun refresh()
    suspend fun synchronize()
    suspend fun triggerTestCrash()
}
