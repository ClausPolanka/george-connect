package georgeconnect

fun main(args: Array<String>) {
    val gc = parse(args, ::argsToCommands)
    gc.execute()
}

