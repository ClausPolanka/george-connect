//package ut.georgeconnect
//
//import georgeconnect.*
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertDoesNotThrow
//import org.junit.jupiter.api.assertThrows
//import java.time.LocalDate
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class OperationsTests {
//
//    @Test
//    fun `run george connect without errors`() {
//        var actual = ""
//        errorHandled(
//            display = { /* Ignore */ },
//            georgeConnect = { actual = "ok" }
//        )
//        assertEquals(expected = "ok", actual, "message")
//    }
//
//    @Test
//    fun `peer not found for given first name`() {
//        errorHandled(
//            display = { actual ->
//                assertEquals(expected = "Sorry, couldn't find 'unknown'", actual, "message")
//            },
//            georgeConnect = { throw PeerNotFoundException(firstName = "unknown") }
//        )
//    }
//
//    @Test
//    fun `multiple entries found for given first name`() {
//        errorHandled(
//            display = { actual ->
//                assertEquals(
//                    expected = "Multiple entries found for 'duplicate'. Please also provide last name.",
//                    actual,
//                    "message"
//                )
//            },
//            georgeConnect = { throw MultipleEntriesFoundException(firstName = "duplicate") }
//        )
//    }
//
//    @Test
//    fun `too many args shows george-connect usage`() {
//        errorHandled(
//            display = { actual ->
//                assertTrue(actual.contains("usage"), "usage is '$actual'")
//            },
//            georgeConnect = { throw WrongNumberOfArgsException() }
//        )
//    }
//
//    @Test
//    fun `peer last interaction date has wrong format`() {
//        val p = Peer("firstname", "lastname", lastInteractionF2F = "xxx")
//        errorHandled(
//            display = { actual ->
//                assertTrue(
//                    actual.contains("xxx"),
//                    "message doesn't contain last interaction date: '${p.lastInteractionF2F}'"
//                )
//            },
//            georgeConnect = { throw PeerLastInteractionDateHasWrongFormat(p) }
//        )
//    }
//
//    @Test
//    fun `in case args are empty run corresponding function`() {
//        var actual = ""
//        inCase(argsOnlyContainPath = true, onShowInteractions = { actual = "ok" }, onUpdatePeer = { /* Ignore */ })
//        assertEquals(expected = "ok", actual, "args are empty")
//    }
//
//    @Test
//    fun `in case args are nonempty run corresponding function`() {
//        var actual = ""
//        inCase(argsOnlyContainPath = false, onShowInteractions = { /* Ignore */ }, onUpdatePeer = { actual = "ok" })
//        assertEquals(expected = "ok", actual, "args are nonempty")
//    }
//
//    @Test
//    fun `parse args containing four values`() {
//        val actual = parse(
//            args = arrayOf("path", "firstname", "lastname", "2021-03-21"),
//            ::parseFourArgs,
//            { ignore() },
//            { _, _ -> ignore() },
//            { _, _ -> ignore() },
//        )
//        assertEquals(
//            expected = Pair("path", Peer("firstname", "lastname", "2021-03-21")),
//            actual,
//            "result"
//        )
//    }
//
//    @Test
//    fun `parse args containing three values`() {
//        val actual = parse(
//            args = arrayOf("path", "firstname", "lastname"),
//            { ignore() },
//            ::parseThreeArgs,
//            { _, _ -> ignore() },
//            { _, _ -> ignore() },
//        )
//        assertEquals(
//            expected = Pair("path", Peer("firstname", "lastname", LocalDate.now().toString())),
//            actual,
//            "result"
//        )
//    }
//
//    @Test
//    fun `parse args containing two values`() {
//        val actual = parse(
//            args = arrayOf("path", "firstname"),
//            { ignore() },
//            { ignore() },
//            ::parseTwoArgs,
//            { firstName, _ ->  Peer(firstName, "lastname", LocalDate.now().toString())}
//        )
//        assertEquals(
//            expected = Pair("path", Peer("firstname", "lastname", LocalDate.now().toString())),
//            actual,
//            "result"
//        )
//    }
//
//    @Test
//    fun `parse args containing two values where first name is unknown`() {
//        val exception = assertThrows<PeerNotFoundException> {
//            parse(
//                args = arrayOf("path", "unknown"),
//                { ignore() },
//                { ignore() },
//                ::parseTwoArgs,
//                findBy = { _, _ -> null }
//            )
//        }
//        assertEquals(expected = "unknown", exception.firstName, "peer first name")
//    }
//
//    @Test
//    fun `parse args containing too many values`() {
//        assertThrows<WrongNumberOfArgsException> {
//            parse(
//                args = arrayOf("too", "many", "args", "foo", "bar"),
//                { ignore() },
//                { ignore() },
//                { _, _ ->  ignore() },
//                { _, _ -> ignore() }
//            )
//        }
//    }
//
//    @Test
//    fun `parse args containing not enough values`() {
//        assertThrows<WrongNumberOfArgsException> {
//            parse(
//                args = emptyArray(),
//                { ignore() },
//                { ignore() },
//                { _, _ ->  ignore() },
//                { _, _ -> ignore() }
//            )
//        }
//    }
//
//    @Test
//    fun `peers must not contain nulls`() {
//        val peers = peersFrom(jsons = listOf("json1", "json2"), jsonToPeer = { null })
//        assertEquals(emptySet(), peers, "peers")
//    }
//
//    @Test
//    fun `json value is converted to peer`() {
//        val peer = Peer(
//            "firstname",
//            "lastname",
//            lastInteractionF2F = "2021-03-05"
//        )
//        val actual = peersFrom(jsons = listOf("json"), jsonToPeer = { peer })
//        assertEquals(setOf(peer), actual, "peers")
//    }
//
//    @Test
//    fun `throw if duplicate peers exist for given first name`() {
//        val p1 = Peer("firstname", "lastname1", lastInteractionF2F = "2021-03-05")
//        val p2 = Peer("firstname", "lastname2", lastInteractionF2F = "2021-03-05")
//        val peers = mutableSetOf(p1, p2)
//
//        assertThrows<MultipleEntriesFoundException> {
//            peers.throwIfDuplicatesExistFor("firstname")
//        }
//    }
//
//    @Test
//    fun `does not throw if no duplicate peers exist for given first name`() {
//        val p1 = Peer("firstname1", "lastname1", lastInteractionF2F = "2021-03-05")
//        val p2 = Peer("firstname2", "lastname2", lastInteractionF2F = "2021-03-05")
//        val peers = mutableSetOf(p1, p2)
//
//        assertDoesNotThrow {
//            peers.throwIfDuplicatesExistFor("firstname1")
//        }
//    }
//
//    @Test
//    fun `does not throw if no peer exists for given first name`() {
//        val p = Peer("firstname", "lastname", lastInteractionF2F = "2021-03-05")
//        val peers = mutableSetOf(p)
//
//        assertDoesNotThrow {
//            peers.throwIfDuplicatesExistFor("unknown")
//        }
//    }
//
//    @Test
//    fun `last interaction in days for given last interaction date`() {
//        val p = Peer("firstname", "lastname", "2021-03-05")
//        val days = p.lastInteractionF2FInDays(now = { LocalDate.of(2021, 3, 10) })
//        assertEquals(expected = 5, days, "last interaction in days")
//    }
//
//    @Test
//    fun `last interaction date has wrong format`() {
//        val p = Peer("firstname", "lastname", lastInteractionF2F = "xxx")
//        assertThrows<PeerLastInteractionDateHasWrongFormat> {
//            p.lastInteractionF2FInDays(now = ::ignore)
//        }
//    }
//
//    @Test
//    fun `output for today`() {
//        val output = outputFor(days = 0)
//        assertEquals(expected = "today", output, "output for days")
//    }
//
//    @Test
//    fun `output for yesterday`() {
//        val output = outputFor(days = 1)
//        assertEquals(expected = "1 day ago", output, "output for days")
//    }
//
//    @Test
//    fun `output for more than one day ago`() {
//        val output = outputFor(days = 2)
//        assertEquals(expected = "2 days ago", output, "output for days")
//    }
//
//    private fun ignore(): Nothing = throw NotImplementedError()
//}
