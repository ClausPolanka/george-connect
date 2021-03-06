import com.beust.klaxon.Klaxon
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun errorHandled(display: (msg: String) -> Unit, fn: () -> Unit) {
    try {
        fn()
    } catch (e: PeerNotFoundException) {
        display("Sorry, couldn't find '${e.firstName}'")
    } catch (e: MultipleEntriesFoundException) {
        display("Multiple entries found for '${e.firstName}'. Please also provide last name.")
    } catch (e: TooManyArgsException) {
        display("""usage
            |george-connect                             list all peer face-to-face interactions
            |george-connect <first_name>                log new peer face-to-face interaction for existing peer
            |george-connect <first_name> <last_name>    log new peer face-to-face interaction for existing or new peer
        """.trimMargin())
    }
}

fun inCase(argsAreEmpty: Boolean, onEmpty: () -> Unit, onNonEmpty: () -> Unit) {
    when (argsAreEmpty) {
        true -> onEmpty()
        else -> onNonEmpty()
    }
}

fun parse(args: Array<String>, findBy: (firstName: String) -> Peer?) = when (args.size) {
    2 -> {
        val firstName = args[0]
        val lastName = args[1]
        Pair(firstName, lastName)
    }
    1 -> {
        val firstName = args[0]
        when (val p = findBy(firstName)) {
            null -> throw PeerNotFoundException(firstName)
            else -> Pair(p.firstName, p.lastName)
        }
    }
    else -> throw TooManyArgsException()
}

class PeerNotFoundException(val firstName: String) : RuntimeException()

class TooManyArgsException : RuntimeException()

fun jsonsFrom(path: String): List<String> {
    return File(path).walk()
        .filter { it.extension == "json" }
        .map { it.readText(Charsets.UTF_8) }
        .toList()
}

fun peersFrom(jsons: List<String>, jsonToPeer: (String) -> Peer?): MutableSet<Peer> {
    return jsons.mapNotNull { jsonToPeer(it) }.toMutableSet()
}

fun MutableSet<Peer>.throwIfDuplicatesExistFor(firstName: String) {
    val result = this.filter { it.firstName == firstName }
    if (result.size > 1) {
        throw MultipleEntriesFoundException(firstName)
    }
}

class MultipleEntriesFoundException(val firstName: String) : RuntimeException()

fun updateJsonFor(p: Peer, path: String) {
    val json = Klaxon().toJsonString(p)
    File("$path/${p.lastName}_${p.firstName}.json").writeText(json)
}

fun toDays(lastInteraction: String): Long {
    val ld = LocalDate.parse(lastInteraction)
    return ChronoUnit.DAYS.between(ld, LocalDate.now())
}

fun outputFor(days: Long): String {
    return when {
        days == 0L -> "today"
        days > 1 -> "$days days ago"
        else -> "$days day ago"
    }
}