package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import okio.FileNotFoundException
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
internal class ConfigFileReaderImplTest {

    private val fileSystem = FakeFileSystem()

    @Test
    fun readsTheFileItWasPointedAt() {
        write("config.yaml", MINIMAL_DOCUMENT)
        assertEquals(
            OpenTelemetryConfiguration(fileFormat = "1.0"),
            reader().read("config.yaml"),
        )
    }

    @Test
    fun readsAnAbsolutePath() {
        write("/etc/otel/config.yaml", MINIMAL_DOCUMENT)
        assertEquals(
            OpenTelemetryConfiguration(fileFormat = "1.0"),
            reader().read("/etc/otel/config.yaml"),
        )
    }

    @Test
    fun missingFileFails() {
        assertFailsWith<FileNotFoundException> { reader().read("does-not-exist.yaml") }
    }

    @Test
    fun emptyFileFails() {
        write("empty.yaml", "")
        assertFailsWith<MissingFieldException> { reader().read("empty.yaml") }
    }

    @Test
    fun malformedFileFails() {
        write("malformed.yaml", "file_format: \"1.0\"\n  bad indentation")
        assertFailsWith<SerializationException> { reader().read("malformed.yaml") }
    }

    @Test
    fun fileWithoutAFileFormatFails() {
        write("no-version.yaml", "disabled: false")
        assertFailsWith<MissingFieldException> { reader().read("no-version.yaml") }
    }

    private fun reader() = ConfigFileReaderImpl(lazyOf(fileSystem))

    private fun write(path: String, contents: String) {
        val target = path.toPath()
        target.parent?.let(fileSystem::createDirectories)
        fileSystem.write(target) { writeUtf8(contents) }
    }

    private companion object {
        const val MINIMAL_DOCUMENT = "file_format: \"1.0\""
    }
}
