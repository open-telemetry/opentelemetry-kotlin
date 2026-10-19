package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.init.useOtelJavaContextStorage
import io.opentelemetry.kotlin.toOtelJavaApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Verifies that opting in to opentelemetry-java's context storage shares a single implicit
 * context between the Kotlin API and the static opentelemetry-java context APIs.
 */
internal class OtelJavaContextStorageTest {

    private lateinit var harness: IntegrationTestHarness
    private lateinit var errorHandler: FakeSdkErrorHandler
    private lateinit var javaApi: OtelJavaOpenTelemetry

    @BeforeTest
    fun setUp() = runTest {
        errorHandler = FakeSdkErrorHandler()
        harness = IntegrationTestHarness(testScheduler) {
            errorHandler(errorHandler)
            context { useOtelJavaContextStorage() }
        }
        javaApi = harness.kotlinApi.toOtelJavaApi()
    }

    @AfterTest
    fun tearDown() {
        try {
            assertSame(OtelJavaContext.root(), OtelJavaContext.current())
        } finally {
            OtelJavaContext.root().makeCurrent()
        }
    }

    @Test
    fun testNoWarningWhenOptedIn() {
        assertTrue(errorHandler.apiMisuses.isEmpty())
    }

    @Test
    fun testImplicitIsRootByDefault() {
        assertSame(harness.kotlinApi.context.root(), harness.kotlinApi.context.implicit())
    }

    @Test
    fun testKotlinAttachVisibleToJavaStaticApis() {
        val span = harness.tracer.startSpan("kotlin_span")
        val ctx = harness.kotlinApi.context.implicit()
            .storeSpan(span)
            .storeBaggage(createBaggage { put("key", "value") })
        val scope = ctx.attach()
        try {
            assertEquals(span.spanContext.spanId, OtelJavaSpan.current().spanContext.spanId)
            assertEquals("value", OtelJavaBaggage.current().getEntryValue("key"))
        } finally {
            assertTrue(scope.detach())
        }
        span.end()
        assertSame(OtelJavaContext.root(), OtelJavaContext.current())
    }

    @Test
    fun testJavaMakeCurrentParentsKotlinSpan() = runTest {
        val javaSpan = javaApi.tracerProvider.get("tracer").spanBuilder("java_span").startSpan()
        OtelJavaContext.current().with(javaSpan).makeCurrent().use {
            assertEquals(javaSpan.spanContext.spanId, harness.kotlinApi.context.implicit().extractSpan().spanContext.spanId)
            harness.tracer.startSpan("kotlin_span").end()
        }
        javaSpan.end()

        harness.assertSpans(2) { spans ->
            val child = spans.single { it.name == "kotlin_span" }
            assertEquals(javaSpan.spanContext.spanId, child.parent.spanId)
        }
    }

    @Test
    fun testJavaBaggageVisibleToKotlin() {
        OtelJavaBaggage.builder().put("key", "value").build().makeCurrent().use {
            assertEquals("value", harness.kotlinApi.context.implicit().extractBaggage().asMap()["key"]?.value)
        }
        assertSame(harness.kotlinApi.context.root(), harness.kotlinApi.context.implicit())
    }

    @Test
    fun testNestedScopesRestoreInOrder() {
        val outer = harness.tracer.startSpan("outer")
        val inner = harness.tracer.startSpan("inner")
        val outerScope = harness.kotlinApi.context.root().storeSpan(outer).attach()
        val innerScope = harness.kotlinApi.context.implicit().storeSpan(inner).attach()

        assertEquals(inner.spanContext.spanId, OtelJavaSpan.current().spanContext.spanId)
        assertTrue(innerScope.detach())
        assertEquals(outer.spanContext.spanId, OtelJavaSpan.current().spanContext.spanId)
        assertTrue(outerScope.detach())
        inner.end()
        outer.end()
    }

    @Test
    fun testOutOfOrderDetachReportsMisuse() {
        val outerScope = harness.kotlinApi.context.root().storeSpan(harness.tracer.startSpan("outer")).attach()
        val innerScope = harness.kotlinApi.context.implicit().storeSpan(harness.tracer.startSpan("inner")).attach()
        assertFalse(outerScope.detach())
        assertEquals("Scope.detach", errorHandler.apiMisuses.single().api)

        assertTrue(innerScope.detach())
        assertTrue(outerScope.detach())
    }
}
