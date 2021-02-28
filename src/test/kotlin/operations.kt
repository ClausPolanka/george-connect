import org.junit.jupiter.api.Test
import kotlin.math.exp
import kotlin.test.assertEquals

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
}
