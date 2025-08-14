package app.organicmaps.bitride.mesh

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object BlePermissionHelper {
  /** Samakan dengan BitChat: minta BT (sesuai API) + lokasi + notif (A13+). */
  fun requiredPermissions(): List<String> {
    val list = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= 31) {
      list += Manifest.permission.BLUETOOTH_SCAN
      list += Manifest.permission.BLUETOOTH_CONNECT
      list += Manifest.permission.BLUETOOTH_ADVERTISE
    } else {
      // Pre-S tetap butuh izin lokasi untuk scan BLE
      list += Manifest.permission.BLUETOOTH
      list += Manifest.permission.BLUETOOTH_ADMIN
    }
    // BitChat tetap minta lokasi (COARSE + FINE) di semua API
    list += Manifest.permission.ACCESS_COARSE_LOCATION
    list += Manifest.permission.ACCESS_FINE_LOCATION

    // A13+ butuh notif runtime agar FGS notifikasi tidak ke-block
    if (Build.VERSION.SDK_INT >= 33) {
      list += Manifest.permission.POST_NOTIFICATIONS
    }
    return list
  }

  fun missingPermissions(ctx: Context): List<String> =
    requiredPermissions().filter { ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED }

  fun hasAll(ctx: Context): Boolean = missingPermissions(ctx).isEmpty()

  /** true bila ada izin yang ditolak permanen (Don't ask again). */
  fun somePermissionPermanentlyDenied(activity: Activity): Boolean =
    missingPermissions(activity).any { !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }

  fun openAppDetailsSettings(activity: Activity) {
    val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", activity.packageName, null)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    activity.startActivity(i)
  }
}
