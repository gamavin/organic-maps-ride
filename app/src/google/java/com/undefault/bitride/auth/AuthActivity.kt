package com.undefault.bitride.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.undefault.bitride.navigation.AppNavigation
import com.undefault.bitride.ui.theme.BitrideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BitrideTheme {
                AppNavigation()
            }
        }
    }
}
