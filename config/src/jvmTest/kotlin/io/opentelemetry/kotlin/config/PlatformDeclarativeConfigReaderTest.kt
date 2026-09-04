package io.opentelemetry.kotlin.config

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class PlatformDeclarativeConfigReaderTest {

    @Test
    fun `should parse a declarative config file into the behavior it supplies`() {
        val reader = assertNotNull(platformDeclarativeConfigReader())
        val behavior = reader.read(writeConfigFile(CONFIG_FILE))
        assertEquals(64, behavior.attributeLimits?.attributeCountLimit)
        assertEquals(256, behavior.tracerProvider?.spanLimits?.attributeCountLimit)
    }

    @Test
    fun `should throw when the file does not exist`() {
        val reader = assertNotNull(platformDeclarativeConfigReader())
        assertFailsWith<Exception> { reader.read("does-not-exist.yaml") }
    }

    @Test
    fun `should throw when the file is not a valid configuration`() {
        val reader = assertNotNull(platformDeclarativeConfigReader())
        val path = writeConfigFile("file_format: [not, a, string")
        assertFailsWith<Exception> { reader.read(path) }
    }

    private fun writeConfigFile(contents: String): String {
        val file = File.createTempFile("opentelemetry-config", ".yaml")
        file.deleteOnExit()
        file.writeText(contents)
        return file.absolutePath
    }

    private companion object {
        val CONFIG_FILE = """
            file_format: "1.0"
            attribute_limits:
              attribute_count_limit: 64
            tracer_provider:
              processors: []
              limits:
                attribute_count_limit: 256
        """.trimIndent()
    }
}
