package georgeconnect

import com.beust.klaxon.Klaxon
import java.lang.String.format
import java.time.LocalDate

fun showInteractions(path: String, loadFileData: (path: String) -> List<String>, deserializePeer: (String) -> Peer?) {
    val peers = sortedPeersFrom(path, loadFileData, deserializePeer)
    showLastInteractionsWith(peers, display = ::println)
}

private fun sortedPeersFrom(path: String, loadFileData: (path: String) -> List<String>, deserializePeer: (String) -> Peer?): List<Peer> {
    val peers = peersFrom(path, loadFileData, deserializePeer)
    return peers.sortedBy { it.lastInteractionF2FInDays(LocalDate::now) }
}

private fun peersFrom(path: String, loadFileData: (path: String) -> List<String>, deserializePeer: (String) -> Peer?): MutableSet<Peer> {
    val fileData = loadFileData(path)
    return peersFrom(fileData, deserializePeer)
}

private fun showLastInteractionsWith(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach {
        val days = it.lastInteractionF2FInDays(LocalDate::now)
        val output = outputFor(days)
        display(format(lastF2FInteractionFormat, it.firstName, it.lastName, output))
    }
}

fun findPeerBy(firstName: String, path: String): FindResult {
    val peers = peersFrom(path, ::jsonsFrom, Klaxon()::parse)
    val duplicates = peers.filter { it.firstName.equals(firstName, ignoreCase = true) }
    return findDuplicates(duplicates, firstName)
}
