package com.undefault.bitride.mapdownload

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnAttach
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import app.organicmaps.R
import app.organicmaps.downloader.CountrySuggestFragment
import app.organicmaps.sdk.downloader.MapManager
import com.undefault.bitride.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MapDownloadScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (MapManager.nativeGetDownloadedCount() > 0) {
            navController.navigate(Routes.CHOOSE_ROLE) {
                popUpTo(Routes.MAP_DOWNLOAD) { inclusive = true }
            }
        } else {
            while (isActive) {
                delay(1000)
                if (MapManager.nativeGetDownloadedCount() > 0) {
                    navController.navigate(Routes.CHOOSE_ROLE) {
                        popUpTo(Routes.MAP_DOWNLOAD) { inclusive = true }
                    }
                    break
                }
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            FragmentContainerView(ctx).apply { id = R.id.map_download_container }
        },
        update = { view ->
            val activity = context as FragmentActivity
            val fm = activity.supportFragmentManager
            if (fm.findFragmentById(view.id) == null) {
                view.doOnAttach {
                    fm.beginTransaction()
                        .replace(view.id, CountrySuggestFragment())
                        .commitNow()
                }
            }
        }
    )
}

