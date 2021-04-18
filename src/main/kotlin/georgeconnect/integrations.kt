package georgeconnect

import com.beust.klaxon.Klaxon
import java.lang.String.format
import java.time.LocalDate

fun showInteractions(path: String) {
    val peers = sortedPeersFrom(path)
    showLastInteractionsWith(peers, display = ::println)
}

private fun sortedPeersFrom(path: String): List<Peer> {
    val peers = peersFrom(path)
    return peers.sortedBy { it.lastInteractionF2FInDays(LocalDate::now) }
}

private fun peersFrom(path: String): MutableSet<Peer> {
    val jsons = jsonsFrom(path)
    return peersFrom(jsons, Klaxon()::parse)
}

private fun showLastInteractionsWith(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach {
        val days = it.lastInteractionF2FInDays(LocalDate::now)
        val output = outputFor(days)
        display(format(lastF2FInteractionFormat, it.firstName, it.lastName, output))
    }
}

fun findPeerBy(firstName: String, path: String): FindResult {
    val peers = peersFrom(path)
    val duplicates = peers.filter { it.firstName.equals(firstName, ignoreCase = true) }
    return findDuplicates(duplicates, firstName)
}
