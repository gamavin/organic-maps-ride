package com.undefault.bitride.chooserole

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.organicmaps.MwmActivity
import app.organicmaps.bitride.mesh.MeshManager
import com.undefault.bitride.navigation.Routes

@Composable
fun ChooseRoleScreen(
    navController: NavController,
    viewModel: ChooseRoleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val navigateToNextScreen: (String) -> Unit = { destination ->
        if (destination == Routes.MAIN) {
            context.startActivity(
                Intent(context, MwmActivity::class.java)
                    .putExtra(MwmActivity.EXTRA_SHOW_SEARCH, true)
            )
            (context as? Activity)?.finish() ?: Unit
        } else {
            navController.navigate(destination) {
                // Bersihkan semua layar sebelumnya sampai ke awal
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
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
                    MeshManager.start(context)
                    viewModel.checkDataAndGetNextRoute(navigateToNextScreen)
                }) {
                    Text("Masuk sebagai Customer")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (uiState.canLoginAsDriver) {
                Button(onClick = {
                    MeshManager.start(context)
                    navController.navigate(Routes.DRIVER_LOUNGE)
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
