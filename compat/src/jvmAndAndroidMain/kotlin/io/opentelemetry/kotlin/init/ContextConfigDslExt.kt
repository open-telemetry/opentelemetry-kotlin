package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.OtelJavaCurrentContextStorage
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.internalRootContext

/**
 * Stores the current context in opentelemetry-java's `ContextStorage`. Enable this if you use
 * [io.opentelemetry.kotlin.toOtelJavaApi], so that the Kotlin and Java APIs share one current
 * context. Replaces any [ContextConfigDsl.storage].
 */
@ExperimentalApi
@OptIn(TempContextStandInApi::class)
public fun ContextConfigDsl.useOtelJavaContextStorage() {
    storage = OtelJavaCurrentContextStorage(::internalRootContext)
}
