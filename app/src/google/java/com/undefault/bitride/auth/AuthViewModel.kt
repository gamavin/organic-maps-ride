package com.undefault.bitride.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.LoggedInData
import com.undefault.bitride.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = true,
    val loggedInData: LoggedInData? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkLocalLoginStatus()
    }

    private fun checkLocalLoginStatus() {
        viewModelScope.launch {
            val loggedInData = userPreferencesRepository.getLoggedInUser()
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    loggedInData = loggedInData
                )
            }
        }
    }
}
