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
            dataPath = args[0],
            loadFileData = ::jsonsFrom,
            deserializePeer = Klaxon()::parse
        )
        UPDATE_BY_FIRST_NAME -> UpdatePeerByFirstNameCmd(
            dataPath = args[0],
            firstName = args[1],
            ::println,
            loadFileData = ::jsonsFrom,
            deserializePeer = Klaxon()::parse
        )
        CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME -> CreateOrUpdatePeerByFirstNameAndLastNameCmd(
            dataPath = args[0],
            firstName = args[1],
            lastName = args[2],
            loadFileData = ::jsonsFrom,
            deserializePeer = Klaxon()::parse
        )
        CREATE_OR_UPDATE_WITH_CUSTOM_DATE -> CreateOrUpdateWithCustomDateCmd(
            dataPath = args[0],
            firstName = args[1],
            lastName = args[2],
            date = args[3],
            loadFileData = ::jsonsFrom,
            deserializePeer = Klaxon()::parse
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
    private val dataPath: String,
    private val loadFileData: (path: String) -> List<String>,
    private val deserializePeer: (String) -> Peer?
) : GeorgeConnectCmd {
    override fun execute() {
        showInteractions(dataPath, loadFileData, deserializePeer)
    }
}

class UpdatePeerByFirstNameCmd(
    private val dataPath: String,
    private val firstName: String,
    private val display: (msg: String) -> Unit,
    private val loadFileData: (path: String) -> List<String>,
    private val deserializePeer: (String) -> Peer?
) : GeorgeConnectCmd {
    override fun execute() {
        val result = findPeerBy(firstName, dataPath)
        when (result.findStatus) {
            FindStatus.SUCCESS -> {
                createOrUpdate(
                    ::createOrUpdateJsonFor,
                    dataPath,
                    Peer(result.peer.firstName, result.peer.lastName),
                    ::showInteractions,
                    display,
                    loadFileData,
                    deserializePeer
                )
            }
            FindStatus.DUPLICATE_PEER_BY_FIRST_NAME -> display(format(multipleEntriesFormat, firstName))
            FindStatus.PEER_UNKNOWN -> display(format(peerNotFoundFormat, firstName))
        }
    }
}

class CreateOrUpdateWithCustomDateCmd(
    private val dataPath: String,
    private val firstName: String,
    private val lastName: String,
    private val date: String,
    private val loadFileData: (path: String) -> List<String>,
    private val deserializePeer: (String) -> Peer?
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdate(
            ::createOrUpdateJsonFor,
            dataPath,
            Peer(firstName, lastName, date),
            ::showInteractions,
            ::println,
            loadFileData,
            deserializePeer
        )
    }
}

class CreateOrUpdatePeerByFirstNameAndLastNameCmd(
    private val dataPath: String,
    private val firstName: String,
    private val lastName: String,
    private val loadFileData: (path: String) -> List<String>,
    private val deserializePeer: (String) -> Peer?
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdate(
            ::createOrUpdateJsonFor,
            dataPath,
            Peer(firstName, lastName),
            ::showInteractions,
            ::println,
            loadFileData,
            deserializePeer
        )
    }
}
