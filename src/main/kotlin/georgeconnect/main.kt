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
            FileDeserializer(
                dataPath = args[0],
                loadFileData = ::jsonsFrom,
                deserializePeer = Klaxon()::parse
            )
        )
        UPDATE_BY_FIRST_NAME -> UpdatePeerByFirstNameCmd(
            firstName = args[1],
            ::println,
            FileDeserializer(
                dataPath = args[0],
                loadFileData = ::jsonsFrom,
                deserializePeer = Klaxon()::parse
            )
        )
        CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME -> CreateOrUpdatePeerByFirstNameAndLastNameCmd(
            firstName = args[1],
            lastName = args[2],
            FileDeserializer(
                dataPath = args[0],
                loadFileData = ::jsonsFrom,
                deserializePeer = Klaxon()::parse
            )
        )
        CREATE_OR_UPDATE_WITH_CUSTOM_DATE -> CreateOrUpdateWithCustomDateCmd(
            firstName = args[1],
            lastName = args[2],
            date = args[3],
            FileDeserializer(
                dataPath = args[0],
                loadFileData = ::jsonsFrom,
                deserializePeer = Klaxon()::parse
            )
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

data class FileDeserializer(
    val dataPath: String,
    val loadFileData: (path: String) -> List<String>,
    val deserializePeer: (String) -> Peer?
)

class ShowInteractionsCmd(private val fileDeserializer: FileDeserializer) : GeorgeConnectCmd {
    override fun execute() {
        showInteractions(fileDeserializer)
    }
}

class UpdatePeerByFirstNameCmd(
    private val firstName: String,
    private val display: (msg: String) -> Unit,
    private val fileDeserializer: FileDeserializer
) : GeorgeConnectCmd {
    override fun execute() {
        val result = findPeerBy(firstName, fileDeserializer.dataPath)
        when (result.findStatus) {
            FindStatus.SUCCESS -> {
                createOrUpdate(
                    ::createOrUpdateJsonFor,
                    Peer(result.peer.firstName, result.peer.lastName),
                    ::showInteractions,
                    display,
                    fileDeserializer
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
    private val fileDeserializer: FileDeserializer
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdate(
            ::createOrUpdateJsonFor,
            Peer(firstName, lastName, date),
            ::showInteractions,
            ::println,
            fileDeserializer
        )
    }
}

class CreateOrUpdatePeerByFirstNameAndLastNameCmd(
    private val firstName: String,
    private val lastName: String,
    private val fileDeserializer: FileDeserializer
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdate(
            ::createOrUpdateJsonFor,
            Peer(firstName, lastName),
            ::showInteractions,
            ::println,
            fileDeserializer
        )
    }
}
