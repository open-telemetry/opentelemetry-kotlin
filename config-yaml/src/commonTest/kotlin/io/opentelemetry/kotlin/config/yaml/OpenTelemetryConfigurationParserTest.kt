package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.config.schema.model.AttributeNameValue
import io.opentelemetry.kotlin.config.schema.model.AttributeType
import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration
import io.opentelemetry.kotlin.config.schema.model.Resource
import io.opentelemetry.kotlin.config.schema.model.SeverityNumber
import io.opentelemetry.kotlin.config.schema.model.SpanLimits
import io.opentelemetry.kotlin.config.schema.model.TracerProvider
import io.opentelemetry.kotlin.framework.loadTestFixture
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
internal class OpenTelemetryConfigurationParserTest {

    private val parser = OpenTelemetryConfigurationParser()

    @Test
    fun parsesGoldenConfigFile() {
        val expected = OpenTelemetryConfiguration(
            fileFormat = "1.0",
            disabled = false,
            logLevel = SeverityNumber.INFO,
            resource = Resource(
                attributes = listOf(
                    AttributeNameValue(
                        name = "service.name",
                        value = "unknown_service",
                        type = AttributeType.STRING,
                    ),
                ),
                schemaUrl = "https://opentelemetry.io/schemas/1.37.0",
                attributesList = "service.namespace=demo,service.version=1.0.0",
            ),
            tracerProvider = TracerProvider(
                processors = emptyList(),
                limits = SpanLimits(attributeCountLimit = 128, eventCountLimit = 64),
            ),
        )

        assertEquals(expected, parser.parse(loadTestFixture(GOLDEN_FILE)))
    }

    @Test
    fun resourceFieldsMayBeOmitted() {
        val yaml = "$MINIMAL_DOCUMENT\nresource: {}"
        assertEquals(Resource(), parser.parse(yaml).resource)
    }

    @Test
    fun parsesFromFileSystem() {
        val fileSystem = FakeFileSystem()
        val path = "config.yaml".toPath()
        fileSystem.write(path) { writeUtf8(MINIMAL_DOCUMENT) }

        assertEquals(
            OpenTelemetryConfiguration(fileFormat = "1.0"),
            parser.parse(fileSystem, path),
        )
    }

    @Test
    fun ignoresUnknownKeys() {
        val yaml = "$MINIMAL_DOCUMENT\nnot_in_the_schema: 1"

        assertEquals(OpenTelemetryConfiguration(fileFormat = "1.0"), parser.parse(yaml))
    }

    @Test
    fun emptyDocumentFails() {
        val documents = listOf("", "   \n\n  ", "# no configuration here\n", "disabled: false")

        documents.forEach { yaml ->
            val error = assertFailsWith<MissingFieldException>("parsing <$yaml> should fail") {
                parser.parse(yaml)
            }
            assertEquals(listOf("file_format"), error.missingFields)
        }
    }

    @Test
    fun nullFileFormatFails() {
        assertFailsWith<SerializationException> { parser.parse("file_format: null") }
    }

    @Test
    fun malformedYamlFails() {
        assertFailsWith<SerializationException> {
            parser.parse("file_format: \"1.0\"\n  bad indent: [unclosed")
        }
    }

    @Test
    fun documentThatIsNotAMappingFails() {
        assertFailsWith<SerializationException> { parser.parse("just a string") }
    }

    @Test
    fun unknownEnumValueFails() {
        assertFailsWith<SerializationException> {
            parser.parse("$MINIMAL_DOCUMENT\nlog_level: nonsense")
        }
    }

    @Test
    fun emptyFileFails() {
        val fileSystem = FakeFileSystem()
        val path = "empty.yaml".toPath()
        fileSystem.write(path) { writeUtf8("") }

        assertFailsWith<MissingFieldException> { parser.parse(fileSystem, path) }
    }

    @Test
    fun missingFileFails() {
        assertFailsWith<FileNotFoundException> {
            parser.parse(FakeFileSystem(), "does-not-exist.yaml".toPath())
        }
    }

    private companion object {
        const val GOLDEN_FILE = "minimal_config.yaml"
        const val MINIMAL_DOCUMENT = "file_format: \"1.0\""
    }
}
