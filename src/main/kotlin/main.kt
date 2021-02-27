fun main(args: Array<String>) {
    errorHandled {
        inCase(args.isEmpty(),
            onEmpty = {
                val peers = sortedPeers()
                show(peers, ::println)
            },
            onNonEmpty = {
                updatePeer(args)
                val peers = sortedPeers()
                show(peers, ::println)
            }
        )
    }
}