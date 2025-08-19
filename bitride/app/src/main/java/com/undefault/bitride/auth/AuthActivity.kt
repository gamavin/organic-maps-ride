package com.undefault.bitride.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.organicmaps.DownloadResourcesLegacyActivity
import com.undefault.bitride.navigation.AppNavigation
import com.undefault.bitride.navigation.Routes
import com.undefault.bitride.ui.theme.BitrideTheme // Ganti dengan nama tema aplikasi Anda

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startRoute = intent.getStringExtra(DownloadResourcesLegacyActivity.EXTRA_NEXT_ROUTE) ?: Routes.AUTH
        setContent {
            // Menggunakan tema yang sudah didefinisikan untuk konsistensi UI
            BitrideTheme {
                AppNavigation(startDestination = startRoute)
            }
        }
    }
}