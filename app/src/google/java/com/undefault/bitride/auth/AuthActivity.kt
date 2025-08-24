package com.undefault.bitride.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.organicmaps.base.BaseMwmFragmentActivity
import app.organicmaps.downloader.CountrySuggestFragment
import app.organicmaps.sdk.downloader.MapManager
import com.undefault.bitride.navigation.AppNavigation
import com.undefault.bitride.ui.theme.BitrideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseMwmFragmentActivity() {
    override fun getFragmentClass(): Class<out Fragment>? = null

    override fun onSafeCreate(savedInstanceState: Bundle?) {
        super.onSafeCreate(savedInstanceState)
        if (MapManager.nativeGetDownloadedCount() == 0) {
            supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentDetached(fm: FragmentManager, fragment: Fragment) {
                    if (fragment is CountrySuggestFragment) {
                        showContent()
                    }
                }
            }, false)
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, CountrySuggestFragment(), CountrySuggestFragment::class.java.name)
                .commitAllowingStateLoss()
        } else {
            showContent()
        }
    }
    private fun showContent() {
        setContent {
            BitrideTheme {
                AppNavigation()
            }
        }
    }
}
