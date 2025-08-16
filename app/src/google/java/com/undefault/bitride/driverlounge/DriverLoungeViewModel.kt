package com.undefault.bitride.driverlounge

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import app.organicmaps.bitride.mesh.DriverReply
import app.organicmaps.bitride.mesh.MeshService
import app.organicmaps.bitride.mesh.RideConfirm
import app.organicmaps.bitride.mesh.RideMeshListener
import app.organicmaps.bitride.mesh.RideRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DriverLoungeViewModel(app: Application) : AndroidViewModel(app), RideMeshListener {
    private val _requests = MutableStateFlow<List<RideRequest>>(emptyList())
    val requests = _requests.asStateFlow()

    private var meshService: MeshService? = null
    private var bound = false

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MeshService.MeshBinder
            meshService = binder.service
            meshService?.setListener(this@DriverLoungeViewModel)
            meshService?.startMesh("#bitride")
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            meshService = null
        }
    }

    init {
        val context = getApplication<Application>()
        MeshService.start(context)
        context.bindService(
            Intent(context, MeshService::class.java),
            conn,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onCleared() {
        if (bound) {
            meshService?.setListener(null)
            getApplication<Application>().unbindService(conn)
        }
        super.onCleared()
    }

    override fun onRideRequestFromCustomer(req: RideRequest, senderPeerId: String) {
        _requests.value = _requests.value + req
    }

    override fun onDriverReply(resp: DriverReply, senderPeerId: String) { /* no-op */ }
    override fun onConfirm(confirm: RideConfirm, senderPeerId: String) { /* no-op */ }
    override fun onChannelMessage(text: String, senderPeerId: String) { /* no-op */ }
    override fun onPeerListUpdated(peers: List<String>) { /* no-op */ }
    override fun onHandshakeComplete(peerId: String) { /* no-op */ }
}
