import com.beust.klaxon.Klaxon
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun main(args: Array<String>) {
    errorHandled {
        inCase(args.isEmpty(),
            onEmpty = {
                val peers = sortedPeers()
                show(peers)
            },
            onNonEmpty = {
                updatePeer(args)
                val peers = sortedPeers()
                show(peers)
            }
        )
    }
}

private fun errorHandled(fn: () -> Unit) {
    try {
        fn()
    } catch (e: PeerNotFoundException) {
        println("Sorry, couldn't find '${e.firstName}'")
    } catch (e: MultipleEntriesFoundException) {
        println("Multiple entries found for '${e.firstName}'. Please also provide last name.")
    }
}

private fun inCase(argsAreEmpty: Boolean, onEmpty: () -> Unit, onNonEmpty: () -> Unit) {
    when (argsAreEmpty) {
        true -> onEmpty()
        else -> onNonEmpty()
    }
}

private fun updatePeer(args: Array<String>) {
    val (firstName, lastName) = parse(args, ::findPeerBy)
    updatePeer(firstName, lastName)
}

private fun parse(args: Array<String>, findBy: (firstName: String) -> Peer?) = when (args.size) {
    2 -> {
        val firstName = args[0]
        val lastName = args[1]
        Pair(firstName, lastName)
    }
    else -> {
        val firstName = args[0]
        when (val p = findBy(firstName)) {
            null -> throw PeerNotFoundException(firstName)
            else -> Pair(p.firstName, p.lastName)
        }
    }
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

private fun jsonsFrom(path: String): List<String> {
    return File(path).walk()
        .filter { it.extension == "json" }
        .map { it.readText(Charsets.UTF_8) }
        .toList()
}

private fun peersFrom(jsons: List<String>, jsonToPeer: (String) -> Peer?): MutableSet<Peer> {
    return jsons.mapNotNull { jsonToPeer(it) }.toMutableSet()
}

private fun MutableSet<Peer>.throwIfDuplicatesExistFor(firstName: String) {
    val result = this.filter { it.firstName == firstName }
    if (result.size > 1) {
        throw MultipleEntriesFoundException(firstName)
    }
}

private fun updatePeer(firstName: String, lastName: String) {
    val p = Peer(firstName, lastName, LocalDate.now().toString())
    val json = Klaxon().toJsonString(p)
    File("./data/${lastName}_$firstName.json").writeText(json)
}

private fun sortedPeers(): List<Peer> {
    val peers = peers()
    return peers.sortedBy { toDays(it.lastInteractionF2F) }
}

private fun toDays(lastInteraction: String): Long {
    val ld = LocalDate.parse(lastInteraction)
    return ChronoUnit.DAYS.between(ld, LocalDate.now())
}

private fun show(peers: List<Peer>) {
    peers.forEach {
        val days = toDays(it.lastInteractionF2F)
        val output = when {
            days == 0L -> "today"
            days > 1 -> "$days days ago"
            else -> "$days day ago"
        }
        println("Last F2F interaction with " + it.firstName + " " + it.lastName + " " + output)
    }
}

data class Peer(
    val firstName: String,
    val lastName: String,
    var lastInteractionF2F: String
)

class MultipleEntriesFoundException(val firstName: String) : RuntimeException()

class PeerNotFoundException(val firstName: String) : RuntimeException()
