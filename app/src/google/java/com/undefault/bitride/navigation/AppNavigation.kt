package com.undefault.bitride.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.undefault.bitride.auth.AuthScreen
import com.undefault.bitride.chooserole.ChooseRoleScreen
import com.undefault.bitride.customerregistrationform.CustomerRegistrationFormScreen
import com.undefault.bitride.driverregistrationform.DriverRegistrationFormScreen
import com.undefault.bitride.idcardscan.IdCardScanScreen
import com.undefault.bitride.driverlounge.DriverLoungeScreen

/**
 * Menangani navigasi aplikasi BitRide.
 */
@Composable
fun AppNavigation(startDestination: String = Routes.AUTH) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.AUTH) {
            AuthScreen(
                onNavigateToChooseRole = { navController.navigate(Routes.CHOOSE_ROLE) },
                onNavigateToNextScreen = {
                    navController.navigate(Routes.CHOOSE_ROLE) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CHOOSE_ROLE) {
            ChooseRoleScreen(navController)
        }
        composable(Routes.DRIVER_LOUNGE) {
            DriverLoungeScreen()
        }
        composable(
            route = "${Routes.ID_CARD_SCAN}?role={role}&isRescan={isRescan}",
            arguments = listOf(
                navArgument("role") { type = NavType.StringType },
                navArgument("isRescan") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            val isRescan = backStackEntry.arguments?.getBoolean("isRescan") ?: false
            IdCardScanScreen(
                isRescan = isRescan,
                onScanComplete = { data ->
                    val destination = when (role.lowercase()) {
                        "customer" -> Routes.customerRegistrationWithArgs(data?.nik, data?.nama)
                        "driver" -> Routes.driverRegistrationWithArgs(data?.nik, data?.nama)
                        else -> Routes.CHOOSE_ROLE
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.ID_CARD_SCAN) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "${Routes.CUSTOMER_REGISTRATION_FORM}?nik={nik}&name={name}",
            arguments = listOf(
                navArgument("nik") { type = NavType.StringType; nullable = true },
                navArgument("name") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val nik = backStackEntry.arguments?.getString("nik")
            val name = backStackEntry.arguments?.getString("name")
            CustomerRegistrationFormScreen(
                initialNik = nik,
                initialName = name,
                onRegistrationComplete = {
                    navController.navigate(Routes.CHOOSE_ROLE) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToScanKtp = { navController.navigate(Routes.idCardScanWithArgs("customer", true)) }
            )
        }
        composable(
            route = "${Routes.DRIVER_REGISTRATION_FORM}?nik={nik}&name={name}",
            arguments = listOf(
                navArgument("nik") { type = NavType.StringType; nullable = true },
                navArgument("name") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val nik = backStackEntry.arguments?.getString("nik")
            val name = backStackEntry.arguments?.getString("name")
            DriverRegistrationFormScreen(
                initialNik = nik,
                initialName = name,
                onRegistrationComplete = {
                    navController.navigate(Routes.CHOOSE_ROLE) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToScanKtp = { navController.navigate(Routes.idCardScanWithArgs("driver", true)) }
            )
        }
    }
}
