package it.georgeconnect

import com.beust.klaxon.Klaxon
import georgeconnect.FileAdapter
import georgeconnect.Peer
import georgeconnect.filesFrom
import georgeconnect.createOrUpdatePeerOnFileSystem
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

class OperationsIntegrationTests {

    @Test
    fun `read jsonsFrom given path`(@TempDir tempDir: Path) {
        val jsonFile1 = tempDir.resolve("lastname1_firstname1.json")
        val jsonFile2 = tempDir.resolve("lastname2_firstname2.json")
        val json1 = """{"firstName" : "firstname1", "lastInteractionF2F" : "2021-03-03", "lastName" : "lastname1"}"""
        val json2 = """{"firstName" : "firstname2", "lastInteractionF2F" : "2021-03-04", "lastName" : "lastname2"}"""
        Files.write(jsonFile1, json1.toByteArray(Charsets.UTF_8))
        Files.write(jsonFile2, json2.toByteArray(Charsets.UTF_8))

        val jsons = filesFrom(path = tempDir.toString(), extension = "json")

        assertEquals(expected = listOf(json1, json2), actual = jsons, message = "jsons")
    }

    @Test
    fun `UpdateJsonFor given peer and path`(@TempDir tempDir: Path) {
        val jsonFile = tempDir.resolve("lastname_firstname.json")
        val json = """{"firstName" : "firstname", "lastInteractionF2F" : "2021-03-03", "lastName" : "lastname"}"""
        Files.write(jsonFile, json.toByteArray(Charsets.UTF_8))

        createOrUpdatePeerOnFileSystem(
            p = Peer("firstname", "lastname", lastInteractionF2F = "2021-03-10"),
            fileAdapter = FileAdapter(
                dataPath = tempDir.toString(),
                loadFileData = { _, _ -> ignore() },
                serializePeer = Klaxon()::toJsonString,
                deserializePeer = { ignore() },
                extension = "json"
            )
        )

        val actual = Files.readAllLines(jsonFile, Charsets.UTF_8)[0]
        assertEquals(
            expected = """{"firstName" : "firstname", "lastInteractionF2F" : "2021-03-10", "lastName" : "lastname"}""",
            actual,
            message = "peer json"
        )
    }

    private fun ignore(): Nothing = throw NotImplementedError()
}