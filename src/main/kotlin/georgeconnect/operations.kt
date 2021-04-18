package georgeconnect

import com.beust.klaxon.Klaxon
import georgeconnect.FindStatus.*
import georgeconnect.GeorgeConnectCommands.*
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun parse(
    args: Array<String>,
    argsToCommands: (args: Array<String>) -> GeorgeConnectCommands,
    createFileAdapter: (args: Array<String>) -> FileAdapter
): GeorgeConnectCmd {
    return when (argsToCommands(args)) {
        WRONG_NR_OF_ARGS -> ShowUsageCmd()
        SHOW_INTERACTIONS -> ShowInteractionsCmd(
            createFileAdapter(args),
            display = ::println
        )
        UPDATE_BY_FIRST_NAME -> UpdatePeerByFirstNameCmd(
            firstName = args[1],
            createFileAdapter(args),
            display = ::println
        )
        CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME -> CreateOrUpdatePeerByFirstNameAndLastNameCmd(
            firstName = args[1],
            lastName = args[2],
            createFileAdapter(args),
            display = ::println
        )
        CREATE_OR_UPDATE_WITH_CUSTOM_DATE -> CreateOrUpdateWithCustomDateCmd(
            firstName = args[1],
            lastName = args[2],
            date = args[3],
            createFileAdapter(args),
            display = ::println
        )
    }
}

fun createJsonKlaxonFileAdapter(args: Array<String>) = FileAdapter(
    dataPath = args[0],
    loadFileData = ::filesFrom,
    deserializePeer = Klaxon()::parse,
    serializePeer = Klaxon()::toJsonString,
    extension = "json"
)

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

fun Peer.lastInteractionF2FInDays(now: () -> LocalDate): Long? {
    return try {
        val localDate = LocalDate.parse(this.lastInteractionF2F)
        ChronoUnit.DAYS.between(localDate, now())
    } catch (e: Exception) {
        null
    }
}

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

fun argsToCommands(args: Array<String>): GeorgeConnectCommands {
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
        peers.size > 1 -> FindResult(
            Peer(firstName, peers[0].lastName, peers[0].lastInteractionF2F),
            DUPLICATE_PEER_BY_FIRST_NAME
        )
        peers.size == 1 -> FindResult(Peer(firstName, peers[0].lastName, peers[0].lastInteractionF2F), SUCCESS)
        else -> FindResult(Peer(firstName, "unknown"), PEER_UNKNOWN)
    }
}

fun showLastF2FInteraction(peer: Peer, outputForDays: (days: Long) -> String, display: (s: String) -> Unit) {
    when (val days = peer.lastInteractionF2FInDays(LocalDate::now)) {
        null -> display("Please check: '$peer' last interaction date")
        else -> {
            val output = outputForDays(days)
            display(java.lang.String.format(lastF2FInteractionFormat, peer.firstName, peer.lastName, output))
        }
    }
}