package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.init.ResourceDetectionConfigDsl
import io.opentelemetry.kotlin.semconv.DeviceAttributes
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig

/**
 * Returns a detector for attributes of the device running the SDK.
 */
@ExperimentalApi
public fun ResourceDetectionConfigDsl.deviceResourceDetector(): ResourceDetector = DeviceResourceDetector

@OptIn(ExperimentalApi::class, IncubatingApi::class)
internal object DeviceResourceDetector : ResourceDetector {

    override val name: String = "device"

    override fun ResourceFactory.detect(): Resource =
        detect(detectDeviceResourceAttributes())

    internal fun ResourceFactory.detect(attributes: DeviceResourceAttributes): Resource {
        if (attributes.isEmpty) {
            return empty
        }

        return create(schemaUrl = SemconvBuildKonfig.SCHEMA_URL) {
            attributes.manufacturer?.let { setStringAttribute(DeviceAttributes.DEVICE_MANUFACTURER, it) }
            attributes.modelIdentifier?.let { setStringAttribute(DeviceAttributes.DEVICE_MODEL_IDENTIFIER, it) }
            attributes.modelName?.let { setStringAttribute(DeviceAttributes.DEVICE_MODEL_NAME, it) }
        }
    }
}
