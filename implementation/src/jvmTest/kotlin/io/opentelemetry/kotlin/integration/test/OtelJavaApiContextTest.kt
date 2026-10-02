package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.toOtelJavaApi
import io.opentelemetry.kotlin.tracing.Span
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

/**
 * Verifies that the Java API returned by toOtelJavaApi() shares implicit context with the
 * Kotlin API.
 */
internal class OtelJavaApiContextTest {

    private lateinit var harness: IntegrationTestHarness
    private lateinit var javaApi: OtelJavaOpenTelemetry

    @BeforeTest
    fun setUp() = runTest {
        harness = IntegrationTestHarness(testScheduler)
        javaApi = harness.kotlinApi.toOtelJavaApi()
    }

    @Test
    fun testJavaMakeCurrentParentsKotlinSpan() = runTest {
        val javaSpan = startJavaSpan("java_span")
        javaSpan.makeCurrent().use {
            harness.tracer.startSpan("kotlin_span").end()
        }
        javaSpan.end()

        harness.assertSpans(2) { spans ->
            val child = spans.single { it.name == "kotlin_span" }
            assertEquals(javaSpan.spanContext.spanId, child.parent.spanId)
            assertEquals(javaSpan.spanContext.traceId, child.spanContext.traceId)
        }
    }

    @Test
    fun testJavaMakeCurrentAppliesToKotlinLog() = runTest {
        val javaSpan = startJavaSpan("java_span")
        javaSpan.makeCurrent().use {
            harness.logger.emit(body = "kotlin_log")
        }
        javaSpan.end()

        harness.assertLogRecords(1) { logs ->
            assertEquals(javaSpan.spanContext.spanId, logs.single().spanContext.spanId)
            assertEquals(javaSpan.spanContext.traceId, logs.single().spanContext.traceId)
        }
    }

    @Test
    fun testJavaMakeCurrentAppliesToJavaLog() = runTest {
        val javaSpan = startJavaSpan("java_span")
        javaSpan.makeCurrent().use {
            javaApi.logsBridge.get("logger").logRecordBuilder().setBody("java_log").emit()
        }
        javaSpan.end()

        harness.assertLogRecords(1) { logs ->
            assertEquals(javaSpan.spanContext.spanId, logs.single().spanContext.spanId)
            assertEquals(javaSpan.spanContext.traceId, logs.single().spanContext.traceId)
        }
    }

    @Test
    fun testKotlinAttachParentsJavaSpan() = runTest {
        val kotlinSpan = harness.tracer.startSpan("kotlin_span")
        withAttached(kotlinSpan) {
            startJavaSpan("java_span").end()
        }
        kotlinSpan.end()

        harness.assertSpans(2) { spans ->
            val child = spans.single { it.name == "java_span" }
            assertEquals(kotlinSpan.spanContext.spanId, child.parent.spanId)
            assertEquals(kotlinSpan.spanContext.traceId, child.spanContext.traceId)
        }
    }

    @Test
    fun testSetNoParentIgnoresImplicitContext() = runTest {
        val kotlinSpan = harness.tracer.startSpan("kotlin_span")
        withAttached(kotlinSpan) {
            javaApi.tracerProvider.get("tracer").spanBuilder("java_span").setNoParent().startSpan().end()
        }
        kotlinSpan.end()

        harness.assertSpans(2) { spans ->
            val child = spans.single { it.name == "java_span" }
            assertFalse(child.parent.isValid)
        }
    }

    @Test
    fun testJavaScopeCloseRestoresKotlinContext() = runTest {
        val before = harness.kotlinApi.context.implicit()
        val javaSpan = startJavaSpan("java_span")
        javaSpan.makeCurrent().use {
            assertEquals(javaSpan.spanContext.spanId, harness.kotlinApi.context.implicit().extractSpan().spanContext.spanId)
        }
        javaSpan.end()
        assertSame(before, harness.kotlinApi.context.implicit())
    }

    private fun startJavaSpan(name: String): OtelJavaSpan =
        javaApi.tracerProvider.get("tracer").spanBuilder(name).startSpan()

    private fun withAttached(span: Span, action: () -> Unit) {
        val scope = harness.kotlinApi.context.implicit().storeSpan(span).attach()
        try {
            action()
        } finally {
            scope.detach()
        }
    }
}
