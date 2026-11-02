package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal class LogRecordProcessorExtTest {

    private lateinit var harness: OtelKotlinHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = OtelKotlinHarness(testScheduler)
    }

    @AfterTest
    fun tearDown() = harness.shutdown()

    @Test
    fun testOnEmit() = runTest {
        val impl = FakeOtelJavaLogRecordProcessor()
        val adapter = impl.toOtelKotlinLogRecordProcessor()
        harness.config.logRecordProcessors.add(adapter)

        val logger = harness.javaApi.logsBridge.get("logger")
        val msg = "hello world"
        logger.logRecordBuilder()
            .setBody(msg)
            .emit()

        val export = impl.exports.single()
        assertEquals(msg, export.bodyValue?.asString())
    }

    @Test
    fun testFlush() = runTest {
        val impl = FakeOtelJavaLogRecordProcessor()
        val adapter = impl.toOtelKotlinLogRecordProcessor()
        adapter.forceFlush()
        assertEquals(1, impl.flushCount)
    }

    @Test
    fun testShutdown() = runTest {
        val impl = FakeOtelJavaLogRecordProcessor()
        val adapter = impl.toOtelKotlinLogRecordProcessor()
        adapter.shutdown()
        assertEquals(1, impl.shutdownCount)
    }
}
