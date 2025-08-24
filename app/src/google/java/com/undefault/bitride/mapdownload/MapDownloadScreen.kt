package com.undefault.bitride.mapdownload

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnAttach
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import app.organicmaps.R
import app.organicmaps.downloader.CountrySuggestFragment
import app.organicmaps.MwmApplication
import app.organicmaps.sdk.downloader.CountryItem
import app.organicmaps.sdk.downloader.MapManager
import com.undefault.bitride.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MapDownloadScreen(navController: NavController) {
    val context = LocalContext.current
    var showSuggest by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        suspend fun isMapReady(): Boolean {
            val location = MwmApplication.from(context).getLocationHelper().getSavedLocation()
            val countryId = location?.let { MapManager.nativeFindCountry(it.latitude, it.longitude) }
            return !countryId.isNullOrEmpty() &&
                    MapManager.nativeGetStatus(countryId) == CountryItem.STATUS_DONE
        }

        if (isMapReady()) {
            showSuggest = false
            navController.navigate(Routes.CHOOSE_ROLE) {
                popUpTo(Routes.MAP_DOWNLOAD) { inclusive = true }
            }
        } else {
            while (isActive) {
                delay(1000)
                if (isMapReady()) {
                    showSuggest = false
                    navController.navigate(Routes.CHOOSE_ROLE) {
                        popUpTo(Routes.MAP_DOWNLOAD) { inclusive = true }
                    }
                    break
                }
            }
        }
    }

    if (showSuggest) {
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
}

