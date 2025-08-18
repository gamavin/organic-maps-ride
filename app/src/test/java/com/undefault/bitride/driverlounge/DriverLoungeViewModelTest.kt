package com.undefault.bitride.driverlounge

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import app.organicmaps.bitride.mesh.BlePermissionHelper
import app.organicmaps.bitride.mesh.GeoPoint
import app.organicmaps.bitride.mesh.RideRequest
import app.organicmaps.bitride.mesh.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic

class DriverLoungeViewModelTest {
  @Test
  fun rideRequestUpdatesList() {
    val app = Mockito.mock(Application::class.java)
    Mockito.doReturn(ComponentName("",""))
      .`when`(app).startService(any(Intent::class.java))
    Mockito.doReturn(true)
      .`when`(app).bindService(any(Intent::class.java), any(ServiceConnection::class.java), Mockito.anyInt())

    mockStatic(BlePermissionHelper::class.java).use { ble ->
      ble.`when`<List<String>> { BlePermissionHelper.missingPermissions(Mockito.any(Context::class.java)) }
        .thenReturn(emptyList())
      ble.`when`<Boolean> { BlePermissionHelper.hasAll(Mockito.any(Context::class.java)) }
        .thenReturn(true)

      val vm = DriverLoungeViewModel(app)

      val req = RideRequest(
        hashHex = "0123",
        vehicle = VehicleType.MOTOR,
        pickup = GeoPoint(0.0,0.0),
        destination = GeoPoint(1.0,1.0),
        priceRp = 10000,
        toll = false,
        totalRides = 0,
        uniqueDrivers = 0,
        positive = 0,
        negative = 0,
        askCancel = 0
      )

      vm.onRideRequestFromCustomer(req, "peer")
      assertEquals(listOf(req), vm.requests.value)
    }
  }
}
