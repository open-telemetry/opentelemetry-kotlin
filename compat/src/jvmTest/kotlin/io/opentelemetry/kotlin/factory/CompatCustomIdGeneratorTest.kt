package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class CompatCustomIdGeneratorTest {

    private val sdks = SdkTracker()

    private val traceId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    private val spanId = "bbbbbbbbbbbbbbbb"

    /** A plain Kotlin [IdGenerator] that does NOT implement [OtelJavaIdGenerator]. */
    private val custom = object : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = spanId.hexToByteArray()
        override fun generateTraceIdBytes(): ByteArray = traceId.hexToByteArray()
        override val invalidTraceId: ByteArray = "00000000000000000000000000000000".hexToByteArray()
        override val invalidSpanId: ByteArray = "0000000000000000".hexToByteArray()
    }

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `custom kotlin id generator is honored via the java adapter`() {
        val sdk = createCompatOpenTelemetry {
            idGenerator { custom }
            tracerProvider { export { FakeSpanProcessor() } }
        }.also(sdks::track)
        val span = sdk.tracerProvider.getTracer("test").startSpan("span")
        assertEquals(traceId, span.spanContext.traceId)
        assertEquals(spanId, span.spanContext.spanId)
    }
}
