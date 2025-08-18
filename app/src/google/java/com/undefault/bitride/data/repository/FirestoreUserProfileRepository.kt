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

        val success = if (role == "driver")
            getInt("successfulRides")
        else
            getInt("successfulPayments")

        val unique = if (role == "customer")
            getInt("numberOfDifferentDrivers")
        else
            getInt("numberOfDifferentCustomers")

        return UserProfileStats(
            totalRides = getInt("totalRides"),
            successful = success,
            uniquePartners = unique,
            positive = getInt("positiveRate"),
            negative = getInt("negativeRate"),
            askCancel = getInt("askingCancel")
        )
    }
}
