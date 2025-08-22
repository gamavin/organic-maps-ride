package com.undefault.bitride.driverregistrationform

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.UserPreferencesRepository
import com.undefault.bitride.data.repository.UserRepository
import com.undefault.bitride.util.runWithGms
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
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

@HiltViewModel
class DriverRegistrationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverRegistrationFormState())
    val uiState: StateFlow<DriverRegistrationFormState> = _uiState.asStateFlow()

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
            val nikToHash = _uiState.value.nik
            val hashedNik = hashSha256(nikToHash)

            if (hashedNik.isBlank()) {
                _uiState.update { it.copy(isLoading = false, validationError = "Gagal melakukan hash NIK.") }
                return@launch
            }

            runWithGms(context, {
                val roleExists = userRepository.doesRoleExist(hashedNik, "DRIVER")
                if (roleExists) {
                    _uiState.update { it.copy(isLoading = false, validationError = "Akun Driver dengan NIK ini sudah terdaftar.") }
                    return@runWithGms
                }

                val success = userRepository.createDriverProfile(hashedNik)
                if (success) {
                    Log.d("DriverRegistrationVM", "Pendaftaran profil Driver berhasil untuk: $hashedNik")
                    userPreferencesRepository.saveLoggedInUser(hashedNik, "DRIVER")
                    Log.d("DriverRegistrationVM", "Data pengguna disimpan ke SharedPreferences.")
                    _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
                } else {
                    Log.e("DriverRegistrationVM", "Pendaftaran profil Driver gagal!")
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
