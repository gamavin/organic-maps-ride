package com.undefault.bitride.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.undefault.bitride.navigation.AppNavigation
import com.undefault.bitride.navigation.Routes
import com.undefault.bitride.ui.theme.BitrideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    companion object {
        const val EXTRA_START_DESTINATION = "extra_start_destination"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startDestination = intent.getStringExtra(EXTRA_START_DESTINATION) ?: Routes.AUTH
        setContent {
            BitrideTheme {
                AppNavigation(startDestination)
            }
        }
    }
}
