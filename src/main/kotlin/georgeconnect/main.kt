package georgeconnect

fun main(args: Array<String>) {
    errorHandled(display = ::println) {
        inCase(argsOnlyContaintPath = args.size == 1,
            onShowInteractions = {
                val peers = sortedPeersFrom(path = args[0])
                showLastInteractionsWith(peers, display = ::println)
            },
            onUpdatePeer = {
                updatePeer(args)
                val peers = sortedPeersFrom(path = args[0])
                showLastInteractionsWith(peers, display = ::println)
            }
        )
    }
}