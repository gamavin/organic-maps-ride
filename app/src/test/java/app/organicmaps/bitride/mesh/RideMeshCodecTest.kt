package app.organicmaps.bitride.mesh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RideMeshCodecTest {

  @Suppress("PropertyName")
  private val H =
    "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"

  @Test
  fun encodeDecodeRequest() {
    val req = RideRequest(
      hashHex = H,
      vehicle = VehicleType.MOTOR,
      pickup = GeoPoint(-6.175392, 106.827153),
      destination = GeoPoint(-6.121435, 106.774124),
      priceRp = 25000,
      toll = false,
      totalRides = 10,
      uniqueDrivers = 7,
      positive = 9,
      negative = 0,
      askCancel = 1,
      payment = "Cash",
      note = "note",
      pickupName = "Monas",
      destinationName = "Sunda"
    )

    val s = RideMeshCodec.encodeRequest(req)
    assertTrue(s.startsWith("BR1|"))
    // di JUnit4 urutannya: message dulu, baru condition
    assertTrue("len=${s.length}", s.length <= 352)

    val p = RideMeshCodec.decodeRequest(s)!!
    assertEquals(req.hashHex, p.hashHex)
    assertEquals(req.vehicle, p.vehicle)
    assertEquals(req.priceRp, p.priceRp)
    assertEquals(req.pickupName, p.pickupName)
    assertEquals(req.destinationName, p.destinationName)
  }

  @Test
  fun encodeDecodeReply() {
    val r = DriverReply(
      'D',
      H,
      VehicleType.CAR,
      40000,
      ok = true
    )
    val s = RideMeshCodec.encodeDriverReply(r)
    val p = RideMeshCodec.decodeDriverReply(s)!!
    assertEquals(r.vehicle, p.vehicle)
    assertEquals(40000, p.priceRp)
    assertTrue(p.ok)
  }

  @Test
  fun encodeDecodeConfirm() {
    val c = RideConfirm(H, true)
    val s = RideMeshCodec.encodeConfirm(c)
    val p = RideMeshCodec.decodeConfirm(s)!!
    assertEquals(H, p.hashHex)
    assertTrue(p.ok)
  }
}
