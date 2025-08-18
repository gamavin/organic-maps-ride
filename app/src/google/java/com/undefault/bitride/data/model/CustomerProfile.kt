package com.undefault.bitride.data.model

/**
 * Profil customer yang disimpan di Firestore di bawah path `roles.customer`.
 */
data class CustomerProfile(
    val name: String = "",
    val numberOfDifferentDrivers: Long = 0L
)
