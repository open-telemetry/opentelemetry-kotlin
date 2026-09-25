package io.opentelemetry.kotlin.init

import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CompatConsoleExporterConfigTest {

    private val clock = FakeClock()
    private val idGenerator = CompatIdGenerator()
    private val noSpanLimits = SpanLimitsBehavior()
    private val noLogLimits = LogLimitsBehavior()

    @Test
    fun testConsoleBehaviorInstallsSpanProcessor() {
        val messages = captureJul(LoggingSpanExporter::class.java.name) {
            val provider = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
                applyResolvedProcessor(SpanProcessorBehavior(console = ConsoleExporterBehavior()))
            }.build(clock, idGenerator, spanLimits = noSpanLimits)
            provider.getTracer("test").startSpan("console-span").end()
        }
        assertTrue(messages.any { it.contains("console-span") })
    }

    @Test
    fun testDslExportTakesPrecedenceOverConsoleSpanBehavior() {
        val dslProcessor = FakeSpanProcessor()
        val provider = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            export { dslProcessor }
            applyResolvedProcessor(SpanProcessorBehavior(console = ConsoleExporterBehavior()))
        }.build(clock, idGenerator, spanLimits = noSpanLimits)
        provider.getTracer("test").startSpan("dsl-span").end()
        assertEquals(1, dslProcessor.endCalls.size)
    }

    @Test
    fun testConsoleBehaviorInstallsLogProcessor() {
        val stdout = captureStdout {
            val provider = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler).apply {
                applyResolvedProcessor(LogRecordProcessorBehavior(console = ConsoleExporterBehavior()))
            }.build(clock, logLimits = noLogLimits)
            provider.getLogger("test").emit(body = "console-log")
        }
        assertTrue(stdout.contains("console-log"))
    }

    @Test
    fun testDslExportTakesPrecedenceOverConsoleLogBehavior() {
        val dslProcessor = FakeLogRecordProcessor()
        val provider = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler).apply {
            export { dslProcessor }
            applyResolvedProcessor(LogRecordProcessorBehavior(console = ConsoleExporterBehavior()))
        }.build(clock, logLimits = noLogLimits)
        provider.getLogger("test").emit(body = "dsl-log")
        assertEquals(1, dslProcessor.logs.size)
    }

    private fun captureJul(loggerName: String, block: () -> Unit): List<String> {
        val logger = Logger.getLogger(loggerName)
        val messages = mutableListOf<String>()
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
        return messages
    }

    private fun captureStdout(block: () -> Unit): String {
        val buffer = ByteArrayOutputStream()
        val previous = System.out
        System.setOut(PrintStream(buffer, true))
        try {
            block()
        } finally {
            System.setOut(previous)
        }
        return buffer.toString()
    }
}
