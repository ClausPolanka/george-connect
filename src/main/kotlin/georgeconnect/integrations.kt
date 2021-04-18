package georgeconnect

import java.lang.String.format
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
    peers.forEach {
        val days = it.lastInteractionF2FInDays(LocalDate::now)
        val output = outputFor(days)
        display(format(lastF2FInteractionFormat, it.firstName, it.lastName, output))
    }
}

fun findPeerBy(firstName: String, FileAdapter: FileAdapter): FindResult {
    val peers = loadPeersFromFileSystem(FileAdapter)
    val potentialDuplicates = peers.filter { it.firstName.equals(firstName, ignoreCase = true) }
    return findDuplicates(potentialDuplicates, firstName)
}
