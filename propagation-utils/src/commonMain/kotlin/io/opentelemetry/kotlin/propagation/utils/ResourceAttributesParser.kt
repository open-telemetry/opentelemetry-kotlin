package io.opentelemetry.kotlin.propagation.utils

/**
 * Parses a comma-separated list of resource attributes.
 *
 * Returns null when [value] does not conform to the `OTEL_RESOURCE_ATTRIBUTES` format.
 */
fun parseResourceAttributes(value: String): Map<String, String>? {
    val attributes = mutableMapOf<String, String>()
    for (rawEntry in value.split(ENTRY_DELIMITER)) {
        val entry = rawEntry.trim(SPACE, HORIZONTAL_TAB)
        val separatorIndex = entry.indexOf(KEY_VALUE_DELIMITER)
        if (separatorIndex <= 0 || separatorIndex != entry.lastIndexOf(KEY_VALUE_DELIMITER)) {
            return null
        }

        val key = percentDecodeBaggageValue(
            entry.substring(0, separatorIndex).trim(SPACE, HORIZONTAL_TAB),
        )
        val attributeValue = percentDecodeBaggageValue(
            entry.substring(separatorIndex + 1).trim(SPACE, HORIZONTAL_TAB),
        )
        if (key.isNullOrEmpty() || attributeValue == null) {
            return null
        }

        attributes[key] = attributeValue
    }
    return attributes
}

private const val ENTRY_DELIMITER = ','
private const val KEY_VALUE_DELIMITER = '='
private const val SPACE = ' '
private const val HORIZONTAL_TAB = '\t'
