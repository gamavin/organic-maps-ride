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

    private fun logPayload(action: String, data: Any) {
        val payloadString = data.toString().lowercase()
        if (payloadString.contains("name") || payloadString.contains("nik") || payloadString.contains("bank")) {
            Log.w("UserRepository", "$action payload contains personal info: $payloadString")
        } else {
            Log.d("UserRepository", "$action payload: $payloadString")
        }
    }

    suspend fun doesRoleExist(nikHash: String, role: String): Boolean = try {
        val snapshot = firestore.collection("users").document(nikHash).get().await()
        snapshot.get("roles.$role") != null
    } catch (_: Exception) {
        false
    }

    suspend fun createDriverProfile(nikHash: String, stats: DriverProfile): Boolean = try {
        val data = mapOf("roles" to mapOf("driver" to stats))
        logPayload("createDriverProfile", data)
        firestore.collection("users").document(nikHash)
            .set(data, SetOptions.merge())
            .await()
        true
    } catch (_: Exception) {
        false
    }

    suspend fun createCustomerProfile(nikHash: String, stats: CustomerProfile): Boolean = try {
        val data = mapOf("roles" to mapOf("customer" to stats))
        logPayload("createCustomerProfile", data)
        firestore.collection("users").document(nikHash)
            .set(data, SetOptions.merge())
            .await()
        true
    } catch (_: Exception) {
        false
    }
}
