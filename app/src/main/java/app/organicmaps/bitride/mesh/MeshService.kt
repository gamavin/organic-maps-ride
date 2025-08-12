package app.organicmaps.bitride.mesh

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bitchat.android.mesh.BluetoothMeshDelegate
import com.bitchat.android.mesh.BluetoothMeshService
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.ui.ChannelManager
import com.bitchat.android.ui.ChatState
import com.bitchat.android.ui.DataManager
import com.bitchat.android.ui.MessageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Foreground service pembungkus mesh.
 */
class MeshService : Service() {

  companion object {
    private const val CH_ID = "bitride_mesh_channel"
    private const val NOTIF_ID = 41

    fun start(ctx: Context) {
      val i = Intent(ctx, MeshService::class.java)
      if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
    }
    fun stop(ctx: Context) {
      ctx.stopService(Intent(ctx, MeshService::class.java))
    }
  }

  private lateinit var mesh: BluetoothMeshService
  private var listener: RideMeshListener? = null
  private var joinedChannel = false
  private var myPeerId: String = ""

  inner class MeshBinder : Binder() { val service get() = this@MeshService }
  private val binder = MeshBinder()

  override fun onBind(intent: Intent?): IBinder = binder

  override fun onCreate() {
    super.onCreate()
    createChannel()
    startForeground(NOTIF_ID, buildNotif())
  }

  override fun onDestroy() {
    if (::mesh.isInitialized) {
      try { mesh.stopServices() } catch (_: Exception) {}
    }
    joinedChannel = false
    super.onDestroy()
  }

  private fun createChannel() {
    if (Build.VERSION.SDK_INT >= 26) {
      val nm = getSystemService(NotificationManager::class.java)
      if (nm.getNotificationChannel(CH_ID) == null) {
        val ch = NotificationChannel(
          CH_ID,
          getStringResource("mesh_channel_name"),
          NotificationManager.IMPORTANCE_LOW
        )
        ch.description = getStringResource("mesh_channel_desc")
        nm.createNotificationChannel(ch)
      }
    }
  }

  private fun buildNotif(): Notification {
    val title = getStringResource("mesh_notif_title")
    val text = getStringResource("mesh_notif_text")
    return NotificationCompat.Builder(this, CH_ID)
      .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
      .setContentTitle(title)
      .setContentText(text)
      .setOngoing(true)
      .build()
  }

  private fun getStringResource(name: String): String {
    val id = resources.getIdentifier(name, "string", packageName)
    return if (id != 0) getString(id) else name
  }

  // ------- API yang dipanggil UI/manager -------
  fun startMesh(channel: String = "#bitride") {
    if (isRunning) return
    Log.d("MeshService", "startMesh")
    val missing = BlePermissionHelper.requiredPermissions().any {
      checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
    }
    if (missing) Log.w("MeshService", "permission not granted")

    mesh = BluetoothMeshService(applicationContext)
    mesh.delegate = object : BluetoothMeshDelegate {
      override fun didReceiveMessage(message: BitchatMessage) {
        val from = message.senderPeerID ?: return
        if (message.isPrivate) {
          Log.d("MeshService", "recv private from $from: ${message.content}")
          when (RideMeshCodec.kindOf(message.content)) {
            RideMessageKind.REPLY ->
              RideMeshCodec.decodeDriverReply(message.content)?.let {
                listener?.onDriverReply(it, from)
              }
            RideMessageKind.CONFIRM ->
              RideMeshCodec.decodeConfirm(message.content)?.let {
                listener?.onConfirm(it, from)
              }
            else -> {}
          }
        } else if (message.channel == "#bitride") {
          Log.d("MeshService", "recv channel from $from: ${message.content}")
          when (RideMeshCodec.kindOf(message.content)) {
            RideMessageKind.REQUEST ->
              RideMeshCodec.decodeRequest(message.content)?.let {
                listener?.onRideRequestFromCustomer(it, from)
              }
            else -> {}
          }
        }
      }
      override fun didUpdatePeerList(peers: List<String>) {}
      override fun didReceiveChannelLeave(channel: String, fromPeer: String) {}
      override fun didReceiveDeliveryAck(ack: com.bitchat.android.model.DeliveryAck) {}
      override fun didReceiveReadReceipt(receipt: com.bitchat.android.model.ReadReceipt) {}
      override fun decryptChannelMessage(encryptedContent: ByteArray, channel: String): String? = null
      override fun getNickname(): String? = null
      override fun isFavorite(peerID: String): Boolean = false
    }

    mesh.startServices()
    myPeerId = mesh.myPeerID
    Log.d("MeshService", "myPeerId=$myPeerId")

    val state = ChatState()
    val data = DataManager(applicationContext)
    val mgr = MessageManager(state)
    val cm = ChannelManager(state, mgr, data, CoroutineScope(Dispatchers.IO))
    joinedChannel = cm.joinChannel(channel, null, myPeerId)
    Log.d("MeshService", "join $channel: $joinedChannel")
  }

  fun stopMesh() {
    if (::mesh.isInitialized) {
      Log.d("MeshService", "stopMesh")
      try { mesh.stopServices() } catch (_: Exception) {}
      mesh.delegate = null
    }
    joinedChannel = false
    stopSelf()
  }

  fun sendChannelMessage(content: String) {
    if (!isRunning) return
    Log.d("MeshService", "send channel: $content")
    mesh.sendMessage(content, emptyList(), "#bitride")
  }

  fun sendPrivateMessage(peerId: String, content: String) {
    if (!isRunning) return
    Log.d("MeshService", "send private to $peerId: $content")
    mesh.sendPrivateMessage(content, peerId, peerId)
  }

  fun setListener(listener: RideMeshListener?) { this.listener = listener }

  val isRunning: Boolean
    get() = ::mesh.isInitialized && joinedChannel
}
