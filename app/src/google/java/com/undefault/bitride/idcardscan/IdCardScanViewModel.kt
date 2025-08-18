package com.undefault.bitride.idcardscan

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.net.Uri // Added for Uri.fromFile
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.mlkit.vision.common.InputImage // Added for InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition // Added for TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions // Added for TextRecognizerOptions
import java.io.File
import java.io.IOException // Added for IOException
import kotlinx.coroutines.launch

@HiltViewModel
class IdCardScanViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

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
                val dir = File(context.filesDir, "id_cards")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "id_card_${System.currentTimeMillis()}_$index.jpg")
                try {
                    context.contentResolver.openInputStream(page.imageUri)?.use { inputStream ->
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    imageFiles.add(file)
                } catch (e: IOException) {
                    onScanFailure()
                    return@handleScanResult
                }
            }
            if (imageFiles.isNotEmpty()) {
                viewModelScope.launch {
                    dataStoreRepository.saveIdCardPhotoPath(imageFiles.first().absolutePath)
                }
                onScanSuccess(imageFiles)
            } else {
                onScanFailure()
            }
        } ?: onScanFailure()
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