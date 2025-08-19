package com.undefault.bitride.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.undefault.bitride.navigation.AppNavigation
import com.undefault.bitride.ui.theme.BitrideTheme // Ganti dengan nama tema aplikasi Anda

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Menggunakan tema yang sudah didefinisikan untuk konsistensi UI
            BitrideTheme {
                AppNavigation()
            }
        }
    }
}