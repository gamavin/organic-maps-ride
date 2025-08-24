package com.undefault.bitride.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalUserRepository @Inject constructor() {
    private val users = mutableMapOf<String, MutableList<String>>()

    suspend fun doesRoleExist(nikHash: String, role: String): Boolean {
        return users[nikHash]?.contains(role) == true
    }

    suspend fun createDriverProfile(nikHash: String): Boolean =
        createRoleIfAbsent(nikHash, "DRIVER")

    suspend fun createCustomerProfile(nikHash: String): Boolean =
        createRoleIfAbsent(nikHash, "CUSTOMER")

    private suspend fun createRoleIfAbsent(nikHash: String, role: String): Boolean {
        val roles = users.getOrPut(nikHash) { mutableListOf() }
        if (!roles.contains(role)) {
            roles.add(role)
        }
        return true
    }
}
