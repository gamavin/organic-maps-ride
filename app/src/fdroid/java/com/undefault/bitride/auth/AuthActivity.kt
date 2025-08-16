package com.undefault.bitride.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * FDroid stub: alihkan ke Activity peta Organic Maps.
 * Tidak menampilkan UI & tidak menarik dependensi Google/Compose.
 */
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kandidat target utama + fallback
        val targets = listOf(
            "app.organicmaps.MwmActivity",               // main map (umumnya ada)
            "app.organicmaps.SplashActivity",            // fallback
            "app.organicmaps.DownloadResourcesActivity"  // fallback lain
        )

        var launched = false
        for (cls in targets) {
            try {
                val intent = Intent().setClassName(this, cls)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                launched = true
                break
            } catch (_: Throwable) {
                // coba target berikutnya
            }
        }

        if (!launched) {
            // Jika semua gagal, pulangkan RESULT_OK agar caller (bila ada) bisa lanjut.
            setResult(RESULT_OK, intent)
        }

        finish()
        overridePendingTransition(0, 0)
    }
}
