package app.organicmaps

import app.organicmaps.sdk.util.log.Logger
import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp
import com.undefault.bitride.firebase.BitrideFirebase

/**
 * Extend MwmApplication agar init Organic Maps tetap berjalan.
 */
@HiltAndroidApp
class BitrideHiltApp : MwmApplication() {

    override fun onCreate() {
        super.onCreate()
        ensureBitrideFirebase()
    }

    private fun ensureBitrideFirebase() {
        val name = BitrideFirebase.APP_NAME
        val already = FirebaseApp.getApps(this).any { it.name == name }
        if (already) {
            Logger.i(TAG, "Secondary FirebaseApp '$name' already initialized")
            return
        }

        // Opsi dari project Bitride
        val options = FirebaseOptions.Builder()
            .setApplicationId("1:1087548347259:android:8611eca5cecb68c57ce3de")
            .setApiKey("AIzaSyByUQdjDImsPz9S-m-WK6VNPQWW66OndjE")
            .setProjectId("bitride-4a48e")
            .setStorageBucket("bitride-4a48e.firebasestorage.app")
            .build()

        FirebaseApp.initializeApp(this, options, name)
        Logger.i(TAG, "Secondary FirebaseApp '$name' initialized")
    }

    companion object {
        private const val TAG = "BitrideHiltApp"
    }
}
