package georgeconnect

import com.beust.klaxon.Klaxon
import georgeconnect.FindStatus.*
import georgeconnect.GeorgeConnectCommands.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

fun jsonsFrom(path: String): List<String> {
    return File(path).walk()
        .filter { it.extension == "json" }
        .map { it.readText(Charsets.UTF_8) }
        .toList()
}

fun peersFrom(jsons: List<String>, jsonToPeer: (String) -> Peer?): MutableSet<Peer> {
    return jsons.mapNotNull { jsonToPeer(it) }.toMutableSet()
}

fun createOrUpdateJsonFor(p: Peer, path: String): CreateOrUpdateStatus {
    val json = Klaxon().toJsonString(p)
    return try {
        val f = File("$path/${p.lastName}_${p.firstName}.json")
        f.writeText(json)
        CreateOrUpdateStatus.SUCCESS
    } catch (e: Exception) {
        CreateOrUpdateStatus.FILE_ERROR
    }
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

fun createOrUpdate(
    createOrUpdate: (p: Peer, path: String) -> CreateOrUpdateStatus,
    dataPath: String,
    peer: Peer,
    onSuccess: (s: String) -> Unit,
    onFileError: (msg: String) -> Unit
) {
    when (createOrUpdate(peer, dataPath)) {
        CreateOrUpdateStatus.SUCCESS -> onSuccess(dataPath)
        CreateOrUpdateStatus.FILE_ERROR -> onFileError("While creating or updating, something went wrong")
    }
}

fun toCommand(args: Array<String>): GeorgeConnectCommands {
    return when (args.size) {
        1 -> SHOW_INTERACTIONS
        2 -> UPDATE_BY_FIRST_NAME
        3 -> CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME
        4 -> CREATE_OR_UPDATE_WITH_CUSTOM_DATE
        else -> WRONG_NR_OF_ARGS
    }
}

fun toFindResult(peers: List<Peer>): FindResult {
    return when {
        peers.size > 1 -> FindResult(peers[0], DUPLICATE_PEER_BY_FIRST_NAME)
        peers.size == 1 -> FindResult(peers[0], SUCCESS)
        else -> FindResult(Peer("unknown", "unknown"), PEER_UNKNOWN)
    }
}