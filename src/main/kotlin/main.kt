import com.beust.klaxon.Klaxon
import java.io.File
import java.time.LocalDate

fun main(args: Array<String>) {
    when {
        args.isNotEmpty() -> {
            val p = Peer(args[0], LocalDate.now().toString())
            val json = Klaxon().toJsonString(p)
            File("./data/${p.name}.json").writeText(json)
        }
    }
    val jsons = jsonsFrom(path = "./data")
    val peers = peersFrom(jsons)
    peers.forEach { println(it) }
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

data class Peer(val name: String, var lastInteraction: String)
