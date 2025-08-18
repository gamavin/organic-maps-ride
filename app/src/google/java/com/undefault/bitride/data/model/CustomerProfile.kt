package com.undefault.bitride.data.model

/**
 * Profil customer yang disimpan di Firestore di bawah path `roles.customer`.
 * Hanya berisi statistik tanpa data pribadi.
 */
data class CustomerProfile(
    val totalRides: Long = 0L,
    val successfulPayments: Long = 0L,
    val numberOfDifferentDrivers: Long = 0L,
    val askingCancel: Long = 0L,
    val positiveRate: Long = 0L,
    val negativeRate: Long = 0L
)
