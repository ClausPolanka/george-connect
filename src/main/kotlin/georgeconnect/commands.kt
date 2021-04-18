package georgeconnect

import java.lang.String.format

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
        val result = findPeerBy(firstName, fileAdapter, display)
        when (result.findStatus) {
            FindStatus.SUCCESS -> {
                createOrUpdatePeer(
                    createOrUpdate = ::createOrUpdatePeerOnFileSystem,
                    Peer(result.peer.firstName, result.peer.lastName),
                    onSuccess = ::showInteractions,
                    onError = display,
                    fileAdapter
                )
            }
            FindStatus.DUPLICATE_PEER_BY_FIRST_NAME -> display(format(multipleEntriesFormat, firstName))
            FindStatus.PEER_UNKNOWN -> display(format(peerNotFoundFormat, firstName))
        }
    }
}

class CreateOrUpdateWithCustomDateCmd(
    private val peer: Peer,
    private val date: String,
    private val fileAdapter: FileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdatePeer(
            createOrUpdate = ::createOrUpdatePeerOnFileSystem,
            peer,
            onSuccess = ::showInteractions,
            onError = display,
            fileAdapter
        )
    }
}

class CreateOrUpdatePeerByFirstNameAndLastNameCmd(
    private val peer: Peer,
    private val fileAdapter: FileAdapter,
    private val display: (msg: String) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdatePeer(
            createOrUpdate = ::createOrUpdatePeerOnFileSystem,
            peer,
            onSuccess = ::showInteractions,
            onError = display,
            fileAdapter
        )
    }
}