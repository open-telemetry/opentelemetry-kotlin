package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.logging.model.LogRecordModel
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class LogRecordMutatorErrorHandlingTest {

    private val hostileCases = listOf(
        Case("string list attribute") { setStringListAttribute("key", HostileList()) },
        Case("any value attribute") {
            setAnyValueAttribute("key", AnyValue.ListValue(HostileList()))
        },
    )

    @Test
    fun testHostileInputDoesNotEscapeMutator() {
        hostileCases.forEach { case ->
            val errorHandler = FakeSdkErrorHandler()
            val log = createLogRecord(errorHandler)
            case.mutate(log)

            val error = errorHandler.userCodeErrors.single()
            assertEquals(SdkErrorSeverity.WARNING, error.severity, case.name)
            assertEquals("boom", error.cause.message, case.name)
            assertFalse(log.attributes.containsKey("key"), case.name)
        }
    }

    @Test
    fun testLogRecordRemainsUsableAfterHostileInput() {
        val errorHandler = FakeSdkErrorHandler()
        val log = createLogRecord(errorHandler)

        log.setStringListAttribute("hostile", HostileList())
        log.setStringAttribute("key", "value")
        log.body = "body"

        assertEquals(1, errorHandler.userCodeErrors.size)
        val data = log.toLogRecordData()
        assertEquals("value", data.attributes["key"])
        assertFalse(data.attributes.containsKey("hostile"))
        assertEquals("body", data.body)
    }

    @Test
    fun testThrowingErrorHandlerDoesNotEscapeMutator() {
        val log = createLogRecord(ThrowingSdkErrorHandler())
        log.setStringListAttribute("key", HostileList())
        assertNull(log.attributes["key"])
    }

    private fun createLogRecord(errorHandler: SdkErrorHandler) = LogRecordModel(
        resource = FakeResource(),
        instrumentationScopeInfo = FakeInstrumentationScopeInfo(),
        timestamp = 0L,
        observedTimestamp = 0L,
        body = null,
        eventName = null,
        severityText = null,
        severityNumber = SeverityNumber.INFO,
        spanContext = FakeSpanContext.VALID,
        logLimits = fakeLogLimitsConfig,
        sdkErrorHandler = errorHandler,
    )

    private class Case(
        val name: String,
        val mutate: ReadWriteLogRecord.() -> Unit,
    )

    private class HostileList<T>(
        private val delegate: List<T> = emptyList(),
    ) : List<T> by delegate {
        override val size: Int
            get() = boom()

        override fun iterator(): Iterator<T> = boom()
    }

    private class ThrowingSdkErrorHandler : SdkErrorHandler {
        override fun onError(error: SdkError): Unit = boom()
    }
}

private fun boom(): Nothing = error("boom")
