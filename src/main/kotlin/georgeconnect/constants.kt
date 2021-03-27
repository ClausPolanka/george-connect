package georgeconnect

val usage: String = """usage
            |george-connect <path>                                         list all peer face-to-face interactions
            |george-connect <path> <first_name>                            log new peer face-to-face interaction for existing peer
            |george-connect <path> <first_name> <last_name>                log new peer face-to-face interaction for existing or new peer
            |george-connect <path> <first_name> <last_name> <YYYY-MM-DD>   log new peer face-to-face interaction for existing or new peer by providing custom date
        """.trimMargin()

const val peerNotFoundFormat = "Sorry, couldn't find '%s'"
const val multipleEntriesFormat = "Multiple entries found for '%s'. Please also provide last name."
const val dateHasWrongFormat = "Unfortunately the last interaction date for '%s %s' has an unknown format: '%s'"
