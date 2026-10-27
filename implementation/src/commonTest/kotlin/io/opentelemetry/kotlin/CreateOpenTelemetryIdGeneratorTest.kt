package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryIdGeneratorTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `default id generator is used when none is configured`() {
        val api = createOpenTelemetry().also(sdks::track) as OpenTelemetrySdk
        assertIs<IdGeneratorImpl>(api.idGenerator)
    }

    @Test
    fun `custom id generator supplied via the DSL is used for spans`() {
        val custom = FakeIdGenerator()
        val api = createOpenTelemetry {
            idGenerator { custom }
            tracerProvider { export { FakeSpanProcessor() } }
        }.also(sdks::track) as OpenTelemetrySdk

        assertSame(custom, api.idGenerator)

        val span = api.tracerProvider.getTracer("test").startSpan("span")
        assertEquals(custom.generateTraceIdBytes().toHexString(), span.spanContext.traceId)
        assertEquals(custom.generateSpanIdBytes().toHexString(), span.spanContext.spanId)
    }
}
