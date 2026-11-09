package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext

/**
 * Converts an opentelemetry-kotlin [Context] to an opentelemetry-java context, e.g. to pass it to
 * Java instrumentation.
 *
 * A context obtained from a Java context converts back onto that Java context, keeping
 * any Java-only keys. Other contexts are wrapped in a Java view of the Kotlin context.
 */
@ExperimentalApi
public fun Context.toOtelJavaContext(): OtelJavaContext = OtelJavaContextBridge.toJava(this, layerOverRoot = false)
