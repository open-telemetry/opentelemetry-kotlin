package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.config.dsl.ResourceDetectionConfigDslImpl
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.semconv.DeviceAttributes
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class, IncubatingApi::class)
internal class DeviceResourceDetectorTest {

    private val detector = DeviceResourceDetector

    @Test
    internal fun `has device name`() {
        val configuredDetector = ResourceDetectionConfigDslImpl().deviceResourceDetector()

        assertEquals("device", configuredDetector.name)
    }

    @Test
    internal fun `detects device attributes`() {
        val factory = RecordingDeviceResourceFactory()

        val resource = with(detector) {
            factory.detect(
                DeviceResourceAttributes(
                    manufacturer = "Apple",
                    modelIdentifier = "iPhone17,2",
                    modelName = "iPhone",
                )
            )
        }

        assertEquals("Apple", resource.attributes[DeviceAttributes.DEVICE_MANUFACTURER])
        assertEquals("iPhone17,2", resource.attributes[DeviceAttributes.DEVICE_MODEL_IDENTIFIER])
        assertEquals("iPhone", resource.attributes[DeviceAttributes.DEVICE_MODEL_NAME])
        assertEquals(SemconvBuildKonfig.SCHEMA_URL, resource.schemaUrl)
    }

    @Test
    internal fun `omits unavailable attributes`() {
        val factory = RecordingDeviceResourceFactory()

        val resource = with(detector) {
            factory.detect(DeviceResourceAttributes(manufacturer = "Apple"))
        }

        assertEquals(mapOf(DeviceAttributes.DEVICE_MANUFACTURER to "Apple"), resource.attributes)
    }

    @Test
    internal fun `returns empty resource when no attributes can be detected`() {
        val factory = RecordingDeviceResourceFactory()

        val resource = with(detector) {
            factory.detect(DeviceResourceAttributes())
        }

        assertSame(factory.empty, resource)
    }
}

private class RecordingDeviceResourceFactory : ResourceFactory {

    override val empty: Resource = FakeResource()

    override fun create(
        schemaUrl: String?,
        attributes: AttributesMutator.() -> Unit,
    ): Resource {
        val mutator = FakeAttributesMutator().apply(attributes)
        return FakeResource(attributes = mutator.attributes, schemaUrl = schemaUrl)
    }
}
