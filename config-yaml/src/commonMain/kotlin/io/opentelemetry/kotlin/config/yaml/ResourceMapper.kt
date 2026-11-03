package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.config.schema.model.AttributeNameValue
import io.opentelemetry.kotlin.config.schema.model.AttributeType
import io.opentelemetry.kotlin.config.schema.model.Resource
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity

private const val ENTRY_DELIMITER = ','
private const val KEY_VALUE_DELIMITER = '='
private const val SPACE = ' '
private const val HORIZONTAL_TAB = '\t'

/**
 * Maps the `resource` section of a declarative config file onto the behavior it supplies.
 *
 * `attributes` entries take precedence over `attributes_list` entries for the same key.
 * Invalid `attributes_list` values cause the entire field to be discarded and an error to be
 * reported through [sdkErrorHandler]. Invalid typed entries inside `attributes[]` are skipped
 * individually and reported as errors.
 */
@OptIn(ExperimentalApi::class)
fun Resource.toBehavior(
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
): ResourceBehavior {
    // Parse attributes_list first (lower priority). Discard the whole field on parse failure.
    val listAttributes: Map<String, Any> = attributesList?.let { raw ->
        parseAttributesList(raw, sdkErrorHandler) ?: emptyMap()
    } ?: emptyMap()

    // Parse typed attributes[] entries (higher priority). Skip individual invalid entries.
    val typedAttributes: Map<String, Any> = attributes
        ?.mapNotNull { entry -> entry.toAttribute(sdkErrorHandler) }
        ?.toMap()
        ?: emptyMap()

    // Merge: typed attributes override list attributes for the same key.
    val merged: Map<String, Any>? = (listAttributes + typedAttributes).takeIf { it.isNotEmpty() }

    return ResourceBehavior(
        schemaUrl = schemaUrl,
        attributes = merged,
    )
}

/**
 * Parses the `OTEL_RESOURCE_ATTRIBUTES`-format string (comma-separated `key=value` pairs) into a
 * map of string attributes.
 *
 * Returns `null` when the entire value is malformed, in which case the caller must discard the
 * value and report an error — the spec requires this: a partly-parseable value must not be
 * partially accepted.
 *
 * Both keys and values may be percent-encoded. An empty key or a missing `=` separator makes the
 * entire value malformed.
 */
private fun parseAttributesList(
    raw: String,
    sdkErrorHandler: SdkErrorHandler,
): Map<String, Any>? {
    val result = mutableMapOf<String, String>()
    for (rawEntry in raw.split(ENTRY_DELIMITER)) {
        val entry = rawEntry.trim(SPACE, HORIZONTAL_TAB)
        if (entry.isEmpty()) {
            continue
        }
        val separatorIndex = entry.indexOf(KEY_VALUE_DELIMITER)
        if (separatorIndex <= 0) {
            sdkErrorHandler.reportAttributesListError(
                "Invalid attributes_list entry \"$entry\": missing or empty key before '='. " +
                    "The entire attributes_list value is discarded."
            )
            return null
        }
        val rawKey = entry.substring(0, separatorIndex).trim(SPACE, HORIZONTAL_TAB)
        val rawValue = entry.substring(separatorIndex + 1).trim(SPACE, HORIZONTAL_TAB)

        val key = percentDecode(rawKey)
        if (key.isNullOrEmpty()) {
            sdkErrorHandler.reportAttributesListError(
                "Invalid attributes_list entry: key \"$rawKey\" is empty after percent-decoding. " +
                    "The entire attributes_list value is discarded."
            )
            return null
        }
        val value = percentDecode(rawValue)
        if (value == null) {
            sdkErrorHandler.reportAttributesListError(
                "Invalid attributes_list entry: value for key \"$key\" contains a malformed " +
                    "percent-encoded sequence. " +
                    "The entire attributes_list value is discarded."
            )
            return null
        }
        result[key] = value
    }
    return result
}

/**
 * Converts a single typed `AttributeNameValue` entry into a `Pair<String, Any>` ready to be
 * inserted into the resource attribute map.
 *
 * Returns `null` and reports an error when:
 * - the attribute name is empty, or
 * - the attribute value is null, or
 * - the value cannot be coerced to the declared type.
 */
