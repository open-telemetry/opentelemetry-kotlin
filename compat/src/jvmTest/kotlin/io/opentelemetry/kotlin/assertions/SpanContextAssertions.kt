package io.opentelemetry.kotlin.assertions

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.SpanContext
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal fun assertSpanContextsMatch(lhs: SpanContext, rhs: SpanContext) {
    assertEquals(lhs.spanId, rhs.spanId)
    assertEquals(lhs.traceId, rhs.traceId)
    assertEquals(lhs.isValid, rhs.isValid)
    assertEquals(lhs.isRemote, rhs.isRemote)

    // trace flags
    assertEquals(lhs.traceFlags.isSampled, rhs.traceFlags.isSampled)
    assertEquals(lhs.traceFlags.isRandom, rhs.traceFlags.isRandom)

    // trace flags
    assertEquals(lhs.traceState.asMap(), rhs.traceState.asMap())
}
