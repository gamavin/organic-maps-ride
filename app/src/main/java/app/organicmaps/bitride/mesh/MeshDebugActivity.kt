package app.organicmaps.bitride.mesh
import app.organicmaps.R

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MeshDebugActivity : AppCompatActivity(), RideMeshListener {

  private var svc: MeshService? = null
  private val msgs = ArrayList<String>()
  private lateinit var list: ArrayAdapter<String>

  private val conn = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
      svc = (binder as MeshService.MeshBinder).service
      svc?.setListener(this@MeshDebugActivity)
      svc?.startMesh("#bitride")
      toast("Bound to MeshService")
    }
    override fun onServiceDisconnected(name: ComponentName) { svc = null }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_mesh_debug)

    list = ArrayAdapter(this, android.R.layout.simple_list_item_1, msgs)
    findViewById<ListView>(R.id.listIncoming).adapter = list

    val perms = BlePermissionHelper.requiredPermissions()
    val need = perms.any { checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
    if (need) requestPermissions(perms, 1001)

    findViewById<Button>(R.id.btnStart).setOnClickListener {
      MeshManager.start(this); bindService(Intent(this, MeshService::class.java), conn, BIND_AUTO_CREATE)
    }
    findViewById<Button>(R.id.btnStop).setOnClickListener {
      svc?.stopMesh(); unbindSafe()
    }
    findViewById<Button>(R.id.btnSendChannel).setOnClickListener {
      val t = findViewById<EditText>(R.id.edtChannelText).text.toString()
      svc?.sendChannelMessage(t)
    }
    findViewById<Button>(R.id.btnSendPrivate).setOnClickListener {
      val peer = findViewById<EditText>(R.id.edtPeerId).text.toString()
      val t = findViewById<EditText>(R.id.edtPrivateText).text.toString()
      svc?.sendPrivateMessage(peer, t)
    }

    // Tombol uji BR1/RP1/CF1
    findViewById<Button>(R.id.btnSendBR1).setOnClickListener {
      val req = RideRequest(
        hashHex = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        vehicle = VehicleType.MOTOR,
        pickup = GeoPoint(-6.175392,106.827153),
        destination = GeoPoint(-6.121435,106.774124),
        priceRp = 25000, toll = false, totalRides = 10, uniqueDrivers = 7, positive = 9, negative = 0, askCancel = 1
      )
      svc?.sendChannelMessage(RideMeshCodec.encodeRequest(req))
    }
    findViewById<Button>(R.id.btnSendRP1).setOnClickListener {
      val peer = findViewById<EditText>(R.id.edtPeerId).text.toString()
      val r = DriverReply('D',
        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        VehicleType.CAR, 40000, ok = true)
      svc?.sendPrivateMessage(peer, RideMeshCodec.encodeDriverReply(r))
    }
    findViewById<Button>(R.id.btnSendCF1).setOnClickListener {
      val peer = findViewById<EditText>(R.id.edtPeerId).text.toString()
      val c = RideConfirm("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", true)
      svc?.sendPrivateMessage(peer, RideMeshCodec.encodeConfirm(c))
    }
  }

  private fun unbindSafe() {
    try { unbindService(conn) } catch (_: Exception) {}
  }

  override fun onDestroy() {
    svc?.setListener(null)
    unbindSafe()
    super.onDestroy()
  }

  override fun onRequestPermissionsResult(code:Int, perms:Array<out String>, res:IntArray) {
    super.onRequestPermissionsResult(code, perms, res)
    // Tidak ada aksi khusus; user bisa tap Start Mesh lagi setelah grant.
  }

  private fun addLine(s: String) { msgs.add(0, s); list.notifyDataSetChanged() }
  private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

  // ------- RideMeshListener --------
  override fun onRideRequestFromCustomer(req: RideRequest, senderPeerId: String) {
    addLine("BR1 from ${senderPeerId.takeLast(6)}: Rp${req.priceRp} ${req.vehicle}")
  }
  override fun onDriverReply(resp: DriverReply, senderPeerId: String) {
    addLine("RP1 from ${senderPeerId.takeLast(6)}: Rp${resp.priceRp} ok=${resp.ok}")
  }
  override fun onConfirm(confirm: RideConfirm, senderPeerId: String) {
    addLine("CF1 from ${senderPeerId.takeLast(6)} ok=${confirm.ok}")
  }
}
