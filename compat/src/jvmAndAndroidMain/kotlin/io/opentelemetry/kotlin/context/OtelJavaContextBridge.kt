package io.opentelemetry.kotlin.context

import io.opentelemetry.api.baggage.otelJavaBaggageContextKey
import io.opentelemetry.api.trace.otelJavaSpanContextKey
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.toOtelJavaBaggage
import io.opentelemetry.kotlin.baggage.toOtelKotlinBaggage
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.createNonRecordingSpan
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpan
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter

/**
 * Converts Kotlin [Context]s to and from opentelemetry-java contexts, so the two APIs can share
 * Java's current context.
 *
 * Java's context can't be iterated, so a Kotlin context is stored whole under a private Java key.
 * Its span and baggage are mirrored into Java's own keys, because Java instrumentation reads
 * those. A [ContextAdapter] unwraps to the Java context it wraps. Any other Kotlin context with
 * no Java origin is layered over Java's root.
 *
 * When Java derives a new context from a bridged one, converting it back applies any span or
 * baggage that Java changed, and records the Java context as the Kotlin context's origin. A
 * later conversion back to Java builds on that origin.
 *
 * Converting the same Java context twice returns the same Kotlin context, for as long as that
 * Kotlin context is reachable.
 */
@OptIn(ExperimentalApi::class)
internal object OtelJavaContextBridge {

    /**
     * The Kotlin context a Java context was built from, and the span and baggage that Java context
     * held when it was built.
     */
    private class Mirror(
        val kotlin: Context,
        val javaSpan: OtelJavaSpan?,
        val javaBaggage: OtelJavaBaggage?,
    )

    /**
     * The Java context a Kotlin context was converted from, and the Kotlin span and baggage it
     * held at the time.
     */
    private class Origin(
        val java: OtelJavaContext,
        val kotlinSpan: Span,
        val kotlinBaggage: Baggage,
    )

    private val KOTLIN_CONTEXT: OtelJavaContextKey<Any> = OtelJavaContextKey.named("otel-kotlin-context")
    private val JAVA_ORIGIN: ContextKey<Any> = createContextKey("otel-java-origin")

    private val cache = WeakIdentityCache<OtelJavaContext, Context>()

    /**
     * Converts [context] to a Java context. A context with no Java origin is layered over Java's
     * root or is otherwise wrapped in [OtelJavaContextAdapter].
     */
    fun toJava(context: Context, layerOverRoot: Boolean): OtelJavaContext {
        if (context is ContextAdapter) {
            return context.impl
        }
        return try {
            val origin = context.get(JAVA_ORIGIN) as? Origin
            when {
                origin == null && !layerOverRoot -> OtelJavaContextAdapter(context)
                origin != null && cache.get(origin.java) === context -> origin.java
                else -> build(context, origin)
            }
        } catch (e: Throwable) {
            report(e, "Converting a Kotlin context to opentelemetry-java failed")
            OtelJavaContextAdapter(context)
        }
    }

    private fun build(context: Context, origin: Origin?): OtelJavaContext {
        var java = origin?.java ?: OtelJavaContext.root()
        val span = context.extractSpan()
        if (origin == null || span !== origin.kotlinSpan) {
            java = java.withOrClear(otelJavaSpanContextKey, span.toJavaSpanOrNull(), OtelJavaSpan.getInvalid())
        }
        val baggage = context.extractBaggage()
        if (origin == null || baggage !== origin.kotlinBaggage) {
            java = java.withOrClear(otelJavaBaggageContextKey, baggage.toJavaBaggageOrNull(), OtelJavaBaggage.empty())
        }
        val mirror = Mirror(context, java[otelJavaSpanContextKey], java[otelJavaBaggageContextKey])
        java = java.with(KOTLIN_CONTEXT, mirror)
        cache.getOrPut(java, context)
        return java
    }

    /**
     * Converts [java] to a Kotlin context. A Java context this bridge didn't build becomes a
     * [ContextAdapter], which is cached only when [cacheAdapter] is set.
     */
    fun toKotlin(java: OtelJavaContext, cacheAdapter: Boolean): Context {
        if (java is OtelJavaContextAdapter) {
            return java.impl
        }
        val context = try {
            val mirror = java[KOTLIN_CONTEXT] as? Mirror
            if (mirror == null && !cacheAdapter) {
                return ContextAdapter(java)
            }
            cache.get(java)?.let { return it }
            when (mirror) {
                null -> ContextAdapter(java)
                else -> overlay(java, mirror)
            }
        } catch (e: Throwable) {
            report(e, "Converting an opentelemetry-java context to Kotlin failed")
            ContextAdapter(java)
        }
        return cache.getOrPut(java, context)
    }

    /**
     * Applies the span and baggage Java changed since [mirror] was built, and records [java] as
     * the origin.
     */
    private fun overlay(java: OtelJavaContext, mirror: Mirror): Context {
        var context = mirror.kotlin
        val javaSpan = java[otelJavaSpanContextKey]
        if (javaSpan !== mirror.javaSpan) {
            context = context.storeSpan(javaSpan.toKotlinSpan())
        }
        val javaBaggage = java[otelJavaBaggageContextKey]
        if (javaBaggage !== mirror.javaBaggage) {
            context = when (javaBaggage) {
                null -> context.clearBaggage()
                else -> context.storeBaggage(javaBaggage.toOtelKotlinBaggage())
            }
        }
        return context.set(JAVA_ORIGIN, Origin(java, context.extractSpan(), context.extractBaggage()))
    }

    /**
     * Sets [value], or [cleared] when [value] is null and the key is present.
     */
    private fun <V : Any> OtelJavaContext.withOrClear(
        key: OtelJavaContextKey<V>,
        value: V?,
        cleared: V,
    ): OtelJavaContext = when {
        value != null -> with(key, value)
        this[key] != null -> with(key, cleared)
        else -> this
    }

    private fun Span.toJavaSpanOrNull(): OtelJavaSpan? = when {
        spanContext.isValid -> toOtelJavaSpan()
        else -> null
    }

    private fun Baggage.toJavaBaggageOrNull(): OtelJavaBaggage? = when {
        asMap().isEmpty() -> null
        else -> toOtelJavaBaggage()
    }

    private fun OtelJavaSpan?.toKotlinSpan(): Span = when (this) {
        is OtelJavaSpanAdapter -> span
        null -> createNonRecordingSpan(createInvalidSpanContext())
        else -> createNonRecordingSpan(spanContext.toOtelKotlinSpanContext())
    }

    private fun report(e: Throwable, message: String) {
        reportError(SdkError.UserCodeError(e, message, SdkErrorSeverity.WARNING))
    }
}
