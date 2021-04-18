package georgeconnect

fun main(args: Array<String>) {
    val gc = parse(args, ::argsToCommands, ::createJsonKlaxonFileAdapter, display = ::println)
    gc.execute()
}

