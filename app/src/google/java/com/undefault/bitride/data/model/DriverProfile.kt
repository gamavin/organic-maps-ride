package com.undefault.bitride.data.model

/**
 * Profil driver yang disimpan di Firestore di bawah path `roles.driver`.
 */
data class DriverProfile(
    val name: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val numberOfDifferentCustomers: Long = 0L
)
