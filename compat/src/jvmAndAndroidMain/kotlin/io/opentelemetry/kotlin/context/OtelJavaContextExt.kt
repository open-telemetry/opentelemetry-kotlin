package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext

/**
 * Converts an opentelemetry-java context to an opentelemetry-kotlin [Context], e.g. to use a
 * context received from Java instrumentation as a span parent.
 *
 * If this Java context was derived from one built from a Kotlin context, the result is that
 * Kotlin context, keeping its Kotlin-only keys, with any span or baggage changes Java made
 * applied. Otherwise the result is derived from the root context and holds this Java context's
 * span and baggage. Either way, converting the result back to Java keeps this context's Java-only keys.
 */
@ExperimentalApi
public fun OtelJavaContext.toOtelKotlinContext(): Context = OtelJavaContextBridge.toKotlin(this)
