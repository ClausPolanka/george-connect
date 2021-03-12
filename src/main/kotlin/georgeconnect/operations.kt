import com.beust.klaxon.Klaxon
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

fun errorHandled(display: (msg: String) -> Unit, georgeConnect: () -> Unit) {
    try {
        georgeConnect()
    } catch (e: PeerNotFoundException) {
        display("Sorry, couldn't find '${e.firstName}'")
    } catch (e: MultipleEntriesFoundException) {
        display("Multiple entries found for '${e.firstName}'. Please also provide last name.")
    } catch (e: TooManyArgsException) {
        display(
            """usage
            |george-connect                             list all peer face-to-face interactions
            |george-connect <first_name>                log new peer face-to-face interaction for existing peer
            |george-connect <first_name> <last_name>    log new peer face-to-face interaction for existing or new peer
        """.trimMargin()
        )
    } catch (e: PeerLastInteractionDateHasWrongFormat) {
        display("Unfortunately the last interaction date for '${e.peer.firstName} ${e.peer.lastName}'" +
                " has an unknown format: '${e.peer.lastInteractionF2F}'")
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

fun Peer.lastInteractionF2FInDays(now: () -> LocalDate): Long {
    val localDate = try {
        LocalDate.parse(this.lastInteractionF2F)
    } catch (e: DateTimeParseException) {
        throw PeerLastInteractionDateHasWrongFormat(this)
    }
    return ChronoUnit.DAYS.between(localDate, now())
}

class PeerLastInteractionDateHasWrongFormat(val peer: Peer) : RuntimeException()

fun outputFor(days: Long): String {
    return when {
        days == 0L -> "today"
        days > 1 -> "$days days ago"
        else -> "$days day ago"
    }
}