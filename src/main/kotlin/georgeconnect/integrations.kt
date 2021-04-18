package georgeconnect

import com.beust.klaxon.Klaxon
import java.lang.String.format
import java.time.LocalDate

fun showInteractions(fileDeserializer: FileDeserializer, display: (s: String) -> Unit) {
    val peers = sortedPeersFrom(fileDeserializer)
    showLastInteractionsWith(peers, display)
}

private fun sortedPeersFrom(fileDeserializer: FileDeserializer): List<Peer> {
    val peers = peersFrom(fileDeserializer)
    return peers.sortedBy { it.lastInteractionF2FInDays(LocalDate::now) }
}

private fun peersFrom(fileDeserializer: FileDeserializer): MutableSet<Peer> {
    val fileData = fileDeserializer.loadFileData(fileDeserializer.dataPath)
    return peersFrom(fileData, fileDeserializer.deserializePeer)
}

private fun showLastInteractionsWith(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach {
        val days = it.lastInteractionF2FInDays(LocalDate::now)
        val output = outputFor(days)
        display(format(lastF2FInteractionFormat, it.firstName, it.lastName, output))
    }
}

fun findPeerBy(firstName: String, path: String): FindResult {
    val peers = peersFrom(FileDeserializer(path, ::jsonsFrom, Klaxon()::parse))
    val potentialDuplicates = peers.filter { it.firstName.equals(firstName, ignoreCase = true) }
    return findDuplicates(potentialDuplicates, firstName)
}
