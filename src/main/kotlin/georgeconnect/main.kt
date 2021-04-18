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
            fileAdapter(
                dataPath = args[0],
                loadFileData = ::jsonsFrom,
                deserializePeer = Klaxon()::parse
            ),
            display = ::println
        )
        UPDATE_BY_FIRST_NAME -> UpdatePeerByFirstNameCmd(
            firstName = args[1],
            fileAdapter(
                dataPath = args[0],
                loadFileData = ::jsonsFrom,
                deserializePeer = Klaxon()::parse
            ),
            display = ::println
        )
        CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME -> CreateOrUpdatePeerByFirstNameAndLastNameCmd(
            firstName = args[1],
            lastName = args[2],
            fileAdapter(
                dataPath = args[0],
                loadFileData = ::jsonsFrom,
                deserializePeer = Klaxon()::parse
            ),
            display = ::println
        )
        CREATE_OR_UPDATE_WITH_CUSTOM_DATE -> CreateOrUpdateWithCustomDateCmd(
            firstName = args[1],
            lastName = args[2],
            date = args[3],
            fileAdapter(
                dataPath = args[0],
                loadFileData = ::jsonsFrom,
                deserializePeer = Klaxon()::parse
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
    private val fileAdapter: fileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        showInteractions(fileAdapter, display)
    }
}

class UpdatePeerByFirstNameCmd(
    private val firstName: String,
    private val fileAdapter: fileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        val result = findPeerBy(firstName, fileAdapter(fileAdapter.dataPath, ::jsonsFrom, Klaxon()::parse))
        when (result.findStatus) {
            FindStatus.SUCCESS -> {
                createOrUpdate(
                    ::createOrUpdateJsonFor,
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
    private val fileAdapter: fileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdate(
            ::createOrUpdateJsonFor,
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
    private val fileAdapter: fileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdate(
            ::createOrUpdateJsonFor,
            Peer(firstName, lastName),
            ::showInteractions,
            display,
            fileAdapter
        )
    }
}
