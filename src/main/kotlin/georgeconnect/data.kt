package georgeconnect

import java.time.LocalDate

class Peer(
    firstName: String,
    lastName: String,
    var lastInteractionF2F: String = LocalDate.now().toString()

) {
    val lastName = lastName.toLowerCase()
    val firstName = firstName.toLowerCase()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Peer

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (lastInteractionF2F != other.lastInteractionF2F) return false

        return true
    }

    override fun hashCode(): Int {
        return 1
    }

    override fun toString(): String {
        return "Peer(lastInteractionF2F='$lastInteractionF2F', lastName='$lastName', firstName='$firstName')"
    }
}

data class FindResult(val peer: Peer, val findStatus: FindStatus)

enum class FindStatus {
    SUCCESS, DUPLICATE_PEER_BY_FIRST_NAME, PEER_UNKNOWN
}

enum class CreateOrUpdateStatus {
    SUCCESS, ERROR
}

enum class GeorgeConnectCommands {
    SHOW_INTERACTIONS,
    WRONG_NR_OF_ARGS,
    UPDATE_BY_FIRST_NAME,
    CREATE_OR_UPDATE_BY_FIRST_NAME_AND_LAST_NAME,
    CREATE_OR_UPDATE_WITH_CUSTOM_DATE
}

data class FileAdapter(
    val dataPath: String,
    val loadFileData: (path: String, extension: String) -> List<String>,
    val deserializePeer: (String) -> Peer?,
    val serializePeer: (Peer) -> String,
    val extension: String
)