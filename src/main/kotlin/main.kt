import com.beust.klaxon.Klaxon
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun main(args: Array<String>) {
    if (args.size == 2) {
        val firstName = args[0]
        val lastName = args[1]
        update(firstName, lastName)
    }
    if (args.size == 1) {
        val firstName = args[0]
        val p = findFirstBy(firstName)
        if (p == null) {
            println("Sorry, couldn't find '$firstName'")
            return
        }
        update(p.firstName, p.lastName)
    }
    val peers = sortedPeers()
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

private fun update(firstName: String, lastName: String) {
    val p = Peer(firstName, lastName, LocalDate.now().toString())
    val json = Klaxon().toJsonString(p)
    File("./data/${lastName}_$firstName.json").writeText(json)
}

private fun findFirstBy(firstName: String): Peer? {
    val peers = peers()
    return peers.find { it.firstName == firstName }
}

private fun peers(): MutableSet<Peer> {
    val jsons = jsonsFrom(path = "./data")
    return peersFrom(jsons)
}

private fun jsonsFrom(path: String): List<String> {
    return File(path).walk()
        .filter { it.extension == "json" }
        .map { it.readText(Charsets.UTF_8) }
        .toList()
}

fun peersFrom(jsons: List<String>): MutableSet<Peer> {
    return jsons.mapNotNull { Klaxon().parse<Peer>(it) }.toMutableSet()
}

private fun sortedPeers(): List<Peer> {
    val peers = peers()
    return peers.sortedBy { toDays(it.lastInteractionF2F) }
}

private fun toDays(lastInteraction: String): Long {
    val ld = LocalDate.parse(lastInteraction)
    return ChronoUnit.DAYS.between(ld, LocalDate.now())
}

data class Peer(
    val firstName: String,
    val lastName: String,
    var lastInteractionF2F: String
)
