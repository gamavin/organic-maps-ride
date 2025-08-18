package com.undefault.bitride.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undefault.bitride.data.model.UserProfile
import com.undefault.bitride.data.repository.UserProfileRepository
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserProfileRepository(application)

    fun interface Callback {
        fun onProfile(profile: UserProfile?)
    }

    fun fetchProfile(role: String, callback: Callback) {
        viewModelScope.launch {
            val profile = repository.getProfile(role)
            callback.onProfile(profile)
        }
    }
}
