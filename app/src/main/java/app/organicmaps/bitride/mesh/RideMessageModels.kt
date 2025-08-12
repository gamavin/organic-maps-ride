package app.organicmaps.bitride.mesh

enum class RideMessageKind { REQUEST, REPLY, CONFIRM, UNKNOWN }

enum class VehicleType(val code: Char) {
  MOTOR('m'), CAR('c');
  companion object { fun fromCode(c: Char) = entries.firstOrNull { it.code == c } }
}

data class GeoPoint(val lat: Double, val lon: Double)

data class RideRequest(
  val role: Char = 'C',
  val hashHex: String,
  val vehicle: VehicleType,
  val pickup: GeoPoint,
  val destination: GeoPoint,
  val priceRp: Int,
  val toll: Boolean,
  val totalRides: Int,
  val uniqueDrivers: Int,
  val positive: Int,
  val negative: Int,
  val askCancel: Int
)

data class DriverReply(
  val role: Char = 'D',
  val hashHex: String,
  val vehicle: VehicleType,
  val priceRp: Int,
  val ok: Boolean
)

data class RideConfirm(
  val hashHex: String,
  val ok: Boolean = true
)
