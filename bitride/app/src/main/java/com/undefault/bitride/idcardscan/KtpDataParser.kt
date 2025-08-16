package com.undefault.bitride.idcardscan

// Data class untuk menyimpan hasil parsing
data class KtpData(val nik: String?, val nama: String?)

/**
 * Fungsi untuk mem-parsing teks mentah dari KTP dan mengambil NIK & Nama. (REVISI 2)
 * @param rawText Teks lengkap hasil scan ML Kit.
 * @return Objek KtpData yang berisi NIK dan Nama yang berhasil ditemukan.
 */
fun parseKtpText(rawText: String): KtpData {
    // Memecah teks menjadi baris-baris dan menghapus baris kosong
    val lines = rawText.lines().filter { it.isNotBlank() }

    var nik: String? = null
    var nama: String? = null
    var nikLineIndex = -1

    // --- Langkah 1: Cari NIK dan lokasinya (indeks baris) ---
    // Ini adalah pola yang paling unik dan bisa diandalkan.
    for ((index, line) in lines.withIndex()) {
        // Membersihkan baris dan hanya menyisakan digit
        val potentialNik = line.replace(Regex("[^0-9]"), "")
        if (potentialNik.length == 16) {
            nik = potentialNik
            nikLineIndex = index
            break // Langsung hentikan jika NIK valid ditemukan
        }
    }

    // --- Langkah 2: Cari Nama berdasarkan posisinya setelah NIK ---
    // Ini adalah strategi utama karena posisi nama sangat konsisten.
    if (nikLineIndex != -1 && nikLineIndex + 1 < lines.size) {
        val potentialName = lines[nikLineIndex + 1].trim()

        // Fungsi bantu untuk memvalidasi apakah sebuah string kemungkinan besar adalah nama
        fun isLikelyName(text: String): Boolean {
            if (text.length < 3) return false
            // Nama tidak boleh mengandung keyword field KTP lain
            val keywords = listOf("Tempat", "Lahir", "Jenis", "Alamat", "Agama", "Status", "Pekerjaan", "WNI", "Berlaku", "PROVINSI", "KOTA", "KABUPATEN")
            if (keywords.any { text.contains(it, ignoreCase = true) }) {
                return false
            }
            // Nama biasanya hanya terdiri dari huruf kapital dan spasi (mungkin titik untuk gelar)
            return text.any { it.isLetter() }
        }

        if (isLikelyName(potentialName)) {
            nama = potentialName
        }
    }

    // --- Langkah 3: Fallback (Rencana Cadangan) ---
    // Jalankan ini HANYA JIKA strategi posisi gagal.
    if (nama == null) {
        val namaKeywordIndex = lines.indexOfFirst { it.contains("Nama", ignoreCase = true) }
        if (namaKeywordIndex != -1) {
            // Coba ambil dari baris yang sama, setelah ":"
            var nameCandidate = lines[namaKeywordIndex].substringAfter(":").trim()

            // Jika di baris yang sama kosong atau tidak valid, coba baris berikutnya
            if (nameCandidate.length <= 2 && namaKeywordIndex + 1 < lines.size) {
                nameCandidate = lines[namaKeywordIndex + 1].trim()
            }

            if (nameCandidate.length > 2) {
                nama = nameCandidate
            }
        }
    }

    // --- Langkah 4: Pembersihan Final ---
    // Membersihkan nama dari teks field lain yang mungkin menempel di akhir.
    nama?.let {
        var cleanedName = it
        val keywordsToCut = listOf("Tempat/Tgl Lahir", "Tempat", "Jenis kelamin", "Gol. Darah")
        for (keyword in keywordsToCut) {
            if (cleanedName.contains(keyword, ignoreCase = true)) {
                cleanedName = cleanedName.substringBefore(keyword).trim()
            }
        }
        nama = cleanedName.ifBlank { null }
    }

    return KtpData(nik, nama)
}
