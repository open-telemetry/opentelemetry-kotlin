package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader
import io.opentelemetry.kotlin.propagation.utils.percentDecodeBaggageValue

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
        val attributes = mutableMapOf<String, String>()
        for (rawEntry in value.split(ENTRY_DELIMITER)) {
            val entry = rawEntry.trim(SPACE, HORIZONTAL_TAB)
            val separatorIndex = entry.indexOf(KEY_VALUE_DELIMITER)
            if (separatorIndex <= 0 || separatorIndex != entry.lastIndexOf(KEY_VALUE_DELIMITER)) {
                return invalidAttributes()
            }

            val key = percentDecodeBaggageValue(
                entry.substring(0, separatorIndex).trim(SPACE, HORIZONTAL_TAB),
            )
            val attributeValue = percentDecodeBaggageValue(
                entry.substring(separatorIndex + 1).trim(SPACE, HORIZONTAL_TAB),
            )
            if (key.isNullOrEmpty() || attributeValue == null) {
                return invalidAttributes()
            }

            attributes[key] = attributeValue
        }
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

        private const val ENTRY_DELIMITER = ','
        private const val KEY_VALUE_DELIMITER = '='
        private const val SPACE = ' '
        private const val HORIZONTAL_TAB = '\t'
    }
}
