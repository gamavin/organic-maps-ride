package app.organicmaps.bitride.mesh

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service pembungkus mesh.
 * LANGKAH INI HANYA STUB: belum memanggil BitChat.
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

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createChannel()
    startForeground(NOTIF_ID, buildNotif())
    // TODO(Plan2): init BitChat advertiser+scanner & join #bitride di sini
  }

  override fun onDestroy() {
    super.onDestroy()
    // TODO(Plan2): stop BitChat components di sini
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

  // ------- API stub yang nanti dipanggil UI/manager -------
  fun startMesh(channel: String = "#bitride") {
    // TODO(Plan2): inisialisasi BitChat join channel
  }
  fun stopMesh() {
    // TODO(Plan2): stop
    stopSelf()
  }
  fun sendChannelMessage(content: String) {
    // TODO(Plan2)
  }
  fun sendPrivateMessage(peerId: String, content: String) {
    // TODO(Plan2)
  }
  fun setListener(listener: RideMeshListener?) {
    // TODO(Plan2)
  }
}
