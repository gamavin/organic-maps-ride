package app.organicmaps.bitride.mesh

import android.content.Context

/**
 * Sementara cukup wrapper start/stop service. Binding dua arah akan ditambah di Plan 2.
 */
object MeshManager {
  fun start(context: Context) = MeshService.start(context.applicationContext)
  fun stop(context: Context) = MeshService.stop(context.applicationContext)
}
