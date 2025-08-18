package com.undefault.bitride.data.model

data class UserProfile(
    val totalRides: Int = 0,
    val uniqueDrivers: Int = 0,
    val positive: Int = 0,
    val negative: Int = 0,
    val askCancel: Int = 0
)
