package com.undefault.bitride.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.undefault.bitride.data.repository.LocalUserRepository
import com.undefault.bitride.data.repository.UserRepository
import app.organicmaps.BuildConfig
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    // Gunakan UserRepository dengan flag BuildConfig.USE_FIRESTORE
    private val userRepository = UserRepository(
        firestore = if (BuildConfig.USE_FIRESTORE) FirebaseFirestore.getInstance() else null,
        localRepository = LocalUserRepository(),
        useFirestore = BuildConfig.USE_FIRESTORE
    )

    /**
     * Fungsi yang dipanggil setelah NIK dan Nama berhasil di-scan dan di-hash.
     */
    fun onRegistrationSubmit(hashedNik: String, userName: String, userType: String) {
        viewModelScope.launch {
            if (userType.equals("D", ignoreCase = true)) {
                val roleExists = userRepository.doesRoleExist(hashedNik, "DRIVER")
                if (roleExists) {
                    println("Error: Akun Driver dengan NIK ini sudah terdaftar!")
                    return@launch
                }
                val success = userRepository.createDriverProfile(hashedNik)
                if (success) {
                    println("Pendaftaran profil Driver berhasil untuk: $hashedNik")
                } else {
                    println("Error: Pendaftaran profil Driver gagal!")
                }
            } else if (userType.equals("C", ignoreCase = true)) {
                val roleExists = userRepository.doesRoleExist(hashedNik, "CUSTOMER")
                if (roleExists) {
                    println("Error: Akun Customer dengan NIK ini sudah terdaftar!")
                    return@launch
                }
                val success = userRepository.createCustomerProfile(hashedNik)
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
