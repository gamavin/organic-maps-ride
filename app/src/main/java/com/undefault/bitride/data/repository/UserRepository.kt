package com.undefault.bitride.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun doesRoleExist(nikHash: String, role: String): Boolean {
        return try {
            val snapshot = firestore.collection("users").document(nikHash).get().await()
            val roles = snapshot.get("roles") as? List<*>
            roles?.contains(role) == true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun createDriverProfile(nikHash: String): Boolean =
        createRoleIfAbsent(nikHash, "DRIVER")

    suspend fun createCustomerProfile(nikHash: String): Boolean =
        createRoleIfAbsent(nikHash, "CUSTOMER")

    private suspend fun createRoleIfAbsent(nikHash: String, role: String): Boolean {
        return try {
            val doc = firestore.collection("users").document(nikHash)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(doc)
                val roles = snapshot.get("roles") as? MutableList<String> ?: mutableListOf()
                if (!roles.contains(role)) {
                    roles.add(role)
                    transaction.set(doc, mapOf("roles" to roles), SetOptions.merge())
                }
            }.await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
