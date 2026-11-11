package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.context.createOtelJavaCurrentContextStorage
import io.opentelemetry.kotlin.context.internalCurrentContext
import io.opentelemetry.kotlin.context.internalRootContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.toOtelJavaApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Verifies that opting in to opentelemetry-java's context storage shares a single current
 * context between the Kotlin API and the static opentelemetry-java context APIs.
 */
@OptIn(TempContextStandInApi::class)
internal class OtelJavaContextStorageTest {

    private lateinit var harness: IntegrationTestHarness
    private lateinit var errorHandler: FakeSdkErrorHandler
    private lateinit var javaApi: OtelJavaOpenTelemetry

    @BeforeTest
    fun setUp() = runTest {
        errorHandler = FakeSdkErrorHandler()
        harness = IntegrationTestHarness(testScheduler) {
            errorHandler(errorHandler)
            context { storage = createOtelJavaCurrentContextStorage() }
        }
        javaApi = harness.kotlinApi.toOtelJavaApi()
    }

    @AfterTest
    fun tearDown() {
        try {
            assertSame(OtelJavaContext.root(), OtelJavaContext.current())
        } finally {
            OtelJavaContext.root().makeCurrent()
            harness.shutdown()
        }
    }

    @Test
    fun testNoWarningWhenOptedIn() {
        assertTrue(errorHandler.apiMisuses.isEmpty())
    }

    @Test
    fun testCurrentIsRootByDefault() {
        assertSame(internalRootContext(), internalCurrentContext())
    }

    @Test
    fun testKotlinAttachVisibleToJavaStaticApis() {
        val span = harness.tracer.startSpan("kotlin_span")
        val ctx = internalCurrentContext()
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
            assertEquals(javaSpan.spanContext.spanId, internalCurrentContext().extractSpan().spanContext.spanId)
            harness.tracer.startSpan("kotlin_span").end()
        }
        javaSpan.end()

        harness.assertSpans(2) { spans ->
            val child = spans.single { it.name == "kotlin_span" }
            assertEquals(javaSpan.spanContext.spanId, child.parent.spanId)
        }
    }

    @Test
    fun testJavaDerivedFromKotlinAttach() = runTest {
        val key = createContextKey<String>("key")
        val outer = harness.tracer.startSpan("outer")
        val javaSpan = javaApi.tracerProvider.get("tracer").spanBuilder("java_span").startSpan()
        val scope = internalCurrentContext().set(key, "value").storeSpan(outer).attach()
        try {
            OtelJavaContext.current().with(javaSpan).makeCurrent().use {
                val current = internalCurrentContext()
                assertEquals("value", current.get(key))
                assertEquals(javaSpan.spanContext.spanId, current.extractSpan().spanContext.spanId)
                assertTrue(current.set(key, "inner").attach().detach())
                harness.tracer.startSpan("kotlin_span").end()
            }
        } finally {
            assertTrue(scope.detach())
        }
        javaSpan.end()
        outer.end()
        assertTrue(errorHandler.apiMisuses.isEmpty())

        harness.assertSpans(3) { spans ->
            val child = spans.single { it.name == "kotlin_span" }
            assertEquals(javaSpan.spanContext.spanId, child.parent.spanId)
        }
    }

    @Test
    fun testJavaBaggageVisibleToKotlin() {
        OtelJavaBaggage.builder().put("key", "value").build().makeCurrent().use {
            assertEquals("value", internalCurrentContext().extractBaggage().asMap()["key"]?.value)
        }
        assertSame(internalRootContext(), internalCurrentContext())
    }

    @Test
    fun testNestedScopesRestoreInOrder() {
        val outer = harness.tracer.startSpan("outer")
        val inner = harness.tracer.startSpan("inner")
        val outerScope = internalRootContext().storeSpan(outer).attach()
        val innerScope = internalCurrentContext().storeSpan(inner).attach()

        assertEquals(inner.spanContext.spanId, OtelJavaSpan.current().spanContext.spanId)
        assertTrue(innerScope.detach())
        assertEquals(outer.spanContext.spanId, OtelJavaSpan.current().spanContext.spanId)
        assertTrue(outerScope.detach())
        inner.end()
        outer.end()
    }

    @Test
    fun testOutOfOrderDetachReportsMisuse() {
        val outer = internalRootContext().storeSpan(harness.tracer.startSpan("outer"))
        val outerScope = outer.attach()
        val inner = internalCurrentContext().storeSpan(harness.tracer.startSpan("inner"))
        val innerScope = inner.attach()

        assertTrue(outerScope.detach())
        assertEquals("Scope.detach", errorHandler.apiMisuses.single().api)
        assertSame(inner, internalCurrentContext())

        assertTrue(innerScope.detach())
        assertSame(outer, internalCurrentContext())
        OtelJavaContext.root().makeCurrent()
    }
}
