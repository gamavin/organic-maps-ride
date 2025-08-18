package app.organicmaps.bitride

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import com.undefault.bitride.data.repository.UserProfileRepository
import com.undefault.bitride.data.repository.UserProfileStats

@HiltViewModel
class RideRequestViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    fun getStatsBlocking(): UserProfileStats = runBlocking {
        userProfileRepository.getActiveUserStats()
    }
}
