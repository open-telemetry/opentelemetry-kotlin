package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig

/**
 * Detects operating system resource attributes for the host running the SDK.
 *
 * This detector is not enabled by default. Register it using
 * [io.opentelemetry.kotlin.init.ResourceDetectionConfigDsl.detector].
 */
@ExperimentalApi
@OptIn(IncubatingApi::class)
public class HostResourceDetector : ResourceDetector {

    override val name: String = "host"

    override fun ResourceFactory.detect(): Resource =
        detect(detectHostResourceAttributes())

    internal fun ResourceFactory.detect(attributes: HostResourceAttributes): Resource {
        if (attributes.isEmpty) {
            return empty
        }

        return create(schemaUrl = SemconvBuildKonfig.SCHEMA_URL) {
            attributes.osType?.let { setStringAttribute(OsAttributes.OS_TYPE, it) }
            attributes.osName?.let { setStringAttribute(OsAttributes.OS_NAME, it) }
            attributes.osVersion?.let { setStringAttribute(OsAttributes.OS_VERSION, it) }
        }
    }
}
