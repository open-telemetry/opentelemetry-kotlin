package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextPropagators
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.toOtelJavaApi
import io.opentelemetry.kotlin.toOtelKotlinApi
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatGuardedPropagatorTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `throwing configured propagator is reported to the error handler`() {
        val handler = FakeSdkErrorHandler()
        val propagator = createCompatOpenTelemetry {
            errorHandler(handler)
            propagator { HostilePropagator }
        }.also(sdks::track).propagator
        val ctx = rootContext()

        assertTrue(propagator.fields().isEmpty())
        propagator.inject(ctx, mutableMapOf(), FakeTextMapSetter)
        assertSame(ctx, propagator.extract(ctx, emptyMap(), FakeTextMapGetter))
        assertEquals(EXPECTED_MESSAGES, handler.userCodeErrors.map { it.message })
    }

    @Test
    fun `throwing configured propagator does not escape the Java facade`() {
        val handler = FakeSdkErrorHandler()
        val propagator = createCompatOpenTelemetry {
            errorHandler(handler)
            propagator { HostilePropagator }
        }.also(sdks::track).toOtelJavaApi().propagators.textMapPropagator
        val ctx = OtelJavaContext.root()

        assertTrue(propagator.fields().isEmpty())
        propagator.inject(ctx, mutableMapOf(), MapSetter)
        propagator.extract(ctx, emptyMap(), MapGetter)
        assertEquals(EXPECTED_MESSAGES, handler.userCodeErrors.map { it.message })
    }

    @Test
    fun `throwing Java propagator does not escape the Kotlin facade`() {
        val handler = FakeSdkErrorHandler()
        val java = OtelJavaOpenTelemetry.propagating(OtelJavaContextPropagators.create(HostileJavaPropagator))
        val propagator = java.toOtelKotlinApi(sdkErrorHandler = handler).also(sdks::track).propagator
        val ctx = rootContext()

        assertTrue(propagator.fields().isEmpty())
        propagator.inject(ctx, mutableMapOf(), FakeTextMapSetter)
        assertSame(ctx, propagator.extract(ctx, emptyMap(), FakeTextMapGetter))
        assertEquals(EXPECTED_MESSAGES, handler.userCodeErrors.map { it.message })
    }

    @Test
    fun `Java propagator round-trips through the Kotlin facade`() {
        val java = OtelJavaOpenTelemetry.propagating(OtelJavaContextPropagators.create(HostileJavaPropagator))
        val kotlin = java.toOtelKotlinApi().also(sdks::track)
        assertSame(HostileJavaPropagator, kotlin.toOtelJavaApi().propagators.textMapPropagator)
    }

    private object HostilePropagator : TextMapPropagator {
        override fun fields(): Collection<String> = error("boom")
        override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>): Unit = error("boom")
        override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = error("boom")
    }

    private object HostileJavaPropagator : OtelJavaTextMapPropagator {
        override fun fields(): Collection<String> = error("boom")
        override fun <C : Any> inject(context: OtelJavaContext, carrier: C?, setter: OtelJavaTextMapSetter<C>): Unit =
            error("boom")

        override fun <C : Any> extract(
            context: OtelJavaContext,
            carrier: C?,
            getter: OtelJavaTextMapGetter<C>,
        ): OtelJavaContext = error("boom")
    }

    private object MapSetter : OtelJavaTextMapSetter<MutableMap<String, String>> {
        override fun set(carrier: MutableMap<String, String>?, key: String, value: String) {
            carrier?.set(key, value)
        }
    }

    private object MapGetter : OtelJavaTextMapGetter<Map<String, String>> {
        override fun get(carrier: Map<String, String>?, key: String): String? = carrier?.get(key)
    }

    private companion object {
        val EXPECTED_MESSAGES = listOf(
            "TextMapPropagator.fields failed",
            "TextMapPropagator.inject failed",
            "TextMapPropagator.extract failed",
        )
    }
}
