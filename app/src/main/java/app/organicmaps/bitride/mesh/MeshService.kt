package app.organicmaps.bitride.mesh

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.bitchat.android.mesh.BluetoothMeshDelegate
import com.bitchat.android.mesh.BluetoothMeshService
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.model.DeliveryAck
import com.bitchat.android.model.ReadReceipt

/**
 * Service pembungkus BluetoothMeshService dari BitChat.
 * Bertugas memastikan permission BLE lengkap sebelum menjalankan mesh.
 */
class MeshService : Service() {
  companion object {
    const val ACTION_PERMS_NEEDED = "app.organicmaps.bitride.mesh.ACTION_PERMS_NEEDED"
    const val EXTRA_MISSING_PERMS = "app.organicmaps.bitride.mesh.EXTRA_MISSING_PERMS"

    fun start(context: Context) {
      val missing = BlePermissionHelper.missingPermissions(context)
      if (missing.isNotEmpty()) {
        context.sendBroadcast(Intent(ACTION_PERMS_NEEDED).apply {
          putExtra(EXTRA_MISSING_PERMS, missing.toTypedArray())
        })
        return
      }
      context.startService(Intent(context, MeshService::class.java))
    }

    fun stop(context: Context) {
      context.stopService(Intent(context, MeshService::class.java))
    }
  }

  private val binder = MeshBinder()
  private var mesh: BluetoothMeshService? = null
  private var listener: RideMeshListener? = null
  private var channel: String? = null

  inner class MeshBinder : Binder() {
    val service: MeshService
      get() = this@MeshService
  }

  override fun onBind(intent: Intent?): IBinder = binder

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (!BlePermissionHelper.hasAll(this)) {
      val missing = BlePermissionHelper.missingPermissions(this).toTypedArray()
      sendBroadcast(Intent(ACTION_PERMS_NEEDED).apply {
        putExtra(EXTRA_MISSING_PERMS, missing)
      })
      return START_NOT_STICKY
    }

    if (mesh == null) {
      mesh = BluetoothMeshService(this).apply {
        delegate = object : BluetoothMeshDelegate {
          override fun didReceiveMessage(message: BitchatMessage) {
            val raw = message.content
            if (!RideMeshCodec.isRideMessage(raw)) return
            when (RideMeshCodec.kindOf(raw)) {
              RideMessageKind.REQUEST -> {
                RideMeshCodec.decodeRequest(raw)?.let { req ->
                  message.senderPeerID?.let { sender ->
                    listener?.onRideRequestFromCustomer(req, sender)
                  }
                }
              }
              RideMessageKind.REPLY -> {
                RideMeshCodec.decodeDriverReply(raw)?.let { reply ->
                  message.senderPeerID?.let { sender ->
                    listener?.onDriverReply(reply, sender)
                  }
                }
              }
              RideMessageKind.CONFIRM -> {
                RideMeshCodec.decodeConfirm(raw)?.let { confirm ->
                  message.senderPeerID?.let { sender ->
                    listener?.onConfirm(confirm, sender)
                  }
                }
              }
              else -> {}
            }
          }

          override fun didUpdatePeerList(peers: List<String>) {}
          override fun didReceiveChannelLeave(channel: String, fromPeer: String) {}
          override fun didReceiveDeliveryAck(ack: DeliveryAck) {}
          override fun didReceiveReadReceipt(receipt: ReadReceipt) {}
          override fun decryptChannelMessage(encryptedContent: ByteArray, channel: String): String? =
            encryptedContent.toString(Charsets.UTF_8)
          override fun getNickname(): String? = null
          override fun isFavorite(peerID: String): Boolean = false
        }
        startServices()
      }
      restartBleScan()
    }
    return START_STICKY
  }

  fun setListener(l: RideMeshListener?) { listener = l }

  val peerId: String
    get() = mesh?.myPeerID ?: ""

  fun startMesh(channel: String) {
    this.channel = channel
    val missing = BlePermissionHelper.missingPermissions(this)
    if (missing.isNotEmpty()) {
      sendBroadcast(Intent(ACTION_PERMS_NEEDED).apply {
        putExtra(EXTRA_MISSING_PERMS, missing.toTypedArray())
      })
      return
    }
    mesh?.startServices()
    restartBleScan()
  }

  fun stopMesh() {
    mesh?.stopServices()
    mesh = null
  }

  fun sendChannelMessage(text: String) {
    val ch = channel
    if (ch != null) mesh?.sendMessage(text, channel = ch)
  }

  fun sendPrivateMessage(peerId: String, text: String) {
    mesh?.sendPrivateMessage(text, peerId, peerId)
  }

  override fun onDestroy() {
    mesh?.stopServices()
    mesh = null
    super.onDestroy()
  }

  /**
   * Memastikan pemindaian BLE dimulai setelah client manager aktif
   * dengan memanggil restartScanning() melalui refleksi.
   */
  private fun restartBleScan() {
    try {
      val meshObj = mesh ?: return
      val cmField = BluetoothMeshService::class.java.getDeclaredField("connectionManager")
      cmField.isAccessible = true
      val connManager = cmField.get(meshObj)
      val clientField = connManager.javaClass.getDeclaredField("clientManager")
      clientField.isAccessible = true
      val clientManager = clientField.get(connManager)
      val restartMethod = clientManager.javaClass.getDeclaredMethod("restartScanning")
      restartMethod.isAccessible = true
      restartMethod.invoke(clientManager)
      Log.d("MeshService", "BLE scanning restarted via reflection")
    } catch (e: Exception) {
      Log.e("MeshService", "Gagal memulai ulang pemindaian BLE", e)
    }
  }
}
