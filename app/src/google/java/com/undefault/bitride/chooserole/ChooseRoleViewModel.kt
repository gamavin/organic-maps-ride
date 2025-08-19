package com.undefault.bitride.chooserole

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.DataStoreRepository
import com.undefault.bitride.data.repository.UserPreferencesRepository
import com.undefault.bitride.data.model.Roles
import app.organicmaps.downloader.DownloaderActivity
import app.organicmaps.sdk.downloader.MapManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ChooseRoleUiState(
    val canLoginAsDriver: Boolean = false,
    val canLoginAsCustomer: Boolean = false,
    val canRegisterDriver: Boolean = false,
    val canRegisterCustomer: Boolean = false
)

@HiltViewModel
class ChooseRoleViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseRoleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadUserRoles() }
    }

    fun refreshRoles() {
        viewModelScope.launch { loadUserRoles() }
    }

    private suspend fun loadUserRoles() {
        val loggedInData = userPreferencesRepository.getLoggedInUser()
        val roles = loggedInData?.roles?.map { it.uppercase() } ?: emptyList()
        val hasDriverRole = roles.contains(Roles.DRIVER)
        val hasCustomerRole = roles.contains(Roles.CUSTOMER)

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

    fun checkDataAndGetNextRoute(destination: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val mapsDownloaded = MapManager.nativeGetDownloadedCount() > 0
            val brouterDir = File(context.filesDir, "brouter/segments4")
            val brouterReady = brouterDir.exists() &&
                (brouterDir.listFiles()?.any { it.name.endsWith(".rd5") } == true)

            if (mapsDownloaded && brouterReady) {
                val mapFile = context.filesDir.listFiles()?.firstOrNull { it.extension == "mwm" }
                val dbFile = context.filesDir.listFiles()?.firstOrNull { it.extension == "db" }
                mapFile?.let { dataStoreRepository.setActiveMapFileName(it.name) }
                dbFile?.let { dataStoreRepository.setActivePoiDbName(it.name) }
                onResult(destination)
            } else {
                val intent = Intent(context, DownloaderActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}