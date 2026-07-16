package io.opentelemetry.kotlin.config

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class YamlConfigParserTest {

    private val parser = YamlConfigParser()

    @Test
    fun emptyStringReturnsNull() {
        assertNull(parser.parse(""))
    }

    @Test
    fun parsesScalarMapping() {
        val result = parser.parse("key: value")
        assertEquals(mapOf("key" to "value"), result)
    }

    @Test
    fun parsesNestedMapping() {
        val yaml =
            """
            parent:
              child: value
            """.trimIndent()
        val result = parser.parse(yaml)
        assertEquals(mapOf("parent" to mapOf("child" to "value")), result)
    }

    @Test
    fun emptyFileReturnsNull() {
        val fileSystem = FakeFileSystem()
        val path = "config.yaml".toPath()
        fileSystem.write(path) { }

        assertNull(parser.parse(fileSystem, path))
    }

    @Test
    fun parsesFileContents() {
        val fileSystem = FakeFileSystem()
        val path = "config.yaml".toPath()
        fileSystem.write(path) {
            writeUtf8("key: value")
        }

        assertEquals(mapOf("key" to "value"), parser.parse(fileSystem, path))
    }
}
