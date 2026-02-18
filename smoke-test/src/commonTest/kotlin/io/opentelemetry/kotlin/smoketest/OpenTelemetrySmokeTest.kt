package io.opentelemetry.kotlin.smoketest

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.export.createOtlpHttpLogRecordExporter
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.createOtlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
class OpenTelemetrySmokeTest {

    private lateinit var server: FakeOtlpServer
    private lateinit var otel: OpenTelemetry

    @BeforeTest
    fun setUp() {
        server = FakeOtlpServer()
        otel = createOpenTelemetry {
            tracerProvider {
                export {
                    simpleSpanProcessor(
                        createOtlpHttpSpanExporter(
                            server.baseUrl,
                            server.mockEngine
                        )
                    )
                }
            }
            loggerProvider {
                export {
                    simpleLogRecordProcessor(
                        createOtlpHttpLogRecordExporter(
                            server.baseUrl,
                            server.mockEngine
                        )
                    )
                }
            }
        }
    }

    @AfterTest
    fun tearDown() {
        server.close()
    }

    @Test
    fun exportsSpansAndLogs() = runTest {
        val spanName = "test-span"
        val tracer = otel.tracerProvider.getTracer("test-tracer")
        val span = tracer.createSpan(spanName)
        span.end()

        val logBody = "test-log-message"
        val logger = otel.loggerProvider.getLogger("test-logger")
        logger.emit(body = logBody)

        // assert span received
        val receivedSpan = server.awaitSpan { it.name == spanName }
        assertEquals(spanName, receivedSpan.name)

        // assert log received
        val receivedLog = server.awaitLog { it.body == logBody }
        assertEquals(logBody, receivedLog.body)
    }
}
