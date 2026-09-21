package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.metrics.MeterImpl
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryAttributeLimitsTest {
    @Test
    fun `global attribute limits apply to tracer scope attributes`() {
        val spanProcessor = FakeSpanProcessor()

        val api = createOpenTelemetry {
            attributeLimits {
                attributeCountLimit = 1
                attributeValueLengthLimit = 3
            }
            tracerProvider {
                export { spanProcessor }
            }
        }

        api.tracerProvider.getTracer("test") {
            setStringAttribute("first", "abcdef")
            setStringAttribute("second", "xyz")
        }.startSpan("span").end()

        val spanTrace = spanProcessor.endCalls.single()
        assertEquals(mapOf("first" to "abc"), spanTrace.instrumentationScopeInfo.attributes)
    }

    @Test
    fun `global attribute limits apply to logger scope attributes`() {
        val logProcessor = FakeLogRecordProcessor()

        val api = createOpenTelemetry {
            attributeLimits {
                attributeCountLimit = 1
                attributeValueLengthLimit = 3
            }
            loggerProvider {
                export { logProcessor }
            }
        }

        api.loggerProvider.getLogger("test") {
            setStringAttribute("first", "abcdef")
            setStringAttribute("second", "xyz")
        }.emit("span")

        val logRecord = logProcessor.logs.single()
        assertEquals(mapOf("first" to "abc"), logRecord.instrumentationScopeInfo.attributes)
    }

    @Test
    fun `global attribute limits apply to metrics scope attributes`() {
        val api = createOpenTelemetry {
            attributeLimits {
                attributeCountLimit = 1
                attributeValueLengthLimit = 3
            }
        }

        val meter = api.meterProvider.getMeter("test") {
            setStringAttribute("first", "abcdef")
            setStringAttribute("second", "xyz")
        }

        val meterImpl = assertIs<MeterImpl>(meter)
        assertEquals(
            mapOf("first" to "abc"),
            meterImpl.instrumentationScopeInfo.attributes,
        )
    }

    @Test
    fun `span limits do not override tracer scope attribute limits`() {
        val spanProcessor = FakeSpanProcessor()

        val api = createOpenTelemetry {
            attributeLimits {
                attributeCountLimit = 1
                attributeValueLengthLimit = 3
            }
            tracerProvider {
                spanLimits {
                    attributeCountLimit = 2
                    attributeValueLengthLimit = 6
                }
                export { spanProcessor }
            }
        }

        api.tracerProvider.getTracer("test") {
            setStringAttribute("first", "abcdef")
            setStringAttribute("second", "xyz")
        }.startSpan("span").end()

        val spanTrace = spanProcessor.endCalls.single()
        assertEquals(mapOf("first" to "abc"), spanTrace.instrumentationScopeInfo.attributes)
    }

    @Test
    fun `log limits do not override logger scope attribute limits`() {
        val logProcessor = FakeLogRecordProcessor()

        val api = createOpenTelemetry {
            attributeLimits {
                attributeCountLimit = 1
                attributeValueLengthLimit = 3
            }
            loggerProvider {
                logLimits {
                    attributeCountLimit = 2
                    attributeValueLengthLimit = 6
                }
                export { logProcessor }
            }
        }

        api.loggerProvider.getLogger("test") {
            setStringAttribute("first", "abcdef")
            setStringAttribute("second", "xyz")
        }.emit("span")

        val logRecord = logProcessor.logs.single()
        assertEquals(mapOf("first" to "abc"), logRecord.instrumentationScopeInfo.attributes)
    }

    @Test
    fun `zero global attribute count limit drops scope attributes`() {
        val api = createOpenTelemetry {
            attributeLimits {
                attributeCountLimit = 0
            }
        }

        val meter = api.meterProvider.getMeter("test") {
            setStringAttribute("first", "abcdef")
        }

        val meterImpl = assertIs<MeterImpl>(meter)
        assertTrue(meterImpl.instrumentationScopeInfo.attributes.isEmpty())
    }

    @Test
    fun `global attribute limits do not apply to resource attributes`() {
        val spanProcessor = FakeSpanProcessor()

        val api = createOpenTelemetry {
            attributeLimits {
                attributeCountLimit = 1
                attributeValueLengthLimit = 3
            }
            resource {
                setStringAttribute("first", "abcdef")
                setStringAttribute("second", "xyz")
            }
            tracerProvider {
                export { spanProcessor }
            }
        }

        api.tracerProvider.getTracer("test").startSpan("span").end()

        val resourceAttributes = spanProcessor.endCalls.single().resource.attributes
        assertEquals("abcdef", resourceAttributes.getValue("first"))
        assertEquals("xyz", resourceAttributes.getValue("second"))
    }
}
