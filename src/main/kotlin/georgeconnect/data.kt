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