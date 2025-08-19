package com.undefault.bitride.chooserole

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.Intent
import app.organicmaps.MwmActivity
import app.organicmaps.bitride.mesh.MeshManager
import com.undefault.bitride.navigation.Routes
import kotlinx.coroutines.flow.collect

@Composable
fun ChooseRoleScreen(
    navController: NavController,
    viewModel: ChooseRoleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(navController) {
        viewModel.refreshRoles()
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            if (backStackEntry.destination.route == Routes.CHOOSE_ROLE) {
                viewModel.refreshRoles()
            }
        }
    }

    val navigateToNextScreen = { destination: String ->
        if (destination == Routes.DRIVER_LOUNGE) {
            MeshManager.start(context)
        }
        navController.navigate(destination) {
            // Bersihkan semua layar sebelumnya sampai ke awal
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.canLoginAsCustomer) {
                Button(onClick = {
                    viewModel.checkDataAndGetNextRoute(Routes.MAP_HOME) { destination ->
                        if (destination == Routes.MAP_HOME) {
                            context.startActivity(Intent(context, MwmActivity::class.java))
                            if (context is Activity) {
                                (context as Activity).finish()
                            }
                        } else {
                            navigateToNextScreen(destination)
                        }
                    }
                }) {
                    Text("Masuk sebagai Customer")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (uiState.canLoginAsDriver) {
                Button(onClick = {
                    viewModel.checkDataAndGetNextRoute(Routes.DRIVER_LOUNGE, navigateToNextScreen)
                }) {
                    Text("Masuk sebagai Driver")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (uiState.canRegisterCustomer) {
                Button(onClick = { navController.navigate(Routes.idCardScanWithArgs("customer", false)) }) {
                    Text("Daftar sebagai Customer Baru")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (uiState.canRegisterDriver) {
                Button(onClick = { navController.navigate(Routes.idCardScanWithArgs("driver", false)) }) {
                    Text("Daftar sebagai Driver Baru")
                }
            }
        }
    }
}