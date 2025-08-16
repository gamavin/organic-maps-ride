package com.undefault.bitride.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.LoggedInData
import com.undefault.bitride.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = true,
    // Kita ubah state ini untuk menampung data lengkap pengguna jika ada
    val loggedInData: LoggedInData? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val userPreferencesRepository = UserPreferencesRepository(application)

    init {
        checkLocalLoginStatus()
    }

    private fun checkLocalLoginStatus() {
        viewModelScope.launch {
            val loggedInData = userPreferencesRepository.getLoggedInUser()

            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    loggedInData = loggedInData // Simpan data pengguna (atau null jika tidak ada)
                )
            }
        }
    }
}