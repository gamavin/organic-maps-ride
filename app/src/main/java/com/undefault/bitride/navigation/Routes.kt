package com.undefault.bitride.navigation

/**
 * Kumpulan konstanta rute yang digunakan pada sistem navigasi BitRide.
 */
object Routes {
    const val AUTH = "auth"
    const val CHOOSE_ROLE = "choose_role"
    const val CUSTOMER_REGISTRATION_FORM = "customer_registration_form"
    const val DRIVER_REGISTRATION_FORM = "driver_registration_form"
    const val MAIN = "main"
    const val IMPORT = "import"

    /**
     * Fungsi utilitas untuk menyesuaikan rute scan KTP dengan target
     * formulir pendaftaran sesuai peran.
     */
    fun idCardScanWithArgs(role: String, @Suppress("UNUSED_PARAMETER") fromImport: Boolean): String =
        when (role.lowercase()) {
            "customer" -> CUSTOMER_REGISTRATION_FORM
            "driver" -> DRIVER_REGISTRATION_FORM
            else -> CHOOSE_ROLE
        }
}
