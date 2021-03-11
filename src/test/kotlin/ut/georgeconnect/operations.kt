import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OperationsTests {

    @Test
    fun `run program without errors`() {
        var actual = ""
        errorHandled(
            display = { /* Ignore */ },
            fn = { actual = "ok" }
        )
        assertEquals(expected = "ok", actual, "message")
    }

    @Test
    fun `peer not found for given first name`() {
        errorHandled(
            display = { actual ->
                assertEquals(expected = "Sorry, couldn't find 'unknown'", actual, "message")
            },
            fn = { throw PeerNotFoundException(firstName = "unknown") }
        )
    }

    @Test
    fun `multiple entries found for given first name`() {
        errorHandled(
            display = { actual ->
                assertEquals(
                    expected = "Multiple entries found for 'duplicate'. Please also provide last name.",
                    actual,
                    "message"
                )
            },
            fn = { throw MultipleEntriesFoundException(firstName = "duplicate") }
        )
    }

    @Test
    fun `too many args shows george-connect usage`() {
        errorHandled(
            display = { actual ->
                assertTrue(actual.contains("usage"), "usage is '$actual'")
            },
            fn = { throw TooManyArgsException() }
        )
    }

    @Test
    fun `peer last interaction date has wrong format`() {
        val lastInteraction = "xxx"
        errorHandled(
            display = { actual ->
                assertTrue(
                    actual.contains(lastInteraction),
                    "message doesn't contain last interaction date: '$lastInteraction'"
                )
            },
            fn = { throw PeerLastInteractionDateHasWrongFormat(lastInteraction) }
        )
    }

    @Test
    fun `in case args are empty run corresponding function`() {
        var actual = ""
        inCase(argsAreEmpty = true, onEmpty = { actual = "ok" }, onNonEmpty = { /* Ignore */ })
        assertEquals(expected = "ok", actual, "args are empty")
    }

    @Test
    fun `in case args are nonempty run corresponding function`() {
        var actual = ""
        inCase(argsAreEmpty = false, onEmpty = { /* Ignore */ }, onNonEmpty = { actual = "ok" })
        assertEquals(expected = "ok", actual, "args are nonempty")
    }

    @Test
    fun `parse args containing two values`() {
        val actual = parse(args = arrayOf("firstname", "lastname"), findBy = { null })
        assertEquals(expected = Pair("firstname", "lastname"), actual, "result")
    }

    @Test
    fun `parse args containing one value`() {
        val actual = parse(
            args = arrayOf("firstname"),
            findBy = { Peer("firstname", "lastname", "ignore") }
        )
        assertEquals(expected = Pair("firstname", "lastname"), actual, "result")
    }

    @Test
    fun `parse args containing one value which is unknown`() {
        val exception = assertThrows<PeerNotFoundException> {
            parse(args = arrayOf("unknown"), findBy = { null })
        }
        assertEquals(expected = "unknown", exception.firstName, "peer first name")
    }

    @Test
    fun `parse args containing too many values`() {
        assertThrows<TooManyArgsException> {
            parse(args = arrayOf("too", "many", "args"), findBy = { null })
        }
    }

    @Test
    fun `peers must not contain nulls`() {
        val peers = peersFrom(jsons = listOf("json1", "json2"), jsonToPeer = { null })
        assertEquals(emptySet(), peers, "peers")
    }

    @Test
    fun `json value is converted to peer`() {
        val peer = Peer(
            "firstname",
            "lastname",
            lastInteractionF2F = "2021-03-05"
        )
        val actual = peersFrom(jsons = listOf("json"), jsonToPeer = { peer })
        assertEquals(setOf(peer), actual, "peers")
    }

    @Test
    fun `throw if duplicate peers exist for given first name`() {
        val p1 = Peer("firstname", "lastname1", lastInteractionF2F = "2021-03-05")
        val p2 = Peer("firstname", "lastname2", lastInteractionF2F = "2021-03-05")
        val peers = mutableSetOf(p1, p2)

        assertThrows<MultipleEntriesFoundException> {
            peers.throwIfDuplicatesExistFor("firstname")
        }
    }

    @Test
    fun `does not throw if no duplicate peers exist for given first name`() {
        val p1 = Peer("firstname1", "lastname1", lastInteractionF2F = "2021-03-05")
        val p2 = Peer("firstname2", "lastname2", lastInteractionF2F = "2021-03-05")
        val peers = mutableSetOf(p1, p2)

        assertDoesNotThrow {
            peers.throwIfDuplicatesExistFor("firstname1")
        }
    }

    @Test
    fun `does not throw if no peer exists for given first name`() {
        val p1 = Peer("firstname", "lastname", lastInteractionF2F = "2021-03-05")
        val peers = mutableSetOf(p1)

        assertDoesNotThrow {
            peers.throwIfDuplicatesExistFor("unknown")
        }
    }

    @Test
    fun `last interaction in days for given last interaction date`() {
        val days = toDays(lastInteraction = "2021-03-05") { LocalDate.of(2021, 3, 10) }
        assertEquals(expected = 5, days, "last interaction in days")
    }

    @Test
    fun `last interaction date has wrong format`() {
        assertThrows<PeerLastInteractionDateHasWrongFormat> {
            toDays(lastInteraction = "xxx", ::IGNORE)
        }
    }

    private fun IGNORE(): Nothing = throw NotImplementedError()
}
