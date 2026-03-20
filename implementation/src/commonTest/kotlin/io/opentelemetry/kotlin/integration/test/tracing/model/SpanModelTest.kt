package io.opentelemetry.kotlin.integration.test.tracing.model

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanModel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SpanModelTest {

    @Test
    fun `testSpanModelEnd`() {
        val span = SpanModel(
            FakeClock(),
            null,
            "span",
            SpanKind.INTERNAL,
            0L,
            FakeInstrumentationScopeInfo(),
            FakeResource(),
            FakeSpanContext.INVALID,
            FakeSpanContext.VALID,
            SpanLimitConfig(
                attributeCountLimit = 100,
                attributeValueLengthLimit = Int.MAX_VALUE,
                linkCountLimit = 100,
                eventCountLimit = 100,
                attributeCountPerEventLimit = 100,
                attributeCountPerLinkLimit = 100
            )
        )
        assertTrue(span.isRecording())
        span.end()
        assertFalse(span.isRecording())
    }
}
