package com.undefault.bitride.data.repository

/**
 * Menyediakan statistik profil pengguna untuk permintaan tumpangan.
 */
data class UserProfileStats(
    val totalRides: Int = 0,
    val successful: Int = 0,
    val uniquePartners: Int = 0,
    val positive: Int = 0,
    val negative: Int = 0,
    val askCancel: Int = 0
)

interface UserProfileRepository {
    suspend fun getActiveUserStats(): UserProfileStats
}
