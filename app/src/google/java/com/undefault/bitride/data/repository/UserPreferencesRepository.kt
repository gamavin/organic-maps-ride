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

    suspend fun saveLoggedInUser(nikHash: String, role: String) {
        val roles = dataStoreRepository.rolesFlow.firstOrNull()?.toMutableSet() ?: mutableSetOf()
        roles.add(role)
        dataStoreRepository.saveLoggedInUser(nikHash, roles.toList())
    }

    suspend fun getLoggedInUser(): LoggedInData? {
        val nik = dataStoreRepository.nikHashFlow.firstOrNull()
        val roles = dataStoreRepository.rolesFlow.firstOrNull() ?: emptyList()
        return if (nik != null) LoggedInData(nik, roles) else null
    }

    suspend fun clearLoggedInUser() {
        dataStoreRepository.clearLoggedInUser()
    }
}
