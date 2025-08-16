package com.undefault.bitride.idcardscan

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun IdCardScanScreen(
    viewModel: IdCardScanViewModel = viewModel(),
    onScanComplete: (KtpData?) -> Unit,
    isRescan: Boolean // New parameter
) {
    val context = LocalContext.current
    val activity = context as Activity
    var scannedImageFile by remember { mutableStateOf<File?>(null) }
    var nik by remember { mutableStateOf<String?>(null) }
    var nama by remember { mutableStateOf<String?>(null) }
    var scanStatusMessage by remember { mutableStateOf(if (isRescan) "Initiating rescan..." else "Scan an ID card to see the extracted text.") }
    var isNavigatingAway by remember { mutableStateOf(false) }

    val triggerNavigation = { ktpData: KtpData? ->
        isNavigatingAway = true
        onScanComplete(ktpData)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            viewModel.handleScanResult(
                result = it,
                context = context,
                onScanSuccess = { files ->
                    val firstFile = files.firstOrNull()
                    scannedImageFile = firstFile
                    if (firstFile != null) {
                        viewModel.extractTextFromImage(
                            context = context,
                            imageFile = firstFile,
                            onTextExtracted = { ktpData ->
                                nik = ktpData.nik
                                nama = ktpData.nama
                                scanStatusMessage = if (ktpData.nik != null && ktpData.nama != null) {
                                    "NIK: ${ktpData.nik}\nNama: ${ktpData.nama}"
                                } else if (ktpData.nik != null) {
                                    "NIK: ${ktpData.nik}\nNama: Not Found"
                                } else if (ktpData.nama != null) {
                                    "NIK: Not Found\nNama: ${ktpData.nama}"
                                } else {
                                    "NIK & Nama not found. Please try again."
                                }
                                triggerNavigation(ktpData)
                            },
                            onFailure = {
                                nik = null
                                nama = null
                                scanStatusMessage = "Text extraction failed: ${it.message}"
                                triggerNavigation(null)
                            }
                        )
                    } else {
                        scanStatusMessage = "Scan successful, but no image was captured."
                        triggerNavigation(null)
                    }
                },
                onScanFailure = {
                    scanStatusMessage = "Scan failed or cancelled."
                    triggerNavigation(null)
                }
            )
        }
    )

    // LaunchedEffect to trigger scan if isRescan is true
    LaunchedEffect(key1 = isRescan) {
        if (isRescan) {
            // Ensure UI doesn't show Start Scan button briefly
            isNavigatingAway = false // Ensure the main UI or loader shows correctly
            scanStatusMessage = "Initiating rescan..."
            viewModel.startScan(activity, scannerLauncher)
        }
    }

    if (isNavigatingAway && !isRescan) { // Show loader only if navigating away from a normal scan, not during rescan initiation
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (scannedImageFile != null) {
                Image(
                    painter = rememberAsyncImagePainter(scannedImageFile),
                    contentDescription = "Scanned ID Card",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(text = scanStatusMessage)
            Spacer(modifier = Modifier.height(32.dp))

            // Show "Start Scan" button only if not a rescan operation
            if (!isRescan) {
                Button(onClick = {
                    scannedImageFile = null
                    nik = null
                    nama = null
                    scanStatusMessage = "Scanning..."
                    isNavigatingAway = false
                    viewModel.startScan(activity, scannerLauncher)
                }) {
                    Text("Start Scan")
                }
            } else {
                // Optionally, show a different message or loader during rescan initiation
                // For now, scanStatusMessage is "Initiating rescan..."
                CircularProgressIndicator() // Show a loader while camera is being invoked
            }
        }
    }
}
