package com.undefault.bitride.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.undefault.bitride.firebase.BitrideFirebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import app.organicmaps.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestore(@ApplicationContext context: Context): FirebaseFirestore? {
        if (!BuildConfig.USE_FIRESTORE) return null
        // Pastikan app sekunder ada (kalau BitrideHiltApp belum sempat init karena urutan, init di sini juga aman)
        val app = try {
            FirebaseApp.getInstance(BitrideFirebase.APP_NAME)
        } catch (_: IllegalStateException) {
            val apps = FirebaseApp.getApps(context)
            // fallback ke default kalau ada; kalau tidak ada, ini menandakan google-services.json default terpasang
            if (apps.isNotEmpty()) apps.first() else FirebaseApp.initializeApp(context)!!
        }
        return FirebaseFirestore.getInstance(app)
    }
}
