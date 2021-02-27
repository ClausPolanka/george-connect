fun main(args: Array<String>) {
    errorHandled {
        inCase(args.isEmpty(),
            onEmpty = {
                val peers = sortedPeers()
                showLastInteractionsWith(peers, display = ::println)
            },
            onNonEmpty = {
                updatePeer(args)
                val peers = sortedPeers()
                showLastInteractionsWith(peers, display = ::println)
            }
        )
    }
}