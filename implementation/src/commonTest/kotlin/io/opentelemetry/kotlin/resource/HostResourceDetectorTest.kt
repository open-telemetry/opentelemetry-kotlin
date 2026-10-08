package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.config.dsl.ResourceDetectionConfigDslImpl
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, IncubatingApi::class)
internal class HostResourceDetectorTest {

    private val detector = HostResourceDetector

    @Test
    internal fun `has the reserved host name`() {
        val configuredDetector = ResourceDetectionConfigDslImpl().hostResourceDetector()

        assertEquals("host", configuredDetector.name)
    }

    @Test
    internal fun `detects operating system attributes`() {
        val factory = RecordingResourceFactory()

        val resource = with(detector) {
            factory.detect(
                HostResourceAttributes(
                    osType = "darwin",
                    osName = "macOS",
                    osVersion = "26.0",
                )
            )
        }

        assertEquals("darwin", resource.attributes[OsAttributes.OS_TYPE])
        assertEquals("macOS", resource.attributes[OsAttributes.OS_NAME])
        assertEquals("26.0", resource.attributes[OsAttributes.OS_VERSION])
        assertEquals(SemconvBuildKonfig.SCHEMA_URL, resource.schemaUrl)
    }

    @Test
    internal fun `omits unavailable attributes`() {
        val factory = RecordingResourceFactory()

        val resource = with(detector) {
            factory.detect(HostResourceAttributes(osType = "linux"))
        }

        assertEquals(mapOf(OsAttributes.OS_TYPE to "linux"), resource.attributes)
    }

    @Test
    internal fun `returns empty resource when no attributes can be detected`() {
        val factory = RecordingResourceFactory()

        val resource = with(detector) {
            factory.detect(HostResourceAttributes())
        }

        assertSame(factory.empty, resource)
    }

    @Test
    internal fun `detects attributes for the current host`() {
        val factory = RecordingResourceFactory()

        val resource = with(detector) { factory.detect() }

        assertTrue(resource.attributes.isNotEmpty())
    }
}

private class RecordingResourceFactory : ResourceFactory {

    override val empty: Resource = FakeResource()

    override fun create(
        schemaUrl: String?,
        attributes: AttributesMutator.() -> Unit,
    ): Resource {
        val mutator = FakeAttributesMutator().apply(attributes)
        return FakeResource(attributes = mutator.attributes, schemaUrl = schemaUrl)
    }
}
