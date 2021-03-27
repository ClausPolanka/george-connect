package georgeconnect

import com.beust.klaxon.Klaxon
import java.io.File
import java.lang.String.format
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

fun errorHandled(display: (msg: String) -> Unit, georgeConnect: () -> Unit) {
    try {
        georgeConnect()
    } catch (e: PeerNotFoundException) {
        display(format(peerNotFoundFormat, e.firstName))
    } catch (e: MultipleEntriesFoundException) {
        display(format(multipleEntriesFormat, e.firstName))
    } catch (e: WrongNumberOfArgsException) {
        display(usage)
    } catch (e: PeerLastInteractionDateHasWrongFormat) {
        display(format(dateHasWrongFormat, e.peer.firstName, e.peer.lastName, e.peer.lastInteractionF2F))
    }
}

fun inCase(argsOnlyContainPath: Boolean, onShowInteractions: () -> Unit, onUpdatePeer: () -> Unit) {
    when (argsOnlyContainPath) {
        true -> onShowInteractions()
        else -> onUpdatePeer()
    }
}

fun parse(
    args: Array<String>,
    onFourArgs: (args: Array<String>) -> Pair<String, Peer>,
    onThreeArgs: (args: Array<String>) -> Pair<String, Peer>,
    onTwoArgs: (args: Array<String>, findBy: (firstName: String, path: String) -> Peer?) -> Pair<String, Peer>,
    findBy: (firstName: String, path: String) -> Peer?
): Pair<String, Peer> {
    return when (args.size) {
        4 -> onFourArgs(args)
        3 -> onThreeArgs(args)
        2 -> onTwoArgs(args, findBy)
        else -> throw WrongNumberOfArgsException()
    }
}

fun parseFourArgs(args: Array<String>): Pair<String, Peer> {
    val path = args[0]
    val firstName = args[1]
    val lastName = args[2]
    val date = args[3]
    return Pair(path, Peer(firstName, lastName, date))
}

fun parseThreeArgs(args: Array<String>): Pair<String, Peer> {
    val path = args[0]
    val firstName = args[1]
    val lastName = args[2]
    return Pair(path, Peer(firstName, lastName))
}

fun parseTwoArgs(args: Array<String>, findBy: (firstName: String, path: String) -> Peer?): Pair<String, Peer> {
    val path = args[0]
    val firstName = args[1]
    return when (val p = findBy(firstName, path)) {
        null -> throw PeerNotFoundException(firstName)
        else -> Pair(
            path,
            Peer(p.firstName, p.lastName)
        )
    }
}

class PeerNotFoundException(val firstName: String) : RuntimeException()

class WrongNumberOfArgsException : RuntimeException()

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