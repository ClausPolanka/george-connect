package it.georgeconnect

import georgeconnect.main
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.assertEquals

class EndToEndTests {

    private val systemIn = System.`in`
    private val systemOut = System.out
    private val programOutput = ByteArrayOutputStream()

    @BeforeEach
    fun setUp() {
        System.setOut(PrintStream(programOutput))
    }

    @AfterEach
    fun tearDown() {
        System.setIn(systemIn)
        System.setOut(systemOut)
    }

    @Test
    fun `show face-2-face peer interactions`(@TempDir tempDir: Path) {
        val jsonFile1 = tempDir.resolve("lastname1_firstname1.json")
        val jsonFile2 = tempDir.resolve("lastname2_firstname2.json")

        val date1 = LocalDate.now().minusDays(1)
        val date2 = LocalDate.now().minusDays(2)

        val json1 = """{"firstName" : "firstname1", "lastInteractionF2F" : "$date1", "lastName" : "lastname1"}"""
        val json2 = """{"firstName" : "firstname2", "lastInteractionF2F" : "$date2", "lastName" : "lastname2"}"""

        Files.write(jsonFile1, json1.toByteArray(Charsets.UTF_8))
        Files.write(jsonFile2, json2.toByteArray(Charsets.UTF_8))

        main(arrayOf(tempDir.toString()))

        assertEquals(
            expected = """Last F2F interaction with firstname1 lastname1 1 day ago
                         |Last F2F interaction with firstname2 lastname2 2 days ago
                         |""".trimMargin(),
            actual = programOutput.toString(),
            message = "output"
        )
    }

    @Test
    fun `update peer based on firstname`(@TempDir tempDir: Path) {
        val jsonFile1 = tempDir.resolve("lastname1_firstname1.json")
        val jsonFile2 = tempDir.resolve("lastname2_firstname2.json")

        val date1 = LocalDate.now().minusDays(1)
        val date2 = LocalDate.now().minusDays(2)

        val json1 = """{"firstName" : "Firstname1", "lastInteractionF2F" : "$date1", "lastName" : "lastname1"}"""
        val json2 = """{"firstName" : "firstname2", "lastInteractionF2F" : "$date2", "lastName" : "lastname2"}"""

        Files.write(jsonFile1, json1.toByteArray(Charsets.UTF_8))
        Files.write(jsonFile2, json2.toByteArray(Charsets.UTF_8))

        main(arrayOf(tempDir.toString(), "firstname1"))

        assertEquals(
            expected = """Last F2F interaction with firstname1 lastname1 today
                         |Last F2F interaction with firstname2 lastname2 2 days ago
                         |""".trimMargin(),
            actual = programOutput.toString(),
            message = "output"
        )
    }

    @Test
    fun `update peer based on firstname and lastname`(@TempDir tempDir: Path) {
        val jsonFile1 = tempDir.resolve("lastname1_firstname1.json")
        val jsonFile2 = tempDir.resolve("lastname2_firstname2.json")

        val date1 = LocalDate.now().minusDays(1)
        val date2 = LocalDate.now().minusDays(2)

        val json1 = """{"firstName" : "firstname1", "lastInteractionF2F" : "$date1", "lastName" : "lastname1"}"""
        val json2 = """{"firstName" : "firstname2", "lastInteractionF2F" : "$date2", "lastName" : "lastname2"}"""

        Files.write(jsonFile1, json1.toByteArray(Charsets.UTF_8))
        Files.write(jsonFile2, json2.toByteArray(Charsets.UTF_8))

        main(arrayOf(tempDir.toString(), "Firstname1", "Lastname1"))

        assertEquals(
            expected = """Last F2F interaction with firstname1 lastname1 today
                         |Last F2F interaction with firstname2 lastname2 2 days ago
                         |""".trimMargin(),
            actual = programOutput.toString(),
            message = "output"
        )
    }

    @Test
    fun `update peer based on firstname, lastname and date`(@TempDir tempDir: Path) {
        val jsonFile1 = tempDir.resolve("lastname1_firstname1.json")
        val jsonFile2 = tempDir.resolve("lastname2_firstname2.json")

        val date1 = LocalDate.now().minusDays(1)
        val date2 = LocalDate.now().minusDays(2)

        val json1 = """{"firstName" : "firstname1", "lastInteractionF2F" : "$date1", "lastName" : "lastname1"}"""
        val json2 = """{"firstName" : "firstname2", "lastInteractionF2F" : "$date2", "lastName" : "lastname2"}"""

        Files.write(jsonFile1, json1.toByteArray(Charsets.UTF_8))
        Files.write(jsonFile2, json2.toByteArray(Charsets.UTF_8))

        val newDate = LocalDate.now().minusDays(3).toString()
        main(arrayOf(tempDir.toString(), "firstname1", "Lastname1", newDate))

        assertEquals(
            expected = """Last F2F interaction with firstname2 lastname2 2 days ago
                         |Last F2F interaction with firstname1 lastname1 3 days ago
                         |""".trimMargin(),
            actual = programOutput.toString(),
            message = "output"
        )
    }

    @Test
    fun `unknown first name`(@TempDir tempDir: Path) {
        val jsonFile = tempDir.resolve("lastname_firstname.json")
        val date = LocalDate.now().minusDays(1)
        val json = """{"firstName" : "firstname", "lastInteractionF2F" : "$date", "lastName" : "lastname"}"""
        Files.write(jsonFile, json.toByteArray(Charsets.UTF_8))

        main(arrayOf(tempDir.toString(), "unknown"))

        assertEquals(
            expected = """Sorry, couldn't find 'unknown'
                         |""".trimMargin(),
            actual = programOutput.toString(),
            message = "output"
        )
    }

    @Test
    fun `duplicate peer based on first name`(@TempDir tempDir: Path) {
        val jsonFile1 = tempDir.resolve("lastname1_firstname1.json")
        val jsonFile2 = tempDir.resolve("lastname2_firstname1.json")

        val date1 = LocalDate.now().minusDays(1)
        val date2 = LocalDate.now().minusDays(2)

        val json1 = """{"firstName" : "firstname1", "lastInteractionF2F" : "$date1", "lastName" : "lastname1"}"""
        val json2 = """{"firstName" : "firstname1", "lastInteractionF2F" : "$date2", "lastName" : "lastname2"}"""

        Files.write(jsonFile1, json1.toByteArray(Charsets.UTF_8))
        Files.write(jsonFile2, json2.toByteArray(Charsets.UTF_8))

        main(arrayOf(tempDir.toString(), "firstname1"))

        assertEquals(
            expected = """Multiple entries found for 'firstname1'. Please also provide last name.
                         |""".trimMargin(),
            actual = programOutput.toString(),
            message = "output"
        )
    }

    @Test
    fun `too many args will prompt for usage`(@TempDir tempDir: Path) {
        main(arrayOf("too", "many", "args", "foo", "bar"))

        assertEquals(
            expected = """usage
     |george-connect <path>                                         list all peer face-to-face interactions
     |george-connect <path> <first_name>                            log new peer face-to-face interaction for existing peer
     |george-connect <path> <first_name> <last_name>                log new peer face-to-face interaction for existing or new peer
     |george-connect <path> <first_name> <last_name> <YYYY-MM-DD>   log new peer face-to-face interaction for existing or new peer by providing custom date
     |""".trimMargin(),
            actual = programOutput.toString(),
            message = "output"
        )
    }
}