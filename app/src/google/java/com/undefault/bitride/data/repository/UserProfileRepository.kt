package com.undefault.bitride.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.undefault.bitride.data.model.UserProfile
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dataStoreRepository: DataStoreRepository
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(),
        DataStoreRepository(context)
    )

    suspend fun getProfile(role: String): UserProfile? {
        val nikHash = dataStoreRepository.nikHashFlow.firstOrNull() ?: return null
        return try {
            val snapshot = firestore.collection("users").document(nikHash).get().await()
            val map = snapshot.get("roles.$role") as? Map<*, *> ?: return null
            UserProfile(
                totalRides = (map["totalRides"] as? Long ?: 0L).toInt(),
                uniqueDrivers = ((map["uniqueDrivers"] ?: map["uniqueCustomers"]) as? Long ?: 0L).toInt(),
                positive = (map["positive"] as? Long ?: 0L).toInt(),
                negative = (map["negative"] as? Long ?: 0L).toInt(),
                askCancel = (map["askCancel"] as? Long ?: 0L).toInt()
            )
        } catch (_: Exception) {
            null
        }
    }
}
