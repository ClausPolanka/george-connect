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
    private val display: (msg: String) -> Unit,
    private val createOrUpdateAndShowPeers: (p: Peer, display: (msg: String) -> Unit, fa: FileAdapter) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        val result = findPeerBy(firstName, fileAdapter, display)
        when (result.findStatus) {
            FindStatus.SUCCESS -> createOrUpdateAndShowPeers(
                Peer(result.peer.firstName, result.peer.lastName),
                display,
                fileAdapter
            )
            FindStatus.DUPLICATE_PEER_BY_FIRST_NAME -> display(format(multipleEntriesFormat, result.peer.firstName))
            FindStatus.PEER_UNKNOWN -> display(format(peerNotFoundFormat, result.peer.firstName))
        }
    }
}

class CreateOrUpdateWithCustomDateCmd(
    private val peer: Peer,
    private val fileAdapter: FileAdapter,
    private val display: (msg: String) -> Unit,
    private val createOrUpdateAndShowPeers: (p: Peer, display: (msg: String) -> Unit, fa: FileAdapter) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdateAndShowPeers(peer, display, fileAdapter)
    }
}

class CreateOrUpdatePeerByFirstNameAndLastNameCmd(
    private val peer: Peer,
    private val fileAdapter: FileAdapter,
    private val display: (msg: String) -> Unit,
    private val createOrUpdateAndShowPeers: (p: Peer, display: (msg: String) -> Unit, fa: FileAdapter) -> Unit
) : GeorgeConnectCmd {
    override fun execute() {
        createOrUpdateAndShowPeers(peer, display, fileAdapter)
    }
}