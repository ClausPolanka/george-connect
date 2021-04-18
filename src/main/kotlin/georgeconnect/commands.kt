package georgeconnect

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
                    createOrUpdate = ::createOrUpdatePeerOnFileSystem,
                    peer = Peer(result.peer.firstName, result.peer.lastName),
                    onSuccess = ::showInteractions,
                    onError = display,
                    fileAdapter = fileAdapter
                )
            }
            FindStatus.DUPLICATE_PEER_BY_FIRST_NAME -> display(
                java.lang.String.format(
                    multipleEntriesFormat,
                    firstName
                )
            )
            FindStatus.PEER_UNKNOWN -> display(java.lang.String.format(peerNotFoundFormat, firstName))
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
            createOrUpdate = ::createOrUpdatePeerOnFileSystem,
            peer = Peer(firstName, lastName, date),
            onSuccess = ::showInteractions,
            onError = display,
            fileAdapter = fileAdapter
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