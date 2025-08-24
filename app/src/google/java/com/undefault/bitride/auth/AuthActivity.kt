package com.undefault.bitride.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.organicmaps.sdk.downloader.MapManager
import com.undefault.bitride.downloader.AutoDownloadActivity
import com.undefault.bitride.navigation.AppNavigation
import com.undefault.bitride.ui.theme.BitrideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MapManager.nativeGetDownloadedCount() == 0) {
            startActivity(Intent(this, AutoDownloadActivity::class.java))
        }
        setContent {
            BitrideTheme {
                AppNavigation()
            }
        }
    }
}
