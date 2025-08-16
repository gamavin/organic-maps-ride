package com.undefault.bitride.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.undefault.bitride.auth.AuthScreen
import com.undefault.bitride.chooserole.ChooseRoleScreen
import com.undefault.bitride.customerregistrationform.CustomerRegistrationFormScreen
import com.undefault.bitride.driverregistrationform.DriverRegistrationFormScreen

/**
 * Menangani navigasi aplikasi BitRide.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.AUTH) {
        composable(Routes.AUTH) {
            AuthScreen(
                onNavigateToChooseRole = { navController.navigate(Routes.CHOOSE_ROLE) },
                onNavigateToNextScreen = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CHOOSE_ROLE) {
            ChooseRoleScreen(navController)
        }
        composable(Routes.CUSTOMER_REGISTRATION_FORM) {
            CustomerRegistrationFormScreen(
                onRegistrationComplete = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToScanKtp = { navController.navigate(Routes.CUSTOMER_REGISTRATION_FORM) }
            )
        }
        composable(Routes.DRIVER_REGISTRATION_FORM) {
            DriverRegistrationFormScreen(
                onRegistrationComplete = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToScanKtp = { navController.navigate(Routes.DRIVER_REGISTRATION_FORM) }
            )
        }
        placeholderScreen(Routes.MAIN, "Layar Utama")
        placeholderScreen(Routes.IMPORT, "Impor Data")
    }
}

private fun NavGraphBuilder.placeholderScreen(route: String, title: String) {
    composable(route) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title)
        }
    }
}
