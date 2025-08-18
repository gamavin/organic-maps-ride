package com.undefault.bitride.data.model

/**
 * Profil driver yang disimpan di Firestore di bawah path `roles.driver`.
 * Hanya berisi statistik tanpa data pribadi.
 */
data class DriverProfile(
    val totalRides: Long = 0L,
    val successfulRides: Long = 0L,
    val numberOfDifferentCustomers: Long = 0L,
    val askingCancel: Long = 0L,
    val positiveRate: Long = 0L,
    val negativeRate: Long = 0L
)
