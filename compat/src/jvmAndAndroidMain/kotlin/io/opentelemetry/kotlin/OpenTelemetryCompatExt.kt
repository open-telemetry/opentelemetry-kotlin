package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaContextPropagators
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.context.ContextStorageOwner
import io.opentelemetry.kotlin.context.OtelJavaCurrentContextStorage
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.logging.OtelJavaLoggerProviderAdapter
import io.opentelemetry.kotlin.metrics.OtelJavaMeterProviderAdapter
import io.opentelemetry.kotlin.propagation.toOtelJavaTextMapPropagator
import io.opentelemetry.kotlin.tracing.OtelJavaTracerProviderAdapter
import java.util.Collections
import java.util.WeakHashMap

/**
 * Constructs an [OtelJavaOpenTelemetry] instance that makes the Kotlin implementation conform
 * to the opentelemetry-java API.
 *
 * End-users should generally not use this function and should call [createCompatOpenTelemetry]
 * or [toOtelKotlinApi] instead.
 *
 * Instances created by [createCompatOpenTelemetry] or [toOtelKotlinApi] share opentelemetry-java's
 * ContextStorage, so the Kotlin and Java APIs share one current context.
 *
 * If the receiver was created by `implementation`, opt in to
 * `context { storage = createOtelJavaCurrentContextStorage() }` when creating the SDK so that the
 * Kotlin and Java APIs share one current context. Otherwise, avoid reading or setting the
 * opentelemetry-java current context directly through `Context.current()`, `Context.makeCurrent()`,
 * `Span.current()`, `Baggage.current()`, etc., as these use a separate store and trace context
 * will not propagate correctly. See the compat README for details.
 */
@ExperimentalApi
public fun OpenTelemetry.toOtelJavaApi(): OtelJavaOpenTelemetry {
    if (this == NoopOpenTelemetry) {
        return OtelJavaOpenTelemetry.noop()
    }
    warnIfNotUsingOtelJavaContextStorage()
    return OtelJavaOpenTelemetrySdk(
        OtelJavaTracerProviderAdapter(tracerProvider),
        OtelJavaLoggerProviderAdapter(loggerProvider),
        OtelJavaMeterProviderAdapter(meterProvider),
        OtelJavaContextPropagators.create(propagator.toOtelJavaTextMapPropagator()),
    )
}

private val warnedOwners: MutableSet<ContextStorageOwner> =
    Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))

private fun OpenTelemetry.warnIfNotUsingOtelJavaContextStorage() {
    val owner = this as? ContextStorageOwner ?: return
    // only warn about a storage this SDK installed
    val storage = owner.installedStorage ?: return
    if (storage is OtelJavaCurrentContextStorage || !warnedOwners.add(owner)) {
        return
    }
    owner.sdkErrorHandler.reportError(
        SdkError.ApiMisuse(
            api = "toOtelJavaApi",
            message = "Current context is not shared with opentelemetry-java. " +
                "Set storage = createOtelJavaCurrentContextStorage() in the context {} config when creating the SDK.",
            severity = SdkErrorSeverity.WARNING,
        )
    )
}
