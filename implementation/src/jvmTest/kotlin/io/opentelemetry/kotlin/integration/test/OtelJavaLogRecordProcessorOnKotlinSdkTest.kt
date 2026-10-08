package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordExporter
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.LoggerConfig
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.export.LogRecordExporter
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.toOtelKotlinLogRecordExporter
import io.opentelemetry.kotlin.logging.export.toOtelKotlinLogRecordProcessor
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.toOtelJavaApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class OtelJavaLogRecordProcessorOnKotlinSdkTest {

    private val stringKey = OtelJavaAttributeKey.stringKey("key")

    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = IntegrationTestHarness(testScheduler)
    }

    @Test
    fun testJavaProcessorReceivesKotlinApiLogs() = runTest {
        val processor = RecordingOtelJavaLogRecordProcessor()
        harness.config.logRecordProcessors.add(processor.toOtelKotlinLogRecordProcessor())
        emitKotlinLog()

        val log = processor.emitCalls.single()
        assertEquals("my_log", log.bodyValue?.asString())
        assertEquals(5L, log.timestampEpochNanos)
        assertEquals(10L, log.observedTimestampEpochNanos)
        assertEquals(OtelJavaSeverity.WARN, log.severity)
        assertEquals("warning", log.severityText)
        assertEquals("my_event", log.eventName)
        assertEquals("value", log.getAttribute(stringKey))
        assertEquals("value", log.attributes.get(stringKey))
        assertEquals("test_logger", log.instrumentationScopeInfo.name)
        assertEquals("my_log", log.toLogRecordData().bodyValue?.asString())
    }

    @Test
    fun testJavaProcessorReceivesJavaApiLogs() = runTest {
        val processor = RecordingOtelJavaLogRecordProcessor()
        harness.config.logRecordProcessors.add(processor.toOtelKotlinLogRecordProcessor())
        harness.kotlinApi.toOtelJavaApi().logsBridge.get("logger")
            .logRecordBuilder()
            .setBody("my_log")
            .setAttribute(stringKey, "value")
            .setSeverity(OtelJavaSeverity.DEBUG2)
            .emit()
        val log = processor.emitCalls.single()
        assertEquals("my_log", log.bodyValue?.asString())
        assertEquals("value", log.getAttribute(stringKey))
        assertEquals(OtelJavaSeverity.DEBUG2, log.severity)
        assertEquals("logger", log.instrumentationScopeInfo.name)
    }

    @Test
    fun testJavaApiExceptionIsExported() = runTest {
        val exception = IllegalStateException("boom")
        harness.kotlinApi.toOtelJavaApi().logsBridge.get("logger")
            .logRecordBuilder()
            .setException(exception)
            .emit()
        val attrs = harness.exportedLog().attributes
        assertEquals(IllegalStateException::class.qualifiedName, attrs["exception.type"])
        assertEquals("boom", attrs["exception.message"])
        assertTrue((attrs["exception.stacktrace"] as String).contains("boom"))
    }

    @Test
    fun testJavaApiIsEnabledHonoursLoggerConfig() = runTest {
        harness.config.loggerProvider = {
            loggerConfigurator { scope ->
                object : LoggerConfig {
                    override val enabled = scope.name != "disabled"
                    override val minimumSeverity = SeverityNumber.WARN
                }
            }
        }
        val logsBridge = harness.kotlinApi.toOtelJavaApi().logsBridge
        val enabled = logsBridge.get("enabled")
        val disabled = logsBridge.get("disabled")

        assertTrue(enabled.isEnabled(OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER))
        assertTrue(enabled.isEnabled(OtelJavaSeverity.WARN))
        assertTrue(enabled.isEnabled(OtelJavaSeverity.ERROR, OtelJavaContext.root()))
        assertFalse(enabled.isEnabled(OtelJavaSeverity.INFO))
        assertFalse(enabled.isEnabled(OtelJavaSeverity.INFO, OtelJavaContext.root()))
        assertFalse(disabled.isEnabled(OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER))
        assertFalse(disabled.isEnabled(OtelJavaSeverity.ERROR))
    }

    @Test
    fun testJavaProcessorMutationsAreExported() = runTest {
        val processor = RecordingOtelJavaLogRecordProcessor { log ->
            log.setAttribute(OtelJavaAttributeKey.stringKey("added"), "yes")
            log.setAttribute(OtelJavaAttributeKey.longKey("count"), 2L)
        }
        harness.config.logRecordProcessors.add(processor.toOtelKotlinLogRecordProcessor())
        emitKotlinLog()
        val exported = harness.exportedLog()
        assertEquals(mapOf("key" to "value", "added" to "yes", "count" to 2L), exported.attributes)
    }

    @Test
    fun testJavaExporterReceivesLogsFromKotlinProcessor() = runTest {
        val exporter = RecordingOtelJavaLogRecordExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        harness.config.logRecordProcessors.add(
            ExportingLogRecordProcessor(exporter.toOtelKotlinLogRecordExporter(), scope)
        )
        emitKotlinLog()
        assertJavaLogRecordDataMatches(exporter.exports.single())
    }

    private fun emitKotlinLog() {
        harness.logger.emit(
            body = "my_log",
            eventName = "my_event",
            timestamp = 5,
            observedTimestamp = 10,
            severityNumber = SeverityNumber.WARN,
            severityText = "warning",
        ) {
            setStringAttribute("key", "value")
        }
    }

    private fun assertJavaLogRecordDataMatches(observed: OtelJavaLogRecordData) {
        val expected = harness.exportedLog()
        assertEquals("my_log", observed.bodyValue?.asString())
        assertEquals(expected.timestamp, observed.timestampEpochNanos)
        assertEquals(expected.observedTimestamp, observed.observedTimestampEpochNanos)
        assertEquals(OtelJavaSeverity.WARN, observed.severity)
        assertEquals("warning", observed.severityText)
        assertEquals("my_event", observed.eventName)
        assertEquals("value", observed.attributes.get(stringKey))
        assertEquals("test_logger", observed.instrumentationScopeInfo.name)
        assertEquals(expected.spanContext.traceId, observed.spanContext.traceId)
    }

    @Test
    fun testJavaProcessorReadsImplicitKotlinSpanAndBaggage() = runTest {
        val processor = RecordingOtelJavaLogRecordProcessor()
        harness.config.logRecordProcessors.add(processor.toOtelKotlinLogRecordProcessor())
        val span = harness.tracer.startSpan("span")
        val ctx = harness.kotlinApi.context.implicit()
            .storeSpan(span)
            .storeBaggage(createBaggage { put("key", "value") })
        val scope = ctx.attach()
        try {
            harness.logger.emit(body = "my_log")
        } finally {
            scope.detach()
        }
        span.end()

        val context = processor.emitContexts.single()
        assertEquals(span.spanContext.spanId, OtelJavaSpan.fromContext(context).spanContext.spanId)
        assertEquals("value", OtelJavaBaggage.fromContext(context).getEntryValue("key"))
    }

    private fun IntegrationTestHarness.exportedLog(): LogRecordData {
        var log: LogRecordData? = null
        assertLogRecords(1) { log = it.single() }
        return checkNotNull(log)
    }

    private class RecordingOtelJavaLogRecordProcessor(
        private val emitAction: (OtelJavaReadWriteLogRecord) -> Unit = {},
    ) : OtelJavaLogRecordProcessor {

        val emitCalls = mutableListOf<OtelJavaReadWriteLogRecord>()
        val emitContexts = mutableListOf<OtelJavaContext>()

        override fun onEmit(context: OtelJavaContext, logRecord: OtelJavaReadWriteLogRecord) {
            emitCalls += logRecord
            emitContexts += context
            emitAction(logRecord)
        }
    }

    private class RecordingOtelJavaLogRecordExporter : OtelJavaLogRecordExporter {
        val exports = mutableListOf<OtelJavaLogRecordData>()

        override fun export(logs: MutableCollection<OtelJavaLogRecordData>): OtelJavaCompletableResultCode {
            exports += logs
            return OtelJavaCompletableResultCode.ofSuccess()
        }

        override fun flush(): OtelJavaCompletableResultCode = OtelJavaCompletableResultCode.ofSuccess()
        override fun shutdown(): OtelJavaCompletableResultCode = OtelJavaCompletableResultCode.ofSuccess()
    }

    private class ExportingLogRecordProcessor(
        private val exporter: LogRecordExporter,
        private val scope: CoroutineScope,
    ) : LogRecordProcessor {
        override fun onEmit(log: ReadWriteLogRecord, context: Context) {
            val data = log.toLogRecordData()
            scope.launch { exporter.export(listOf(data)) }
        }

        override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
        override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
    }
}
