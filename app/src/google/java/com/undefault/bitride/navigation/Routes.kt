package com.undefault.bitride.navigation

/**
 * Kumpulan konstanta rute yang digunakan pada sistem navigasi BitRide.
 */
object Routes {
    const val AUTH = "auth"
    const val CHOOSE_ROLE = "choose_role"
    const val ID_CARD_SCAN = "id_card_scan"
    const val CUSTOMER_REGISTRATION_FORM = "customer_registration_form"
    const val DRIVER_REGISTRATION_FORM = "driver_registration_form"
    const val DRIVER_LOUNGE = "driver_lounge"
    const val MAIN = "main"

    /**
     * Rute untuk membuka layar pemindaian KTP.
     */
    fun idCardScanWithArgs(role: String, isRescan: Boolean): String =
        "$ID_CARD_SCAN?role=$role&isRescan=$isRescan"

    /**
     * Rute untuk formulir pendaftaran customer dengan data hasil scan.
     */
    fun customerRegistrationWithArgs(nik: String?, name: String?): String {
        val params = listOfNotNull(
            nik?.let { "nik=$it" },
            name?.let { "name=$it" }
        ).joinToString("&")
        return if (params.isNotEmpty()) "$CUSTOMER_REGISTRATION_FORM?$params" else CUSTOMER_REGISTRATION_FORM
    }

    /**
     * Rute untuk formulir pendaftaran driver dengan data hasil scan.
     */
    fun driverRegistrationWithArgs(nik: String?, name: String?): String {
        val params = listOfNotNull(
            nik?.let { "nik=$it" },
            name?.let { "name=$it" }
        ).joinToString("&")
        return if (params.isNotEmpty()) "$DRIVER_REGISTRATION_FORM?$params" else DRIVER_REGISTRATION_FORM
    }
}
