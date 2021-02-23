import com.beust.klaxon.Klaxon
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        val p = Peer(args[0], LocalDate.now().toString())
        val json = Klaxon().toJsonString(p)
        File("./data/${p.name}.json").writeText(json)
    }
    val jsons = jsonsFrom(path = "./data")
    val peers = peersFrom(jsons)
    val sortedPeers = peers.sortedBy { toDays(it.lastInteraction) }
    sortedPeers.forEach {
        val days = toDays(it.lastInteraction)
        val output = when {
            days == 0L -> "today"
            days > 1 -> "$days days ago"
            else -> "$days day ago"
        }
        println("Last interaction with " + it.name + " " + output)
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

data class Peer(val name: String, var lastInteraction: String)
