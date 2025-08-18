package com.undefault.bitride.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi [UserProfileRepository] yang mengambil data dari Firestore
 * berdasarkan nikHash dan peran pengguna aktif.
 */
@Singleton
class FirestoreUserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userPreferencesRepository: UserPreferencesRepository
) : UserProfileRepository {

    override suspend fun getActiveUserStats(): UserProfileStats {
        val loggedIn = userPreferencesRepository.getLoggedInUser() ?: return UserProfileStats()
        val role = loggedIn.roles.firstOrNull() ?: return UserProfileStats()
        val snapshot = firestore.collection("users").document(loggedIn.nikHash).get().await()
        val base = "roles.$role"

        fun getInt(field: String): Int = (snapshot.get("$base.$field") as? Long)?.toInt() ?: 0

        val unique = if (role == "customer")
            getInt("uniqueDrivers")
        else
            getInt("uniqueCustomers")

        return UserProfileStats(
            totalRides = getInt("totalRides"),
            uniquePartners = unique,
            positive = getInt("positive"),
            negative = getInt("negative"),
            askCancel = getInt("askCancel")
        )
    }
}
