package georgeconnect

import georgeconnect.FindStatus.*
import georgeconnect.GeorgeConnectCommands.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

fun filesFrom(path: String, extension: String): List<String> {
    return File(path).walk()
        .filter { it.extension == extension }
        .map { it.readText(Charsets.UTF_8) }
        .toList()
}

fun peersFrom(serializedPeers: List<String>, deserializePeer: (String) -> Peer?): MutableSet<Peer> {
    return serializedPeers.mapNotNull { deserializePeer(it) }.toMutableSet()
}

fun createOrUpdatePeerOnFileSystem(p: Peer, fileAdapter: FileAdapter): CreateOrUpdateStatus {
    val serializedPeer = fileAdapter.serializePeer(p)
    return try {
        val f = File("${fileAdapter.dataPath}/${p.lastName}_${p.firstName}.${fileAdapter.extension}")
        f.writeText(serializedPeer)
        CreateOrUpdateStatus.SUCCESS
    } catch (e: Exception) {
        CreateOrUpdateStatus.ERROR
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

fun createOrUpdatePeer(
    createOrUpdate: (p: Peer, fileAdapter: FileAdapter) -> CreateOrUpdateStatus,
    peer: Peer,
    onSuccess: (fileAdapter: FileAdapter, display: (msg: String) -> Unit) -> Unit,
    onError: (msg: String) -> Unit,
    fileAdapter: FileAdapter
) {
    when (createOrUpdate(peer, fileAdapter)) {
        CreateOrUpdateStatus.SUCCESS -> onSuccess(fileAdapter, onError)
        CreateOrUpdateStatus.ERROR -> onError("While creating or updating, something went wrong")
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

fun findDuplicates(peers: List<Peer>, firstName: String): FindResult {
    return when {
        peers.size > 1 -> FindResult(Peer(firstName, peers[0].lastName, peers[0].lastInteractionF2F), DUPLICATE_PEER_BY_FIRST_NAME)
        peers.size == 1 -> FindResult(Peer(firstName, peers[0].lastName, peers[0].lastInteractionF2F), SUCCESS)
        else -> FindResult(Peer(firstName, "unknown"), PEER_UNKNOWN)
    }
}