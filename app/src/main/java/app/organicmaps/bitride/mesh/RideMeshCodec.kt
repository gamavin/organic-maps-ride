package app.organicmaps.bitride.mesh

import java.util.Locale

/**
 * Channel: #bitride (public)
 * Payload (ASCII, <=352 char):
 * BR1|r=C;h=<64HEX>;v=<m|c>;p=<lat,lon>;d=<lat,lon>;pr=<int>;t=<0|1>;tr=<int>;ud=<int>;ps=<int>;ng=<int>;ac=<int>
 * RP1|r=D;h=<64HEX>;v=<m|c>;pr=<int>;ok=<0|1>
 * CF1|h=<64HEX>;ok=1
 */
object RideMeshCodec {
  private const val MAX_LEN = 352
  private val HEX64 = Regex("^[0-9a-fA-F]{64}$")
  private val LOCALE = Locale.US

  fun isRideMessage(raw: String) =
    raw.startsWith("BR1|") || raw.startsWith("RP1|") || raw.startsWith("CF1|")

  fun kindOf(raw: String) = when {
    raw.startsWith("BR1|") -> RideMessageKind.REQUEST
    raw.startsWith("RP1|") -> RideMessageKind.REPLY
    raw.startsWith("CF1|") -> RideMessageKind.CONFIRM
    else -> RideMessageKind.UNKNOWN
  }

  fun encodeRequest(x: RideRequest): String {
    require(x.hashHex.matches(HEX64)) { "hashHex must be 64 hex" }
    val p = formatPoint(x.pickup)
    val d = formatPoint(x.destination)
    val s = buildString {
      append("BR1|r=C;")
      append("h=${x.hashHex};")
      append("v=${x.vehicle.code};")
      append("p=$p;")
      append("d=$d;")
      append("pr=${x.priceRp};")
      append("t=${if (x.toll) 1 else 0};")
      append("tr=${x.totalRides};")
      append("ud=${x.uniqueDrivers};")
      append("ps=${x.positive};")
      append("ng=${x.negative};")
      append("ac=${x.askCancel}")
    }
    require(s.length <= MAX_LEN) { "payload too long (${s.length})" }
    return s
  }

  fun encodeDriverReply(x: DriverReply): String {
    require(x.hashHex.matches(HEX64)) { "hashHex must be 64 hex" }
    val s = "RP1|r=D;h=${x.hashHex};v=${x.vehicle.code};pr=${x.priceRp};ok=${if (x.ok) 1 else 0}"
    require(s.length <= MAX_LEN) { "payload too long (${s.length})" }
    return s
  }

  fun encodeConfirm(x: RideConfirm): String {
    require(x.hashHex.matches(HEX64)) { "hashHex must be 64 hex" }
    val s = "CF1|h=${x.hashHex};ok=${if (x.ok) 1 else 0}"
    require(s.length <= MAX_LEN) { "payload too long (${s.length})" }
    return s
  }

  fun decodeRequest(raw: String): RideRequest? {
    if (!raw.startsWith("BR1|")) return null
    val map = toMap(raw.removePrefix("BR1|"))
    val h = map["h"] ?: return null
    if (!HEX64.matches(h)) return null
    val v = map["v"]?.firstOrNull()?.let { VehicleType.fromCode(it) } ?: return null
    val p = map["p"]?.let { parsePoint(it) } ?: return null
    val d = map["d"]?.let { parsePoint(it) } ?: return null
    val pr = map["pr"]?.toIntOrNull() ?: return null
    val t = map["t"] == "1"
    val tr = map["tr"]?.toIntOrNull() ?: 0
    val ud = map["ud"]?.toIntOrNull() ?: 0
    val ps = map["ps"]?.toIntOrNull() ?: 0
    val ng = map["ng"]?.toIntOrNull() ?: 0
    val ac = map["ac"]?.toIntOrNull() ?: 0
    return RideRequest('C', h, v!!, p, d, pr, t, tr, ud, ps, ng, ac)
  }

  fun decodeDriverReply(raw: String): DriverReply? {
    if (!raw.startsWith("RP1|")) return null
    val map = toMap(raw.removePrefix("RP1|"))
    val h = map["h"] ?: return null
    if (!HEX64.matches(h)) return null
    val v = map["v"]?.firstOrNull()?.let { VehicleType.fromCode(it) } ?: return null
    val pr = map["pr"]?.toIntOrNull() ?: return null
    val ok = map["ok"] == "1"
    return DriverReply('D', h, v!!, pr, ok)
  }

  fun decodeConfirm(raw: String): RideConfirm? {
    if (!raw.startsWith("CF1|")) return null
    val map = toMap(raw.removePrefix("CF1|"))
    val h = map["h"] ?: return null
    if (!HEX64.matches(h)) return null
    val ok = map["ok"] == "1"
    return RideConfirm(h, ok)
  }

  private fun toMap(body: String): Map<String, String> =
    body.split(';').mapNotNull { s ->
      val i = s.indexOf('=')
      if (i <= 0) null else s.substring(0, i) to s.substring(i + 1)
    }.toMap()

  private fun parsePoint(s: String): GeoPoint {
    val parts = s.split(',')
    require(parts.size == 2) { "point must be lat,lon" }
    val lat = parts[0].toDoubleOrNull() ?: error("lat invalid")
    val lon = parts[1].toDoubleOrNull() ?: error("lon invalid")
    return GeoPoint(lat, lon)
  }

  private fun formatPoint(p: GeoPoint): String {
    fun fmt(d: Double) = String.format(LOCALE, "%.6f", d).trimEnd('0').trimEnd('.')
    return "${fmt(p.lat)},${fmt(p.lon)}"
  }
}
