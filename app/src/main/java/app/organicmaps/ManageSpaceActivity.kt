package app.organicmaps

import android.app.Activity
import android.os.Bundle

/**
 * Placeholder untuk "Manage Space" (pembersihan cache, dsb).
 * Sekarang langsung selesai; nanti bisa diisi UI beneran kalau perlu.
 */
class ManageSpaceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tutup saja; Organic Maps biasanya pakai pengelolaan storage internal.
        finish()
    }
}
