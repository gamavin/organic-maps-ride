package com.undefault.bitride.chooserole

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.DataStoreRepository
import com.undefault.bitride.data.repository.UserPreferencesRepository
import com.undefault.bitride.navigation.Routes
import app.organicmaps.downloader.DownloaderActivity
import app.organicmaps.sdk.downloader.MapManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @param:ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseRoleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserRoles()
    }

    private fun loadUserRoles() {
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
            val hasMaps = MapManager.nativeGetDownloadedCount() > 0

            if (hasMaps) {
                dataStoreRepository.setActiveMapFileName("default.mwm")
                dataStoreRepository.setActivePoiDbName("default.poi.db")
                onResult(Routes.MAIN)
            } else {
                context.startActivity(android.content.Intent(context, DownloaderActivity::class.java))
            }
        }
    }
}