package georgeconnect

import java.time.LocalDate


fun showInteractions(fa: FileAdapter, display: (s: String) -> Unit) {
    val peers = sortedPeersFromFileSystem(fa, display)
    showLastInteractionsWith(peers, display)
}

private fun sortedPeersFromFileSystem(fa: FileAdapter, display: (s: String) -> Unit): List<Peer> {
    val peers = loadPeersFromFileSystem(fa, display)
    return peers.sortedBy { it.lastInteractionF2FInDays(LocalDate::now) }
}

private fun loadPeersFromFileSystem(fa: FileAdapter, display: (s: String) -> Unit): MutableSet<Peer> {
    val fileData = fa.loadFileData()
    return peersFrom(fileData, fa.deserializePeer, display)
}

private fun showLastInteractionsWith(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach { showLastF2FInteraction(it, ::outputFor, display) }
}

fun findPeerBy(firstName: String, fa: FileAdapter, display: (s: String) -> Unit): FindResult {
    val peers = loadPeersFromFileSystem(fa, display)
    return findDuplicates(peers, firstName)
}

fun createOrUpdateAndShowPeers(peer: Peer, display: (msg: String) -> Unit, fa: FileAdapter) {
    validateInput(peer, onValid = ::createOrUpdatePeer, onInvalid = display, fa)
}

