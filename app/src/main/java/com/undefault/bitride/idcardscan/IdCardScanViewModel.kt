package com.undefault.bitride.idcardscan

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.net.Uri // Added for Uri.fromFile
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage // Added for InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition // Added for TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions // Added for TextRecognizerOptions
import java.io.File
import java.io.IOException // Added for IOException

class IdCardScanViewModel : ViewModel() {

    fun startScan(activity: Activity, launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                try {
                    launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                } catch (e: IntentSender.SendIntentException) {
                    // TODO: Handle exception
                }
            }
            .addOnFailureListener {
                // TODO: Handle failure
            }
    }

    fun handleScanResult(
        result: ActivityResult,
        context: Context,
        onScanSuccess: (List<File>) -> Unit,
        onScanFailure: () -> Unit
    ) {
        val gmsResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        gmsResult?.pages?.let { pages ->
            val imageFiles = mutableListOf<File>()
            pages.forEachIndexed { index, page ->
                val file = File(context.cacheDir, "id_card_scan_${System.currentTimeMillis()}_$index.jpg")
                try { // Added try-catch for file operations
                    context.contentResolver.openInputStream(page.imageUri)?.use { inputStream ->
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    imageFiles.add(file)
                } catch (e: IOException) {
                    // TODO: Handle file IO exception
                    onScanFailure() // Propagate failure
                    return@handleScanResult
                }
            }
            if (imageFiles.isNotEmpty()) {
                onScanSuccess(imageFiles)
            } else {
                onScanFailure() // Call failure if no files were processed successfully
            }
        } ?: onScanFailure() // Call onScanFailure if gmsResult is null
    }

    fun extractTextFromImage(
        context: Context,
        imageFile: File,
        onTextExtracted: (KtpData) -> Unit, // Changed to KtpData
        onFailure: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, Uri.fromFile(imageFile))
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val rawText = visionText.text
                    val ktpData = parseKtpText(rawText) // Use the parser
                    onTextExtracted(ktpData) // Pass KtpData object
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } catch (e: IOException) {
            onFailure(e)
        }
    }
}