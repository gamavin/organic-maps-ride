package app.organicmaps.bitride.mesh

import android.Manifest
import android.os.Build

object BlePermissionHelper {
  fun requiredPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= 31) arrayOf(
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_ADVERTISE,
    Manifest.permission.BLUETOOTH_CONNECT
  ) else arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION
  )
}
