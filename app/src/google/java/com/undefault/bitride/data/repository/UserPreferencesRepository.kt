package com.undefault.bitride.data.repository

import android.content.Context
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    constructor(context: Context) : this(DataStoreRepository(context))

    private val rolesCache = mutableSetOf<String>()

    suspend fun saveLoggedInUser(nikHash: String, role: String) {
        val normalizedRole = role.uppercase()
        if (rolesCache.isEmpty()) {
            rolesCache.addAll(dataStoreRepository.rolesFlow.firstOrNull() ?: emptyList())
        }
        rolesCache.add(normalizedRole)
        dataStoreRepository.saveLoggedInUser(nikHash, normalizedRole)
    }

    suspend fun getLoggedInUser(): LoggedInData? {
        val nik = dataStoreRepository.nikHashFlow.firstOrNull()
        val roles = if (rolesCache.isEmpty()) {
            val stored = dataStoreRepository.rolesFlow.firstOrNull() ?: emptyList()
            rolesCache.addAll(stored)
            stored
        } else {
            rolesCache.toList()
        }
        return if (nik != null) LoggedInData(nik, roles) else null
    }

    suspend fun clearLoggedInUser() {
        dataStoreRepository.clearLoggedInUser()
        rolesCache.clear()
    }
}
