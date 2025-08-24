package com.undefault.bitride.downloader

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.organicmaps.base.BaseMwmFragmentActivity
import app.organicmaps.downloader.CountrySuggestFragment

class AutoDownloadActivity : BaseMwmFragmentActivity() {
    override fun getFragmentClass(): Class<out Fragment> = CountrySuggestFragment::class.java

    override fun onSafeCreate(savedInstanceState: Bundle?) {
        super.onSafeCreate(savedInstanceState)
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDetached(fm: FragmentManager, fragment: Fragment) {
                if (fragment is CountrySuggestFragment) {
                    finish()
                }
            }
        }, false)
    }
}
