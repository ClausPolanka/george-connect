package ut.georgeconnect

import georgeconnect.*
import georgeconnect.FindStatus.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class OperationsTests {

    @Test
    fun `peers must not contain nulls`() {
        var actualMsg = ""
        val peers = peersFrom(
            serializedPeers = listOf("json"),
            deserializePeer = { _ -> null },
            display = { msg -> actualMsg = msg }
        )
        assertEquals(emptySet(), peers, "peers")
        assertEquals(expected = "Please check 'json'. Could not be deserialized", actualMsg, "message")
    }

    @Test
    fun `json value is converted to peer`() {
        val peer = Peer("firstname", "lastname", lastInteractionF2F = "2021-03-05")
        val peers = peersFrom(
            serializedPeers = listOf("json"),
            deserializePeer = { _ -> peer },
            display = { ignore() }
        )
        assertEquals(setOf(peer), peers, "peers")
    }

    @Test
    fun `two peers with same first name yield duplication status`() {
        val p1 = Peer("firstname", "lastname1", lastInteractionF2F = "2021-03-05")
        val p2 = Peer("firstname", "lastname2", lastInteractionF2F = "2021-03-05")
        val peers = mutableSetOf(p1, p2)

        val result = findDuplicates(peers, "firstname")

        assertEquals(
            expected = FindResult(
                Peer("firstname", "duplicate"),
                findStatus = DUPLICATE_PEER_BY_FIRST_NAME
            ),
            actual = result,
            "find result"
        )
    }

    @Test
    fun `two peers with different first names yield success status and found peer`() {
        val p1 = Peer("firstname1", "lastname1", lastInteractionF2F = "2021-03-05")
        val p2 = Peer("firstname2", "lastname2", lastInteractionF2F = "2021-03-05")
        val peers = mutableSetOf(p1, p2)

        val result = findDuplicates(peers, "firstname1")

        assertEquals(
            expected = FindResult(p1, findStatus = SUCCESS),
            actual = result,
            "find result"
        )
    }

    @Test
    fun `one peer where firstname is in upper case but equals search term yields success status`() {
        val p = Peer("FIRSTNAME", "lastname", lastInteractionF2F = "2021-03-05")
        val peers = mutableSetOf(p)

        val result = findDuplicates(peers, "firstname")

        assertEquals(
            expected = FindResult(p, findStatus = SUCCESS),
            actual = result,
            "find result"
        )
    }

    @Test
    fun `one peer where firstname is in lower case but equals search term yields success status`() {
        val p = Peer("firstname", "lastname", lastInteractionF2F = "2021-03-05")
        val peers = mutableSetOf(p)

        val result = findDuplicates(peers, "FIRSTNAME")

        assertEquals(
            expected = FindResult(p, findStatus = SUCCESS),
            actual = result,
            "find result"
        )
    }

    @Test
    fun `two peers not containing search term yield peer unknown status`() {
        val p1 = Peer("firstname1", "lastname1", lastInteractionF2F = "2021-03-05")
        val p2 = Peer("firstname2", "lastname2", lastInteractionF2F = "2021-03-05")
        val peers = mutableSetOf(p1, p2)

        val result = findDuplicates(peers, "firstname3")

        assertEquals(
            expected = FindResult(Peer("firstname3", "unknown"), findStatus = PEER_UNKNOWN),
            actual = result,
            "find result"
        )
    }

    @Test
    fun `last interaction in days for given last interaction date`() {
        val p = Peer("firstname", "lastname", "2021-03-05")
        val days = p.lastInteractionF2FInDays(now = { LocalDate.of(2021, 3, 10) })
        assertEquals(expected = 5, days, "last interaction in days")
    }

    @Test
    fun `output for today`() {
        val output = outputFor(days = 0)
        assertEquals(expected = "today", output, "output for days")
    }

    @Test
    fun `output for yesterday`() {
        val output = outputFor(days = 1)
        assertEquals(expected = "1 day ago", output, "output for days")
    }

    @Test
    fun `output for more than one day ago`() {
        val output = outputFor(days = 2)
        assertEquals(expected = "2 days ago", output, "output for days")
    }

    private fun ignore(): Nothing = throw NotImplementedError()
}
