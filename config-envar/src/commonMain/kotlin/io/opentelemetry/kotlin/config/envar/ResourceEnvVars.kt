package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader
import io.opentelemetry.kotlin.propagation.utils.parseResourceAttributes

/**
 * Maps resource environment variables onto [ResourceBehavior].
 *
 * https://opentelemetry.io/docs/specs/otel/resource/sdk/#specifying-resource-information-via-an-environment-variable
 */
@ExperimentalApi
class ResourceEnvVars(
    private val reader: ReportingEnvVarReader,
) {

    fun toBehavior(): ResourceBehavior? {
        val attributes = reader.readStringAndTransform(RESOURCE_ATTRIBUTES, ::parseAttributes)
        val serviceName = reader.readString(SERVICE_NAME)

        return if (attributes == null && serviceName == null) {
            null
        } else {
            ResourceBehavior(
                serviceName = serviceName,
                attributes = attributes,
            )
        }
    }

    private fun parseAttributes(value: String): EnvVarReadResult<Map<String, String>?> {
        val attributes = parseResourceAttributes(value) ?: return invalidAttributes()
        return Value(attributes)
    }

    private fun invalidAttributes(): Invalid = Invalid(
        EnvVarReadWarning(
            name = RESOURCE_ATTRIBUTES,
            message = "Invalid resource attributes; ignoring",
        ),
    )

    internal companion object {
        const val RESOURCE_ATTRIBUTES = "OTEL_RESOURCE_ATTRIBUTES"
        const val SERVICE_NAME = "OTEL_SERVICE_NAME"
    }
}
