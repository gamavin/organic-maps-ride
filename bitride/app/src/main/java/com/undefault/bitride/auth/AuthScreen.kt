package com.undefault.bitride.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToChooseRole: () -> Unit,
    onNavigateToNextScreen: () -> Unit // Parameter baru untuk auto-login
) {
    val uiState by viewModel.uiState.collectAsState()

    // LaunchedEffect akan memeriksa status login saat layar pertama kali muncul
    LaunchedEffect(uiState.isLoading) {
        // Hanya jalankan jika pengecekan selesai
        if (!uiState.isLoading) {
            if (uiState.loggedInData != null) {
                // Jika ada data login, langsung navigasi ke layar berikutnya
                onNavigateToNextScreen()
            }
            // Jika loggedInData null, biarkan UI login/daftar ditampilkan
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            // Tampilkan loading saat ViewModel sedang memeriksa status login
            CircularProgressIndicator()
        } else if (uiState.loggedInData == null) {
            // Tampilkan UI login/daftar HANYA jika tidak ada pengguna yang login
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Selamat Datang di BitRide")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateToChooseRole) {
                    Text("Login / Daftar")
                }
            }
        }
        // Jika sudah login, Box akan kosong sejenak sebelum navigasi otomatis terjadi
    }
}