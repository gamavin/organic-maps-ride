package com.undefault.bitride.chooserole

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.UserPreferencesRepository
import com.undefault.bitride.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChooseRoleUiState(
    val canLoginAsDriver: Boolean = false,
    val canLoginAsCustomer: Boolean = false,
    val canRegisterDriver: Boolean = false,
    val canRegisterCustomer: Boolean = false
)

@HiltViewModel
class ChooseRoleViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseRoleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadUserRoles()
        }
    }

    private suspend fun loadUserRoles() {
        val loggedInData = userPreferencesRepository.getLoggedInUser()
        val roles = loggedInData?.roles ?: emptyList()
        val hasDriverRole = roles.contains("DRIVER")
        val hasCustomerRole = roles.contains("CUSTOMER")

        _uiState.value = if (loggedInData == null) {
            ChooseRoleUiState(canRegisterDriver = true, canRegisterCustomer = true)
        } else {
            ChooseRoleUiState(
                canLoginAsDriver = hasDriverRole,
                canLoginAsCustomer = hasCustomerRole,
                canRegisterDriver = !hasDriverRole,
                canRegisterCustomer = !hasCustomerRole
            )
        }
    }

    fun checkDataAndGetNextRoute(onResult: (String) -> Unit) {
        viewModelScope.launch {
            onResult(Routes.IMPORT)
        }
    }
}