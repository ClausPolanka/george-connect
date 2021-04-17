package georgeconnect

import georgeconnect.GeorgeConnectCommands.*
import java.lang.String.format

fun main(args: Array<String>) {
    val gc = parse(args)
    gc.execute()
}

fun parse(args: Array<String>): GeorgeConnectCmd {
    return when (toCommand(args)) {
        WRONG_NR_OF_ARGS -> ShowUsageCmd()
        SHOW_INTERACTIONS -> ShowInteractionsCmd(dataPath = args[0])
        UPDATE_BY_FIRST_NAME -> UpdatePeerByFirstNameCmd(dataPath = args[0], firstName = args[1])
        CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME -> CreateOrUpdatePeerByFirstNameAndLastNameCmd(
            dataPath = args[0],
            firstName = args[1],
            lastName = args[2]
        )
        CREATE_OR_UPDATE_WITH_CUSTOM_DATE -> CreateOrUpdateWithCustomDateCmd(
            dataPath = args[0],
            firstName = args[1],
            lastName = args[2],
            date = args[3]
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

class ShowInteractionsCmd(val dataPath: String) : GeorgeConnectCmd {
    override fun execute() {
        showInteractions(dataPath)
    }
}

class UpdatePeerByFirstNameCmd(val dataPath: String, val firstName: String) : GeorgeConnectCmd {
    override fun execute() {
        val result = findPeerBy(firstName, dataPath)
        when (result.findStatus) {
            FindStatus.SUCCESS -> {
                if (result.peer == null) {
                    return
                }
                createOrUpdate(
                    ::createOrUpdateJsonFor,
                    dataPath,
                    Peer(result.peer.firstName, result.peer.lastName),
                    ::showInteractions,
                    ::println
                )
            }
            FindStatus.DUPLICATE_PEER_BY_FIRST_NAME -> println(format(multipleEntriesFormat, firstName))
            FindStatus.PEER_UNKNOWN -> println(format(peerNotFoundFormat, firstName))
        }
    }
}

class CreateOrUpdateWithCustomDateCmd(
    val dataPath: String,
    val firstName: String,
    val lastName: String,
    val date: String,
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdate(
            ::createOrUpdateJsonFor,
            dataPath,
            Peer(firstName, lastName, date),
            ::showInteractions,
            ::println
        )
    }
}

class CreateOrUpdatePeerByFirstNameAndLastNameCmd(
    val dataPath: String,
    val firstName: String,
    val lastName: String
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdate(
            ::createOrUpdateJsonFor,
            dataPath,
            Peer(firstName, lastName),
            ::showInteractions,
            ::println
        )
    }
}
