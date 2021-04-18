package georgeconnect

import java.time.LocalDate

fun showInteractions(fileAdapter: FileAdapter, display: (s: String) -> Unit) {
    val peers = sortedPeersFromFileSystem(fileAdapter, display)
    showLastInteractionsWith(peers, display)
}

private fun sortedPeersFromFileSystem(FileAdapter: FileAdapter, display: (s: String) -> Unit): List<Peer> {
    val peers = loadPeersFromFileSystem(FileAdapter, display)
    return peers.sortedBy { it.lastInteractionF2FInDays(LocalDate::now) }
}

private fun loadPeersFromFileSystem(fileAdapter: FileAdapter, display: (s: String) -> Unit): MutableSet<Peer> {
    val fileData = fileAdapter.loadFileData()
    return peersFrom(fileData, fileAdapter.deserializePeer, display)
}

private fun showLastInteractionsWith(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach { showLastF2FInteraction(it, ::outputFor, display) }
}

fun findPeerBy(firstName: String, FileAdapter: FileAdapter, display: (s: String) -> Unit): FindResult {
    val peers = loadPeersFromFileSystem(FileAdapter, display)
    val potentialDuplicates = peers.filter { it.firstName.equals(firstName, ignoreCase = true) }
    return findDuplicates(potentialDuplicates, firstName)
}
