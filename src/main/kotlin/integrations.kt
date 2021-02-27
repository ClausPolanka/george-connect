import com.beust.klaxon.Klaxon
import java.time.LocalDate

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

private fun peers(): MutableSet<Peer> {
    val jsons = jsonsFrom(path = "./data")
    return peersFrom(jsons, Klaxon()::parse)
}

fun sortedPeers(): List<Peer> {
    val peers = peers()
    return peers.sortedBy { toDays(it.lastInteractionF2F) }
}

fun show(peers: List<Peer>, display: (s: String) -> Unit) {
    peers.forEach {
        val days = toDays(it.lastInteractionF2F)
        val output = outputFor(days)
        display("Last F2F interaction with ${it.firstName} ${it.lastName} $output")
    }
}