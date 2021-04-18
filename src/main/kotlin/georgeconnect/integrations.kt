package georgeconnect

import java.time.LocalDate

fun showInteractions(fileAdapter: FileAdapter, display: (s: String) -> Unit) {
    val peers = sortedPeersFromFileSystem(fileAdapter)
    showLastInteractionsWith(peers, display)
}

private fun sortedPeersFromFileSystem(FileAdapter: FileAdapter): List<Peer> {
    val peers = loadPeersFromFileSystem(FileAdapter)
    return peers.sortedBy { it.lastInteractionF2FInDays(LocalDate::now) }
}

private fun loadPeersFromFileSystem(fileAdapter: FileAdapter): MutableSet<Peer> {
    val fileData = fileAdapter.loadFileData(fileAdapter.dataPath, fileAdapter.extension)
    return peersFrom(fileData, fileAdapter.deserializePeer)
}

private fun showLastInteractionsWith(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach { showLastF2FInteraction(it, ::outputFor, display) }
}

fun findPeerBy(firstName: String, FileAdapter: FileAdapter): FindResult {
    val peers = loadPeersFromFileSystem(FileAdapter)
    val potentialDuplicates = peers.filter { it.firstName.equals(firstName, ignoreCase = true) }
    return findDuplicates(potentialDuplicates, firstName)
}
