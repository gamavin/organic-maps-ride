package com.undefault.bitride.driverregistrationform

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.DataStoreRepository
import com.undefault.bitride.data.repository.UserPreferencesRepository
import com.undefault.bitride.data.repository.UserRepository
import com.undefault.bitride.data.model.DriverProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class DriverRegistrationFormState(
    val nik: String = "",
    val name: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val validationError: String? = null,
    val showConfirmationDialog: Boolean = false,
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false
)

class DriverRegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DriverRegistrationFormState())
    val uiState: StateFlow<DriverRegistrationFormState> = _uiState.asStateFlow()

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val dataStoreRepository = DataStoreRepository(application)
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())

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

    fun onBankNameChange(bankName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                bankName = bankName,
                validationError = null
            )
        }
    }

    fun onBankAccountNumberChange(accountNumber: String) {
        _uiState.update { currentState ->
            currentState.copy(
                bankAccountNumber = accountNumber.filter { it.isDigit() },
                validationError = null
            )
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        if (state.nik.isBlank() || state.name.isBlank() || state.bankName.isBlank() || state.bankAccountNumber.isBlank()) {
            _uiState.update { it.copy(validationError = "Semua field wajib diisi.") }
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
            val nik = _uiState.value.nik
            val hashedNik = hashSha256(nik)

            if (hashedNik.isBlank()) {
                _uiState.update { it.copy(isLoading = false, validationError = "Gagal melakukan hash NIK.") }
                return@launch
            }

            // Profil awal hanya berisi statistik dengan nilai 0
            val profile = DriverProfile()

            val success = userRepository.createDriverProfile(hashedNik, profile)
            if (success) {
                dataStoreRepository.savePersonalInfo(_uiState.value.name, nik)
                dataStoreRepository.saveBankInfo(_uiState.value.bankName, _uiState.value.bankAccountNumber)
                userPreferencesRepository.saveLoggedInUser(hashedNik, "driver")
                Log.d("DriverRegistrationVM", "Data driver disimpan ke storage lokal dan Firestore.")
                _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, validationError = "Pendaftaran gagal, coba lagi.")
                }
            }
        }
    }

    fun onDismissConfirmationDialog() {
        _uiState.update { it.copy(showConfirmationDialog = false) }
    }

    private fun hashSha256(input: String): String {
        return try {
            Log.d("DriverRegistrationVM", "Inside hashSha256 for input: $input")
            val bytes = input.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("DriverRegistrationVM", "Error during SHA-256 hashing", e)
            ""
        }
    }

    fun processKtpData(nikFromScan: String?, nameFromScan: String?) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    nik = nikFromScan?.filter { it.isDigit() }?.take(16) ?: currentState.nik,
                    name = nameFromScan ?: currentState.name,
                    validationError = null
                )
            }
        }
    }
}
