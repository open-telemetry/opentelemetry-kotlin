package io.opentelemetry.kotlin.integration.test.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanModel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanModelTest {

    @Test
    fun `testSpanModelEnd`() {
        val span = SpanModel(
            FakeClock(),
            null,
            "span",
            io.opentelemetry.kotlin.tracing.model.SpanKind.INTERNAL,
            0L,
            FakeInstrumentationScopeInfo(),
            FakeResource(),
            FakeSpanContext.INVALID,
            FakeSpanContext.VALID,
            SpanLimitConfig(100, 100, 100, 100, 100)
        )
        assertTrue(span.isRecording())
        span.end()
        assertFalse(span.isRecording())
    }
}
