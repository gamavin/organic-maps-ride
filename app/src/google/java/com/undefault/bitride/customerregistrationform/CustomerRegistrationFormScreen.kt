package com.undefault.bitride.customerregistrationform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CustomerRegistrationFormScreen(
    viewModel: CustomerRegistrationViewModel = hiltViewModel(),
    initialNik: String? = null,
    initialName: String? = null,
    onRegistrationComplete: () -> Unit, // Tipe diubah, tidak perlu hash
    onNavigateToScanKtp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentValidationError = uiState.validationError

    LaunchedEffect(initialNik, initialName) {
        viewModel.processScannedData(initialNik, initialName)
    }

    // LaunchedEffect diubah untuk mengamati 'registrationSuccess'
    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            onRegistrationComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Formulir Pendaftaran Customer",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = uiState.nik,
                onValueChange = { viewModel.onNikChange(it) },
                label = { Text("Nomor Induk Kependudukan (NIK)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = currentValidationError?.contains("NIK", ignoreCase = true) == true || (currentValidationError != null && uiState.nik.isBlank()),
                supportingText = {
                    if (uiState.nik.isNotEmpty() && uiState.nik.length != 16) {
                        Text("NIK harus 16 digit. Panjang saat ini: ${uiState.nik.length}", color = MaterialTheme.colorScheme.error)
                    } else if (currentValidationError?.contains("NIK", ignoreCase = true) == true && uiState.nik.isBlank()){
                        Text("NIK tidak boleh kosong.", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = currentValidationError != null && uiState.name.isBlank(),
                supportingText = {
                    if (currentValidationError != null && uiState.name.isBlank()){
                        Text("Nama Lengkap tidak boleh kosong.", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            if (currentValidationError != null &&
                !(currentValidationError.contains("NIK", ignoreCase = true) && uiState.nik.isNotEmpty() && uiState.nik.length != 16) &&
                !(currentValidationError.contains("NIK", ignoreCase = true) && uiState.nik.isBlank()) &&
                !(currentValidationError.isNotEmpty() && uiState.name.isBlank())
            ) {
                Text(
                    text = currentValidationError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onNavigateToScanKtp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan Ulang KTP")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onContinueClicked() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text("Lanjut")
            }
        }

        if (uiState.showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissConfirmationDialog() },
                title = { Text("Konfirmasi Data") },
                text = { Text("Pastikan semua data yang Anda masukkan sudah benar sebelum melanjutkan.") },
                confirmButton = {
                    Button(onClick = { viewModel.onConfirmData() }) {
                        Text("Konfirmasi")
                    }
                },
                dismissButton = {
                    Button(onClick = { viewModel.onDismissConfirmationDialog() }) {
                        Text("Batal")
                    }
                }
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}