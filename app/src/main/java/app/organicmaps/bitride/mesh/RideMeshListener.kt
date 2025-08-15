package app.organicmaps.bitride.mesh

interface RideMeshListener {
  fun onRideRequestFromCustomer(req: RideRequest, senderPeerId: String)
  fun onDriverReply(resp: DriverReply, senderPeerId: String)
  fun onConfirm(confirm: RideConfirm, senderPeerId: String)
  fun onChannelMessage(text: String, senderPeerId: String)
}
