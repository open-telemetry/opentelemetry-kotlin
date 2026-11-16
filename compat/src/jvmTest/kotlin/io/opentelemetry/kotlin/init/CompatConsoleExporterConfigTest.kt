package io.opentelemetry.kotlin.init

import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.TracerProviderAdapter
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CompatConsoleExporterConfigTest {

    private val clock = FakeClock()
    private val idGenerator = CompatIdGenerator()
    private val noSpanLimits = SpanLimitsBehavior()
    private val noLogLimits = LogLimitsBehavior()

    @Test
    fun testSimpleBehaviorExportsOnEnd() {
        val messages = captureJul(LoggingSpanExporter::class.java.name) {
            provider(
                SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(console = ConsoleExporterBehavior()),
                    ),
                ),
            ).getTracer("test").startSpan("simple-span").end()
        }
        assertTrue(messages.any { it.contains("simple-span") })
    }

    @Test
    fun testBatchBehaviorExportsOnFlush() {
        val messages = mutableListOf<String>()
        captureJul(LoggingSpanExporter::class.java.name, messages) {
            val tracerProvider = provider(
                SpanProcessorBehavior(
                    batch = BatchSpanProcessorBehavior(
                        scheduleDelay = 60_000,
                        exporter = SpanExporterBehavior(console = ConsoleExporterBehavior()),
                    ),
                ),
            )
            tracerProvider.getTracer("test").startSpan("batch-span").end()
            assertFalse(messages.any { it.contains("batch-span") })
            runBlocking { tracerProvider.forceFlush() }
        }
        assertTrue(messages.any { it.contains("batch-span") })
    }

    @Test
    fun testExplicitSimpleWinsOverBatch() {
        val messages = captureJul(LoggingSpanExporter::class.java.name) {
            provider(
                SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(
                            console = ConsoleExporterBehavior(),
                        )
                    ),
                    batch = BatchSpanProcessorBehavior(
                        scheduleDelay = 60_000,
                        exporter = SpanExporterBehavior(
                            console = ConsoleExporterBehavior(),
                        )
                    ),
                ),
            ).getTracer("test").startSpan("simple-wins").end()
        }
        assertTrue(messages.any { it.contains("simple-wins") })
    }

    @Test
    fun testSimpleWithoutConsoleInstallsNothing() {
        val messages = captureJul(LoggingSpanExporter::class.java.name) {
            provider(SpanProcessorBehavior(simple = SimpleSpanProcessorBehavior()))
                .getTracer("test").startSpan("no-console").end()
        }
        assertFalse(messages.any { it.contains("no-console") })
    }

    @Test
    fun testDslExportTakesPrecedenceOverConsoleSpanBehavior() {
        val dslProcessor = FakeSpanProcessor()
        val provider = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            export { dslProcessor }
            applyResolvedProcessor(
                SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(console = ConsoleExporterBehavior()),
                    )
                )
            )
        }.build(clock, idGenerator, spanLimits = noSpanLimits)
        provider.getTracer("test").startSpan("dsl-span").end()
        assertEquals(1, dslProcessor.endCalls.size)
    }

    @Test
    fun testDslExportTakesPrecedenceOverConsoleLogBehavior() {
        val dslProcessor = FakeLogRecordProcessor()
        val provider = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler).apply {
            export { dslProcessor }
            applyResolvedProcessor(
                LogRecordProcessorBehavior(
                    simple = SimpleLogRecordProcessorBehavior(
                        exporter = LogRecordExporterBehavior(console = ConsoleExporterBehavior())
                    )
                )
            )
        }.build(clock, logLimits = noLogLimits)
        provider.getLogger("test").emit(body = "dsl-log")
        assertEquals(1, dslProcessor.logs.size)
    }

    private fun provider(behavior: SpanProcessorBehavior): TracerProviderAdapter =
        CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            applyResolvedProcessor(behavior)
        }.build(clock, idGenerator, spanLimits = noSpanLimits) as TracerProviderAdapter

    private fun captureJul(loggerName: String, block: () -> Unit): List<String> {
        val messages = mutableListOf<String>()
        captureJul(loggerName, messages, block)
        return messages
    }

    private fun captureJul(loggerName: String, messages: MutableList<String>, block: () -> Unit) {
        val logger = Logger.getLogger(loggerName)
        val handler = object : Handler() {
            override fun publish(record: LogRecord) {
                messages.add(record.message.orEmpty())
            }

            override fun flush() {}

            override fun close() {}
        }
        val previousLevel = logger.level
        val previousUseParent = logger.useParentHandlers
        logger.addHandler(handler)
        logger.level = Level.ALL
        logger.useParentHandlers = false
        try {
            block()
        } finally {
            logger.removeHandler(handler)
            logger.level = previousLevel
            logger.useParentHandlers = previousUseParent
        }
    }
}
