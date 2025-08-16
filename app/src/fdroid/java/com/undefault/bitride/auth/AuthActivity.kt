package com.undefault.bitride.auth

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * FDroid stub untuk AuthActivity.
 * Tidak menampilkan UI apa pun, langsung mengembalikan RESULT_OK agar alur caller lanjut.
 */
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_OK, intent)
        finish()
        overridePendingTransition(0, 0)
    }
}
