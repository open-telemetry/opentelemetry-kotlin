
package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import io.opentelemetry.kotlin.tracing.model.SpanAdapter

/**
 * Spans created by this implementation wrap an opentelemetry-java span, so that is returned
 * directly. Any other implementation is wrapped so that its span context still survives.
 */
internal fun Span.asOtelJavaSpan(): OtelJavaSpan = (this as? SpanAdapter)?.impl ?: OtelJavaSpanAdapter(this)
