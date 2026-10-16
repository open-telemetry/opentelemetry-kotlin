package io.opentelemetry.kotlin.config.yaml

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class InstrumentationNodeReaderTest {

    @Test
    fun returnsInstrumentationMapping() {
        val yaml =
            """
            instrumentation:
              general:
                peer:
                  service_mapping: []
            """.trimIndent()

        val result = readInstrumentationNode(yaml)
        assertEquals(setOf("general"), result?.keys)
    }

    @Test
    fun returnsNullWhenThereIsNoInstrumentationMapping() {
        assertNull(readInstrumentationNode(""))
        assertNull(readInstrumentationNode("just a string"))
        assertNull(readInstrumentationNode("other: value"))
        assertNull(readInstrumentationNode("instrumentation: enabled"))
        assertNull(readInstrumentationNode("instrumentation: [a, b]"))
    }

    @Test
    fun readsFromFileSystem() {
        val fileSystem = FakeFileSystem()
        val path = "config.yaml".toPath()
        fileSystem.write(path) {
            writeUtf8("instrumentation:\n  general: {}")
        }

        val result = readInstrumentationNode(fileSystem, path)
        assertEquals(setOf("general"), result?.keys)
    }

    @Test
    fun onlyReadsTheInstrumentationKey() {
        assertNull(readInstrumentationNode("instrumentation/development:\n  general: {}"))
    }
}
