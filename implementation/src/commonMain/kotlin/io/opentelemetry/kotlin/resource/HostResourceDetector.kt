package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.init.ResourceDetectionConfigDsl
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig

/**
 * Returns a detector for operating system resource attributes of the host running the SDK.
 */
@ExperimentalApi
public fun ResourceDetectionConfigDsl.hostResourceDetector(): ResourceDetector = HostResourceDetector

@OptIn(ExperimentalApi::class, IncubatingApi::class)
internal object HostResourceDetector : ResourceDetector {

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
