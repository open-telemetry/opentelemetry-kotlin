package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.NO_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.toAttributeValues
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

/**
 * Builds the attributes of a [Resource] from a raw map, without an attribute limit. An empty key
 * is not a valid attribute key, so it is dropped and reported to the process-wide error handler
 * as an [SdkError.ApiMisuse] attributed to [api]. A map holds at most one empty key, so this
 * reports at most once per resource.
 */
internal fun resourceAttributesOf(attributes: Map<String, Any>, api: String): AttributesModel {
    val hasEmptyKey = "" in attributes
    if (hasEmptyKey) {
        reportError(
            SdkError.ApiMisuse(
                api = api,
                message = "Resource attribute keys must not be empty, so an attribute with an " +
                    "empty key was dropped.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }
    val valid = when {
        hasEmptyKey -> attributes - ""
        else -> attributes
    }
    return AttributesModel(
        attributeLimit = NO_ATTRIBUTE_LIMIT,
        attrs = valid.toAttributeValues().toMutableMap(),
    )
}
