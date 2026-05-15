package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeTraceExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class CoreSpanExporterApiTest {

    private val config = FakeTraceExportConfig()
    private val fakeSpan = FakeReadWriteSpan()
    private val fakeContext = FakeContext()

    @Test
    fun compositeSpanProcessorEmptyReturnsNoopWithoutThrowing() = runTest {
        val emptyProcessor = config.compositeSpanProcessor().apply {
            onStart(fakeSpan, fakeContext)
            onEnding(fakeSpan)
            onEnd(fakeSpan)
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }

        assertSame(NoopSpanProcessor, emptyProcessor)
    }

    @Test
    fun compositeSpanExporterEmptyReturnsNoopWithoutThrowing() = runTest {
        val emptyExporter = config.compositeSpanExporter().apply {
            assertEquals(OperationResultCode.Failure, export(listOf(fakeSpan)))
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }
        assertSame(NoopSpanExporter, emptyExporter)
    }
}
