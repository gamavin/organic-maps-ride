package com.undefault.bitride.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.undefault.bitride.data.model.CustomerProfile
import com.undefault.bitride.data.model.DriverProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun doesRoleExist(nikHash: String, role: String): Boolean = try {
        val snapshot = firestore.collection("users").document(nikHash).get().await()
        snapshot.get("roles.$role") != null
    } catch (_: Exception) {
        false
    }

    suspend fun createDriverProfile(nikHash: String, stats: DriverProfile): Boolean = try {
        val data = mapOf("roles" to mapOf("driver" to stats))
        Log.d("UserRepository", "createDriverProfile payload: $data")
        firestore.collection("users").document(nikHash)
            .set(data, SetOptions.merge())
            .await()
        true
    } catch (_: Exception) {
        false
    }

    suspend fun createCustomerProfile(nikHash: String, stats: CustomerProfile): Boolean = try {
        val data = mapOf("roles" to mapOf("customer" to stats))
        Log.d("UserRepository", "createCustomerProfile payload: $data")
        firestore.collection("users").document(nikHash)
            .set(data, SetOptions.merge())
            .await()
        true
    } catch (_: Exception) {
        false
    }
}
