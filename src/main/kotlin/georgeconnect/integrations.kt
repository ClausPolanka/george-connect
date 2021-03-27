package georgeconnect

import com.beust.klaxon.Klaxon
import java.time.LocalDate

fun showInteractions(path: String) {
    val peers = sortedPeersFrom(path)
    showLastInteractionsWith(peers, display = ::println)
}

private fun sortedPeersFrom(path: String): List<Peer> {
    val peers = peersFrom(path)
    return peers.sortedBy { it.lastInteractionF2FInDays(LocalDate::now)  }
}

private fun peersFrom(path: String): MutableSet<Peer> {
    val jsons = jsonsFrom(path)
    return peersFrom(jsons, Klaxon()::parse)
}

private fun showLastInteractionsWith(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach {
        val days = it.lastInteractionF2FInDays(LocalDate::now)
        val output = outputFor(days)
        display("Last F2F interaction with ${it.firstName} ${it.lastName} $output")
    }
}

fun updatePeer(args: Array<String>) {
    val (path, peer) = parse(args, ::parseFourArgs, ::parseThreeArgs, ::parseTwoArgs, ::findPeerBy)
    updateJsonFor(peer, path)
}

fun findPeerBy(firstName: String, path: String): Peer? {
    val peers = peersFrom(path)
    peers.throwIfDuplicatesExistFor(firstName)
    return peers.find { it.firstName.equals(firstName, ignoreCase = true) }
}
