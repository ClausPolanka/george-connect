package georgeconnect

fun main(args: Array<String>) {
    val path = args[0]
    errorHandled(display = ::println) {
        inCase(argsOnlyContainPath = args.size == 1,
            onShowInteractions = {
                showInteractions(path)
            },
            onUpdatePeer = {
                updatePeer(args)
                showInteractions(path)
            }
        )
    }
}