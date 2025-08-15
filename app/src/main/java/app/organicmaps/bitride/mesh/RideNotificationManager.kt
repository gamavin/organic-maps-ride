package app.organicmaps.bitride.mesh

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Menampilkan notifikasi sederhana untuk pesan mesh yang diterima.
 */
class RideNotificationManager(private val context: Context) {
  companion object {
    private const val CHANNEL_ID = "ride_mesh_messages"
  }

  private val nm = NotificationManagerCompat.from(context)

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Ride Mesh Messages",
        NotificationManager.IMPORTANCE_DEFAULT
      )
      val sys = context.getSystemService(NotificationManager::class.java)
      sys?.createNotificationChannel(channel)
    }
  }

  fun show(title: String, text: String) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(title)
      .setContentText(text)
      .setAutoCancel(true)
      .build()
    nm.notify((System.currentTimeMillis() and 0xFFFFFF).toInt(), notification)
  }
}
