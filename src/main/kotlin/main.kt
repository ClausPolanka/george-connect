import com.beust.klaxon.Klaxon
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun main(args: Array<String>) {
    if (args.size == 2) {
        val p = Peer(args[0], args[1], LocalDate.now().toString())
        val json = Klaxon().toJsonString(p)
        File("./data/${p.lastName}_${p.firstName}.json").writeText(json)
    }
    if (args.size == 1) {
        val jsons = jsonsFrom(path = "./data")
        val peers = peersFrom(jsons)
        val p = peers.find { it.firstName == args[0] }
        p?.lastInteractionF2F = LocalDate.now().toString()
        val json = Klaxon().toJsonString(p)
        File("./data/${p?.lastName}_${p?.firstName}.json").writeText(json)
    }
    val jsons = jsonsFrom(path = "./data")
    val peers = peersFrom(jsons)
    val sortedPeers = peers.sortedBy { toDays(it.lastInteractionF2F) }
    sortedPeers.forEach {
        val days = toDays(it.lastInteractionF2F)
        val output = when {
            days == 0L -> "today"
            days > 1 -> "$days days ago"
            else -> "$days day ago"
        }
        println("Last F2F interaction with " + it.firstName + " " + it.lastName + " " + output)
    }
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

private fun toDays(lastInteraction: String): Long {
    val ld = LocalDate.parse(lastInteraction)
    return ChronoUnit.DAYS.between(ld, LocalDate.now())
}

data class Peer(val firstName: String, val lastName: String, var lastInteractionF2F: String)
