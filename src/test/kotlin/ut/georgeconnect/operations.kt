import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        val peers = peersFrom(jsons = listOf("json1", "json2"), jsonToPeer = { json -> null })
        assertEquals(emptySet(), peers, "peers")
    }

    @Test
    fun `json value are converted to peers`() {
        val peer = Peer(
            "berni",
            "fleck",
            lastInteractionF2F = "2021-03-05"
        )
        val actual = peersFrom(jsons = listOf("json"), jsonToPeer = { json -> peer })
        assertEquals(setOf(peer), actual, "peers")
    }
}
