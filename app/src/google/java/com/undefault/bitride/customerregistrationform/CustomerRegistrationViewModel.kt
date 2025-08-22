package com.undefault.bitride.customerregistrationform

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.undefault.bitride.data.repository.UserPreferencesRepository
import com.undefault.bitride.data.repository.UserRepository
import com.undefault.bitride.util.runWithGms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class CustomerRegistrationFormState(
    val nik: String = "",
    val name: String = "",
    val validationError: String? = null,
    val showConfirmationDialog: Boolean = false,
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false
)

class CustomerRegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CustomerRegistrationFormState())
    val uiState: StateFlow<CustomerRegistrationFormState> = _uiState.asStateFlow()

    // PASS Firestore ke constructor
    private val userRepository = UserRepository(FirebaseFirestore.getInstance(), application)
    private val userPreferencesRepository = UserPreferencesRepository(application)

    fun processScannedData(scannedNik: String?, scannedName: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                nik = scannedNik?.filter { it.isDigit() }?.take(16) ?: currentState.nik,
                name = scannedName ?: currentState.name,
                validationError = null
            )
        }
    }

    fun onNikChange(nik: String) {
        _uiState.update { currentState ->
            currentState.copy(
                nik = nik.filter { it.isDigit() }.take(16),
                validationError = null
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = name,
                validationError = null
            )
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        if (state.nik.isBlank() || state.name.isBlank()) {
            _uiState.update { it.copy(validationError = "Nama dan NIK wajib diisi.") }
            return false
        }
        if (state.nik.length != 16) {
            _uiState.update { it.copy(validationError = "NIK harus terdiri dari 16 digit.") }
            return false
        }
        _uiState.update { it.copy(validationError = null) }
        return true
    }

    fun onContinueClicked() {
        if (validateInputs()) {
            _uiState.update { it.copy(showConfirmationDialog = true) }
        }
    }

    fun onConfirmData() {
        _uiState.update { it.copy(showConfirmationDialog = false, isLoading = true, validationError = null) }
        viewModelScope.launch {
            val nikToHash = _uiState.value.nik
            val hashedNik = hashSha256(nikToHash)

            if (hashedNik.isBlank()) {
                _uiState.update { it.copy(isLoading = false, validationError = "Gagal melakukan hash NIK.") }
                return@launch
            }

            runWithGms(getApplication(), {
                val roleExists = userRepository.doesRoleExist(hashedNik, "CUSTOMER")
                if (roleExists) {
                    _uiState.update { it.copy(isLoading = false, validationError = "Akun Customer dengan NIK ini sudah terdaftar.") }
                    return@runWithGms
                }

                val success = userRepository.createCustomerProfile(hashedNik)
                if (success) {
                    Log.d("CustomerRegistrationVM", "Pendaftaran profil Customer berhasil untuk: $hashedNik")
                    userPreferencesRepository.saveLoggedInUser(hashedNik, "CUSTOMER")
                    Log.d("CustomerRegistrationVM", "Data pengguna disimpan ke SharedPreferences.")
                    _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
                } else {
                    Log.e("CustomerRegistrationVM", "Pendaftaran profil Customer gagal!")
                    _uiState.update { it.copy(isLoading = false, validationError = "Pendaftaran gagal, coba lagi.") }
                }
            }, {
                _uiState.update { it.copy(isLoading = false, validationError = "Google Play Services tidak tersedia.") }
            })
        }
    }

    fun onDismissConfirmationDialog() {
        _uiState.update { it.copy(showConfirmationDialog = false) }
    }

    private fun hashSha256(input: String): String {
        return try {
            Log.d("CustomerRegistrationVM", "Di dalam hashSha256 untuk input: $input")
            val bytes = input.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("CustomerRegistrationVM", "Error saat hashing SHA-256", e)
            ""
        }
    }
}
