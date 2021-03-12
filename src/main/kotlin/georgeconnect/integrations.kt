package georgeconnect

import com.beust.klaxon.Klaxon
import java.time.LocalDate

fun sortedPeers(): List<Peer> {
    val peers = peers()
    return peers.sortedBy { it.lastInteractionF2FInDays(LocalDate::now)  }
}

private fun peers(): MutableSet<Peer> {
    val jsons = jsonsFrom(path = "./data")
    return peersFrom(jsons, Klaxon()::parse)
}

fun showLastInteractionsWith(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach {
        val days = it.lastInteractionF2FInDays(LocalDate::now)
        val output = outputFor(days)
        display("Last F2F interaction with ${it.firstName} ${it.lastName} $output")
    }
}

fun updatePeer(args: Array<String>) {
    val (firstName, lastName) = parse(args, ::findPeerBy)
    val p = Peer(firstName, lastName, LocalDate.now().toString())
    updateJsonFor(p, path = "./data")
}

private fun findPeerBy(firstName: String): Peer? {
    val peers = peers()
    peers.throwIfDuplicatesExistFor(firstName)
    return peers.find { it.firstName == firstName }
}
