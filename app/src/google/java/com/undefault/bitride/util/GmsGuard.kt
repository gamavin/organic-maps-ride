package com.undefault.bitride.util

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object GmsGuard {
    fun isGmsAvailable(context: Context): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        return availability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }
}

suspend fun <T> Context.runWithGms(
    onAvailable: suspend () -> T,
    onUnavailable: suspend () -> T
): T {
    return if (GmsGuard.isGmsAvailable(this)) {
        onAvailable()
    } else {
        onUnavailable()
    }
}

