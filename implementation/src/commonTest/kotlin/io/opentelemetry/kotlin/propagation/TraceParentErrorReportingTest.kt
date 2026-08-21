package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class TraceParentErrorReportingTest {

    private val flagsFactory = TraceFlagsFactoryImpl()
    private val traceId = "0af7651916cd43dd8448eb211c80319c"
    private val spanId = "b7ad6b7169203331"
    private val traceFlags = TraceFlagsImpl(isSampled = true, isRandom = false)

    private lateinit var handler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        handler = FakeSdkErrorHandler()
    }

    @Test
    fun `create reports ApiMisuse for forbidden version`() {
        assertNull(TraceParent.create("ff", traceId, spanId, traceFlags, handler))

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("TraceParent.create", error.api)
        assertEquals(
            "version must be 2 lowercase hex characters and not ff",
            error.message,
        )
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun `create reports ApiMisuse for invalid traceId`() {
        assertNull(
            TraceParent.create(
                "00",
                traceId.replaceFirst('a', 'g'),
                spanId,
                traceFlags,
                handler,
            )
        )

        assertEquals(1, handler.apiMisuses.size)
        assertEquals("traceId must be 32 lowercase hex characters", handler.apiMisuses.single().message)
    }

    @Test
    fun `create reports ApiMisuse for invalid spanId`() {
        assertNull(
            TraceParent.create(
                "00",
                traceId,
                spanId.replaceFirst('b', 'g'),
                traceFlags,
                handler,
            )
        )

        assertEquals(1, handler.apiMisuses.size)
        assertEquals("spanId must be 16 lowercase hex characters", handler.apiMisuses.single().message)
    }

    @Test
    fun `decode reports ApiMisuse when parsed fields fail create validation`() {
        val invalidTraceId = traceId.replaceFirst('a', 'g')
        assertNull(
            TraceParent.decode("00-$invalidTraceId-$spanId-01", flagsFactory, handler),
        )

        assertEquals(1, handler.apiMisuses.size)
        assertEquals("TraceParent.create", handler.apiMisuses.single().api)
        assertEquals("traceId must be 32 lowercase hex characters", handler.apiMisuses.single().message)
    }
}
