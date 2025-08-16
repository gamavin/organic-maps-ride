package com.undefault.bitride.driverlounge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.organicmaps.bitride.mesh.VehicleType

/**
 * Layar lounge untuk driver yang menampilkan permintaan tumpangan dari channel #bitride.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverLoungeScreen(
    vm: DriverLoungeViewModel = viewModel()
) {
    val requests by vm.requests.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Driver Lounge") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(requests) { req ->
                Card(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("customer")
                        Text("Pickup: ${req.pickupName} (${req.pickup.lat}, ${req.pickup.lon})")
                        Text("Destination: ${req.destinationName} (${req.destination.lat}, ${req.destination.lon})")
                        Text("Kendaraan: ${if (req.vehicle == VehicleType.CAR) "Car" else "Motorcycle"}")
                        Text("Harga: Rp${req.priceRp}")
                        Text("Pembayaran: ${req.payment}")
                        Text("Toll: ${if (req.toll) "Ya" else "Tidak"}")
                        if (req.note.isNotEmpty()) {
                            Text("Catatan: ${req.note}")
                        }
                    }
                }
            }
        }
    }
}
