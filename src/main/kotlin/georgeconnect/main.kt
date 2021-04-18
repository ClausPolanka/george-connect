package georgeconnect

import com.beust.klaxon.Klaxon
import georgeconnect.GeorgeConnectCommands.*
import java.lang.String.format

fun main(args: Array<String>) {
    val gc = parse(args)
    gc.execute()
}

fun parse(args: Array<String>): GeorgeConnectCmd {
    return when (toCommand(args)) {
        WRONG_NR_OF_ARGS -> ShowUsageCmd()
        SHOW_INTERACTIONS -> ShowInteractionsCmd(
            FileAdapter(
                dataPath = args[0],
                loadFileData = ::filesFrom,
                deserializePeer = Klaxon()::parse,
                serializePeer = Klaxon()::toJsonString,
                extension = "json"
            ),
            display = ::println
        )
        UPDATE_BY_FIRST_NAME -> UpdatePeerByFirstNameCmd(
            firstName = args[1],
            FileAdapter(
                dataPath = args[0],
                loadFileData = ::filesFrom,
                deserializePeer = Klaxon()::parse,
                serializePeer = Klaxon()::toJsonString,
                extension = "json"
            ),
            display = ::println
        )
        CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME -> CreateOrUpdatePeerByFirstNameAndLastNameCmd(
            firstName = args[1],
            lastName = args[2],
            FileAdapter(
                dataPath = args[0],
                loadFileData = ::filesFrom,
                deserializePeer = Klaxon()::parse,
                serializePeer = Klaxon()::toJsonString,
                extension = "json"
            ),
            display = ::println
        )
        CREATE_OR_UPDATE_WITH_CUSTOM_DATE -> CreateOrUpdateWithCustomDateCmd(
            firstName = args[1],
            lastName = args[2],
            date = args[3],
            FileAdapter(
                dataPath = args[0],
                loadFileData = ::filesFrom,
                deserializePeer = Klaxon()::parse,
                serializePeer = Klaxon()::toJsonString,
                extension = "json"
            ),
            display = ::println
        )
    }
}

interface GeorgeConnectCmd {
    fun execute()
}

class ShowUsageCmd : GeorgeConnectCmd {
    override fun execute() {
        println(usage)
    }
}

class ShowInteractionsCmd(
    private val fileAdapter: FileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        showInteractions(fileAdapter, display)
    }
}

class UpdatePeerByFirstNameCmd(
    private val firstName: String,
    private val fileAdapter: FileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        val result = findPeerBy(firstName, fileAdapter)
        when (result.findStatus) {
            FindStatus.SUCCESS -> {
                createOrUpdatePeer(
                    ::createOrUpdatePeerOnFileSystem,
                    Peer(result.peer.firstName, result.peer.lastName),
                    ::showInteractions,
                    display,
                    fileAdapter
                )
            }
            FindStatus.DUPLICATE_PEER_BY_FIRST_NAME -> display(format(multipleEntriesFormat, firstName))
            FindStatus.PEER_UNKNOWN -> display(format(peerNotFoundFormat, firstName))
        }
    }
}

class CreateOrUpdateWithCustomDateCmd(
    private val firstName: String,
    private val lastName: String,
    private val date: String,
    private val fileAdapter: FileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdatePeer(
            ::createOrUpdatePeerOnFileSystem,
            Peer(firstName, lastName, date),
            ::showInteractions,
            display,
            fileAdapter
        )
    }
}

class CreateOrUpdatePeerByFirstNameAndLastNameCmd(
    private val firstName: String,
    private val lastName: String,
    private val fileAdapter: FileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdatePeer(
            ::createOrUpdatePeerOnFileSystem,
            Peer(firstName, lastName),
            ::showInteractions,
            display,
            fileAdapter
        )
    }
}
