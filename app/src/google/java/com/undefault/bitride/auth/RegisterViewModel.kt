package com.undefault.bitride.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.undefault.bitride.data.model.CustomerProfile
import com.undefault.bitride.data.model.DriverProfile
import com.undefault.bitride.data.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    // PASS Firestore ke constructor
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())

    /**
     * Fungsi yang dipanggil setelah NIK dan Nama berhasil di-scan dan di-hash.
     */
    fun onRegistrationSubmit(hashedNik: String, userName: String, userType: String) {
        viewModelScope.launch {
            if (userType.equals("D", ignoreCase = true)) {
                val roleExists = userRepository.doesRoleExist(hashedNik, "driver")
                if (roleExists) {
                    println("Error: Akun Driver dengan NIK ini sudah terdaftar!")
                    return@launch
                }
                val profile = DriverProfile(name = userName)
                val success = userRepository.createDriverProfile(hashedNik, profile)
                if (success) {
                    println("Pendaftaran profil Driver berhasil untuk: $hashedNik")
                } else {
                    println("Error: Pendaftaran profil Driver gagal!")
                }
            } else if (userType.equals("C", ignoreCase = true)) {
                val roleExists = userRepository.doesRoleExist(hashedNik, "customer")
                if (roleExists) {
                    println("Error: Akun Customer dengan NIK ini sudah terdaftar!")
                    return@launch
                }
                val profile = CustomerProfile(name = userName)
                val success = userRepository.createCustomerProfile(hashedNik, profile)
                if (success) {
                    println("Pendaftaran profil Customer berhasil untuk: $hashedNik")
                } else {
                    println("Error: Pendaftaran profil Customer gagal!")
                }
            } else {
                println("Error: Tipe pengguna tidak valid: $userType")
            }
        }
    }
}
