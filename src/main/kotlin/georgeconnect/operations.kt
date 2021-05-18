package georgeconnect

import com.beust.klaxon.Klaxon
import georgeconnect.FindStatus.*
import georgeconnect.GeorgeConnectCommands.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun parse(
    args: Array<String>,
    argsToCommands: (args: Array<String>) -> GeorgeConnectCommands,
    createFileAdapter: (args: Array<String>) -> FileAdapter,
    display: (msg: String) -> Unit
): GeorgeConnectCmd {
    return when (argsToCommands(args)) {
        WRONG_NR_OF_ARGS -> ShowUsageCmd()
        SHOW_INTERACTIONS -> ShowInteractionsCmd(createFileAdapter(args), display)
        UPDATE_BY_FIRST_NAME -> UpdatePeerByFirstNameCmd(
            firstName = args[1],
            createFileAdapter(args),
            display,
            ::createOrUpdateAndShowPeers
        )
        CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME -> CreateOrUpdatePeerByFirstNameAndLastNameCmd(
            Peer(firstName = args[1], lastName = args[2]),
            createFileAdapter(args),
            display,
            ::createOrUpdateAndShowPeers
        )
        CREATE_OR_UPDATE_WITH_CUSTOM_DATE -> CreateOrUpdateWithCustomDateCmd(
            Peer(firstName = args[1], lastName = args[2], lastInteractionF2F = args[3]),
            createFileAdapter(args),
            display,
            ::createOrUpdateAndShowPeers
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

fun peersFrom(
    serializedPeers: List<String>,
    deserializePeer: (String) -> Peer?,
    display: (msg: String) -> Unit
): MutableSet<Peer> {
    return serializedPeers.mapNotNull {
        when (val deserialized = deserializePeer(it)) {
            null -> {
                display("Please check '$it'. Could not be deserialized")
                deserialized
            }
            else -> deserialized
        }
    }.toMutableSet()
}

fun createOrUpdatePeerOnFileSystem(p: Peer, fa: FileAdapter): CreateOrUpdateStatus {
    val serializedPeer = fa.serializePeer(p)
    return try {
        val f = File("${fa.dataPath}/${p.lastName}_${p.firstName}.${fa.extension}")
        f.writeText(serializedPeer)
        CreateOrUpdateStatus.SUCCESS
    } catch (e: Exception) {
        CreateOrUpdateStatus.ERROR
    }
}

fun validateInput(
    p: Peer,
    onValid: (
        createOrUpdate: (p: Peer, fa: FileAdapter) -> CreateOrUpdateStatus,
        peer: Peer,
        onSuccess: (fa: FileAdapter, display: (msg: String) -> Unit) -> Unit,
        onError: (msg: String) -> Unit,
        fa: FileAdapter
    ) -> Unit,
    onInvalid: (msg: String) -> Unit,
    fa: FileAdapter
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    try {
        LocalDate.parse(p.lastInteractionF2F, formatter)
        onValid(::createOrUpdatePeerOnFileSystem, p, ::showInteractions, onInvalid, fa)
    } catch (e: Exception) {
        onInvalid("Please check '$p' last interaction date. Expected date format: yyyy-MM-dd")
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
    createOrUpdate: (p: Peer, fa: FileAdapter) -> CreateOrUpdateStatus,
    peer: Peer,
    onSuccess: (fa: FileAdapter, display: (msg: String) -> Unit) -> Unit,
    onError: (msg: String) -> Unit,
    fa: FileAdapter
) {
    when (createOrUpdate(peer, fa)) {
        CreateOrUpdateStatus.SUCCESS -> onSuccess(fa, onError)
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

fun findDuplicates(peers: MutableSet<Peer>, firstName: String): FindResult {
    val potentialDuplicates = peers.filter { it.firstName.equals(firstName, ignoreCase = true) }
    return when {
        potentialDuplicates.size > 1 -> FindResult(
            Peer(firstName, "duplicate"),
            findStatus = DUPLICATE_PEER_BY_FIRST_NAME
        )
        potentialDuplicates.size == 1 -> FindResult(
            Peer(firstName, potentialDuplicates[0].lastName, potentialDuplicates[0].lastInteractionF2F),
            findStatus = SUCCESS
        )
        else -> FindResult(
            Peer(firstName, "unknown"),
            findStatus = PEER_UNKNOWN
        )
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