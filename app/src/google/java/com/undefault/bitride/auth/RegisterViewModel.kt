package com.undefault.bitride.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.repository.UserRepository
import com.undefault.bitride.util.runWithGms
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun registerCustomer(nikHash: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            runWithGms(context, {
                val success = userRepository.createCustomerProfile(nikHash)
                onResult(success)
            }, {
                onResult(false)
            })
        }
    }

    fun registerDriver(nikHash: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            runWithGms(context, {
                val success = userRepository.createDriverProfile(nikHash)
                onResult(success)
            }, {
                onResult(false)
            })
        }
    }
}

