package app.organicmaps.bitride.mesh

import app.organicmaps.R
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MeshDebugActivity : AppCompatActivity(), RideMeshListener {

  private var svc: MeshService? = null
  private var bound = false

  private val msgs = ArrayList<String>()
  private lateinit var list: ArrayAdapter<String>

  private lateinit var txtMyPeerId: TextView
  private var currentPeerId: String = ""
  private lateinit var spnPeers: Spinner
  private val peers = ArrayList<String>()
  private lateinit var peersAdapter: ArrayAdapter<String>

  private val REQ_BLE = 1001

  private val conn = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
      bound = true
      svc = (binder as MeshService.MeshBinder).service
      svc?.setListener(this@MeshDebugActivity)
      // Service hanya start kalau izin sudah clear (alur dikunci di Activity)
      svc?.startMesh("#bitride")
      toast("Bound to MeshService")
      txtMyPeerId.postDelayed({ refreshPeerId() }, 600)
    }
    override fun onServiceDisconnected(name: ComponentName) { bound = false; svc = null }
  }

  // Dialog sistem: minta user menyalakan Bluetooth bila OFF
  private val enableBtLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { res ->
    if (res.resultCode == Activity.RESULT_OK) continueAfterBtOn()
    else toast("Bluetooth perlu dinyalakan agar mesh bekerja.")
  }

  // Receiver: Service minta Activity menampilkan dialog permission (backup)
  private val permsReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (intent.action == MeshService.ACTION_PERMS_NEEDED) {
        val missing = intent.getStringArrayExtra(MeshService.EXTRA_MISSING_PERMS) ?: emptyArray()
        if (missing.isNotEmpty()) {
          ActivityCompat.requestPermissions(this@MeshDebugActivity, missing, REQ_BLE)
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_mesh_debug)

    txtMyPeerId = findViewById(R.id.txtMyPeerId)
    list = ArrayAdapter(this, android.R.layout.simple_list_item_1, msgs)
    findViewById<ListView>(R.id.listIncoming).adapter = list

    spnPeers = findViewById(R.id.spnPeers)
    peersAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, peers)
    peersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spnPeers.adapter = peersAdapter
    spnPeers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        findViewById<EditText>(R.id.edtPeerId).setText(peers[position])
      }
      override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    // UI handlers
    findViewById<Button>(R.id.btnCopyPeerId).setOnClickListener {
      val id = currentPeerId.trim()
      if (id.isNotEmpty()) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("peerId", id))
        toast("Peer ID copied")
      } else toast("Peer ID kosong")
    }
    findViewById<Button>(R.id.btnStart).setOnClickListener { autoStart() }
    findViewById<Button>(R.id.btnStop).setOnClickListener {
      svc?.stopMesh(); unbindSafe(); txtMyPeerId.text = "My Peer ID: -"; currentPeerId = ""
    }
    findViewById<Button>(R.id.btnSendChannel).setOnClickListener {
      val t = findViewById<EditText>(R.id.edtChannelText).text.toString()
      if (t.isNotEmpty()) svc?.sendChannelMessage(t) else toast("Channel text kosong")
    }
    findViewById<Button>(R.id.btnSendPrivate).setOnClickListener {
      val peer = findViewById<EditText>(R.id.edtPeerId).text.toString()
      val t = findViewById<EditText>(R.id.edtPrivateText).text.toString()
      if (peer.isEmpty()) { toast("Peer ID kosong"); return@setOnClickListener }
      if (t.isEmpty()) { toast("Private text kosong"); return@setOnClickListener }
      svc?.sendPrivateMessage(peer, t)
    }
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
      if (peer.isEmpty()) { toast("Peer ID kosong"); return@setOnClickListener }
      val r = DriverReply(
        'D',
        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        VehicleType.CAR,
        40000,
        ok = true
      )
      svc?.sendPrivateMessage(peer, RideMeshCodec.encodeDriverReply(r))
    }
    findViewById<Button>(R.id.btnSendCF1).setOnClickListener {
      val peer = findViewById<EditText>(R.id.edtPeerId).text.toString()
      if (peer.isEmpty()) { toast("Peer ID kosong"); return@setOnClickListener }
      val c = RideConfirm("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", true)
      svc?.sendPrivateMessage(peer, RideMeshCodec.encodeConfirm(c))
    }

    // Start otomatis on first open
    autoStart()
  }

  override fun onStart() {
    super.onStart()
    val filter = IntentFilter(MeshService.ACTION_PERMS_NEEDED)
    // Pakai ContextCompat agar selalu ada flag & aman lint + runtime
    ContextCompat.registerReceiver(this, permsReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
  }

  override fun onStop() {
    super.onStop()
    try { unregisterReceiver(permsReceiver) } catch (_: Exception) {}
  }

  /** Alur otomatis: cek BT → cek permission → start service & bind. */
  private fun autoStart() {
    val bt = BluetoothAdapter.getDefaultAdapter()
    if (bt == null) { toast("Perangkat tidak mendukung Bluetooth"); return }

    if (!bt.isEnabled) {
      enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
      return
    }

    val missing = BlePermissionHelper.missingPermissions(this)
    if (missing.isNotEmpty()) {
      ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQ_BLE)
      return
    }

    startAndBindService()
  }

  private fun continueAfterBtOn() {
    val missing = BlePermissionHelper.missingPermissions(this)
    if (missing.isNotEmpty()) {
      ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQ_BLE)
    } else startAndBindService()
  }

  private fun startAndBindService() {
    MeshService.start(this)
    bindService(Intent(this, MeshService::class.java), conn, BIND_AUTO_CREATE)
  }

  private fun refreshPeerId() {
    val id = svc?.peerId ?: ""
    currentPeerId = id
    txtMyPeerId.text = if (id.isNotEmpty()) "My Peer ID: $id" else "My Peer ID: -"
  }

  private fun unbindSafe() { if (bound) try { unbindService(conn) } catch (_: Exception) {}.also { bound = false } }

  override fun onDestroy() { svc?.setListener(null); unbindSafe(); super.onDestroy() }

  override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, res: IntArray) {
    super.onRequestPermissionsResult(code, perms, res)
    if (code == REQ_BLE) {
      if (BlePermissionHelper.hasAll(this)) {
        toast("Permissions granted. Starting mesh…")
        if (svc != null) {
          svc?.startMesh("#bitride")
        } else {
          startAndBindService()
        }
      } else {
        if (BlePermissionHelper.somePermissionPermanentlyDenied(this)) {
          toast("Izin ditolak permanen. Buka Settings untuk mengaktifkan.")
          BlePermissionHelper.openAppDetailsSettings(this)
        } else {
          toast("Permissions ditolak. Nearby Devices/Location diperlukan.")
        }
      }
    }
  }

  private fun addLine(s: String) {
    runOnUiThread {
      msgs.add(0, s)
      list.notifyDataSetChanged()
      toast(s)
    }
  }
  private fun toast(s: String) = runOnUiThread {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
  }

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

  override fun onChannelMessage(text: String, senderPeerId: String) {
    addLine("CH from ${senderPeerId.takeLast(6)}: $text")
  }

  override fun onPeerListUpdated(peers: List<String>) {
    runOnUiThread {
      this.peers.clear()
      this.peers.addAll(peers)
      peersAdapter.notifyDataSetChanged()
    }
  }

  override fun onHandshakeStatus(peerId: String, success: Boolean) {
    val status = if (success) "Handshake selesai" else "Handshake gagal"
    addLine("$status dengan ${peerId.takeLast(6)}")
  }
}
