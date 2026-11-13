package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.error.userCode

/**
 * Creates a [CompatAttributesModel] populated by the user-supplied [block], which runs inside
 * [userCode] so that its failures are reported as user code errors.
 */
internal fun compatAttributes(block: (AttributesMutator.() -> Unit)?): CompatAttributesModel {
    val container = CompatAttributesModel()
    if (block != null) {
        userCode { block(container) }
    }
    return container
}

/**
 * Builds [OtelJavaAttributes] from the user-supplied [block], returning the shared empty instance
 * without allocating a container when [block] is null.
 */
internal fun compatOtelJavaAttributes(block: (AttributesMutator.() -> Unit)?): OtelJavaAttributes = when (block) {
    null -> OtelJavaAttributes.empty()
    else -> compatAttributes(block).otelJavaAttributes()
}
