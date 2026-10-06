package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.ContextFactory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OtelJavaLoggerAdapterTest {

    @Test
    fun `isEnabled with severity and context delegates`() {
        val impl = RecordingLogger()
        val adapter = OtelJavaLoggerAdapter(impl, CompatContextFactory())
        val ctx = OtelJavaContext.root().with(OtelJavaContextKey.named<String>("key"), "value")
        assertTrue(adapter.isEnabled(OtelJavaSeverity.WARN, ctx))

        val call = impl.enabledCalls.single()
        assertEquals(SeverityNumber.WARN, call.severityNumber)
        assertSame(ctx, call.context?.toOtelJavaContext())
        assertNull(call.eventName)
    }

    @Test
    fun `isEnabled with undefined severity maps to unknown`() {
        val impl = RecordingLogger()
        val adapter = OtelJavaLoggerAdapter(impl, CompatContextFactory())
        assertTrue(adapter.isEnabled(OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER))
        assertEquals(SeverityNumber.UNKNOWN, impl.enabledCalls.single().severityNumber)
    }

    @Test
    fun `isEnabled returns false when kotlin logger is disabled`() {
        val adapter = OtelJavaLoggerAdapter(RecordingLogger(enabledResult = false), CompatContextFactory())
        val ctx = OtelJavaContext.root()
        assertFalse(adapter.isEnabled(OtelJavaSeverity.INFO))
        assertFalse(adapter.isEnabled(OtelJavaSeverity.INFO, ctx))
    }

    @Test
    fun `isEnabled without context uses implicit context from factory`() {
        val impl = RecordingLogger()
        val ctx = OtelJavaContext.root().with(OtelJavaContextKey.named<String>("key"), "value").toOtelKotlinContext()
        val contextFactory = object : ContextFactory by CompatContextFactory() {
            override fun implicit() = ctx
        }
        val adapter = OtelJavaLoggerAdapter(impl, contextFactory)
        assertTrue(adapter.isEnabled(OtelJavaSeverity.INFO))
        assertSame(ctx, impl.enabledCalls.single().context)
    }
}