private fun AttributeNameValue.toAttribute(
    sdkErrorHandler: SdkErrorHandler,
): Pair<String, Any>? {
    if (name.isEmpty()) {
        sdkErrorHandler.reportAttributeEntryError(
            "Attribute entry with empty name is invalid and will be ignored."
        )
        return null
    }
    val rawValue = value ?: return null  // spec: null value means ignore the entry

    val effectiveType = type ?: AttributeType.STRING
    val coerced = coerceValue(rawValue, effectiveType)
    if (coerced == null) {
        sdkErrorHandler.reportAttributeEntryError(
            "Attribute \"$name\" declares type $effectiveType but the value " +
                "\"$rawValue\" cannot be converted to that type. The entry is ignored."
        )
        return null
    }
    return name to coerced
}

/**
 * Attempts to coerce [rawValue] (which may be a String, Number, Boolean, or List as produced by
 * the YAML deserializer) to the declared [type].
 *
 * Returns `null` when the coercion is not possible.
 */
private fun coerceValue(rawValue: Any, type: AttributeType): Any? = when (type) {
    AttributeType.STRING -> rawValue.toString()
    AttributeType.BOOL -> when (rawValue) {
        is Boolean -> rawValue
        is String -> rawValue.toBooleanStrictOrNull()
        else -> null
    }
    AttributeType.INT -> when (rawValue) {
        is Int -> rawValue.toLong()
        is Long -> rawValue
        is Number -> rawValue.toLong()
        is String -> rawValue.toLongOrNull()
        else -> null
    }
    AttributeType.DOUBLE -> when (rawValue) {
        is Double -> rawValue
        is Float -> rawValue.toDouble()
        is Number -> rawValue.toDouble()
        is String -> rawValue.toDoubleOrNull()
        else -> null
    }
    AttributeType.STRING_ARRAY -> coerceList(rawValue) { it.toString() }
    AttributeType.BOOL_ARRAY -> coerceList(rawValue) {
        when (it) {
            is Boolean -> it
            is String -> it.toBooleanStrictOrNull()
            else -> null
        }
    }
    AttributeType.INT_ARRAY -> coerceList(rawValue) {
        when (it) {
            is Int -> it.toLong()
            is Long -> it
            is Number -> it.toLong()
            is String -> it.toLongOrNull()
            else -> null
        }
    }
    AttributeType.DOUBLE_ARRAY -> coerceList(rawValue) {
        when (it) {
            is Double -> it
            is Float -> it.toDouble()
            is Number -> it.toDouble()
            is String -> it.toDoubleOrNull()
            else -> null
        }
    }
}

/**
 * Coerces [rawValue] to a `List<T>`, applying [transform] to each element.
 * Returns `null` if [rawValue] is not a list or if any element cannot be transformed.
 */
private fun <T : Any> coerceList(rawValue: Any, transform: (Any) -> T?): List<T>? {
    val list = rawValue as? List<*> ?: return null
    return list.mapNotNull { item ->
        if (item == null) return null
        transform(item) ?: return null
    }
}

/**
 * Decodes `%HH` percent-encoded sequences in [value].
 * Returns `null` if a malformed sequence is encountered.
 */
private fun percentDecode(value: String): String? {
    if (!value.contains('%')) return value
    val bytes = ByteArray(value.length)
    var pos = 0
    var i = 0
    while (i < value.length) {
        val c = value[i]
        if (c == '%') {
            if (i + 2 >= value.length) return null
            val hi = hexDigitValue(value[i + 1])
            val lo = hexDigitValue(value[i + 2])
            if (hi < 0 || lo < 0) return null
            bytes[pos++] = ((hi shl 4) or lo).toByte()
            i += 3
        } else {
            bytes[pos++] = c.code.toByte()
            i++
        }
    }
    return bytes.decodeToString(0, pos)
}

private fun hexDigitValue(c: Char): Int = when (c) {
    in '0'..'9' -> c - '0'
    in 'a'..'f' -> c - 'a' + 10
    in 'A'..'F' -> c - 'A' + 10
    else -> -1
}

private fun SdkErrorHandler.reportAttributesListError(message: String) {
    onError(
        SdkError.ApiMisuse(
            api = "resource.attributes_list",
            message = message,
            severity = SdkErrorSeverity.WARNING,
        )
    )
}

private fun SdkErrorHandler.reportAttributeEntryError(message: String) {
    onError(
        SdkError.ApiMisuse(
            api = "resource.attributes",
            message = message,
            severity = SdkErrorSeverity.WARNING,
        )
    )
}
