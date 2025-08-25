package com.undefault.bitride.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.undefault.bitride.chooserole.ChooseRoleScreen
import com.undefault.bitride.customerregistrationform.CustomerRegistrationFormScreen
import com.undefault.bitride.driverregistrationform.DriverRegistrationFormScreen
import com.undefault.bitride.idcardscan.IdCardScanScreen
import com.undefault.bitride.driverlounge.DriverLoungeScreen
import app.organicmaps.MwmActivity
import app.organicmaps.bitride.mesh.MeshManager

/**
 * Menangani navigasi aplikasi BitRide.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Routes.CHOOSE_ROLE) {
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
                    MeshManager.start(context)
                    val intent = Intent(context, MwmActivity::class.java).apply {
                        putExtra(MwmActivity.EXTRA_SHOW_SEARCH, true)
                    }
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
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
                    MeshManager.start(context)
                    navController.navigate(Routes.DRIVER_LOUNGE) {
                        popUpTo(Routes.CHOOSE_ROLE) { inclusive = true }
                    }
                },
                onNavigateToScanKtp = { navController.navigate(Routes.idCardScanWithArgs("driver", true)) }
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
