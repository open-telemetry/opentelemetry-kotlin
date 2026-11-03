package io.opentelemetry.kotlin.config.yaml

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @Test
    fun parsesIntegerScalar() {
        assertEquals(42, parseMapping("n: 42")["n"])
    }

    @Test
    fun parsesFloatScalar() {
        assertEquals(42.5, parseMapping("d: 42.5")["d"])
    }

    @Test
    fun parsesBooleanScalar() {
        assertEquals(true, parseMapping("b: true")["b"])
    }

    @Test
    fun parsesExplicitNullAsPresentKey() {
        val result = parseMapping("nil: null")
        assertTrue(result.containsKey("nil"))
        assertNull(result["nil"])
    }

    @Test
    fun parsesSequenceOfScalars() {
        assertEquals(listOf(1, 2, 3), parseMapping("list: [1, 2, 3]")["list"])
    }

    @Test
    fun parsesSequenceOfMappings() {
        val yaml =
            """
            list:
              - a: 1
              - a: 2
            """.trimIndent()
        assertEquals(listOf(mapOf("a" to 1), mapOf("a" to 2)), parseMapping(yaml)["list"])
    }

    private fun parseMapping(yaml: String): Map<*, *> = parser.parse(yaml) as Map<*, *>
}
