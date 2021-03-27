package ut.georgeconnect

import georgeconnect.Peer
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DataTests {

    @Test
    fun `create new peer for given first name and last name`() {
        val p = Peer("firstname", "lastname")
        assertEquals(Peer("firstname", "lastname", LocalDate.now().toString()), p, "peer")
    }

    @Test
    fun `create new peer for given first name, last name and last intercation face-2-face date`() {
        val p = Peer("firstname", "lastname", "2021-03-27")
        assertEquals(Peer("firstname", "lastname", "2021-03-27"), p, "peer")
    }

    @Test
    fun `peers are not equal for different first names`() {
        val p1 = Peer("firstname1", "ignore")
        val p2 = Peer("firstname2", "ignore")
        assertNotEquals(p1, p2, "p1 != p2")
    }

    @Test
    fun `peers are not equal for different last names`() {
        val p1 = Peer("ignore", "lastname1")
        val p2 = Peer("ignore", "lastname2")
        assertNotEquals(p1, p2, "p1 != p2")
    }

    @Test
    fun `peers are not equal for different last interaction face-2-face dates`() {
        val p1 = Peer("ignore", "ignore", "2021-03-22")
        val p2 = Peer("ignore", "ignore", "2021-03-23")
        assertNotEquals(p1, p2, "p1 != p2")
    }

    @Test
    fun `peer is not equal to null type`() {
        val p1 = Peer("ignore", "ignore")
        val p2 = null
        @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
        assertNotEquals(p1, p2, "p1 != p2")
    }
}