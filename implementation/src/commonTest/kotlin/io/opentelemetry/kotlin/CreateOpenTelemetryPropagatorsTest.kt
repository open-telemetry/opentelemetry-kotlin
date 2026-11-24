package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.propagation.FakeTextMapGetter
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapGetter
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapSetter
import io.opentelemetry.kotlin.propagation.createNoopPropagator
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryPropagatorsTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `noop propagator is used when the DSL configures nothing`() {
        assertSame(createNoopPropagator(), createOpenTelemetry().also(sdks::track).propagator)
    }

    @Test
    fun `throwing configured propagator is reported to the error handler`() {
        val handler = FakeSdkErrorHandler()
        val propagator = createOpenTelemetry {
            errorHandler(handler)
            propagator { HostilePropagator }
        }.also(sdks::track).propagator
        val ctx = rootContext()

        assertTrue(propagator.fields().isEmpty())
        propagator.inject(ctx, mutableMapOf(), HostileSetter)
        assertSame(ctx, propagator.extract(ctx, emptyMap(), FakeTextMapGetter))
        assertEquals(
            listOf(
                "TextMapPropagator.fields failed",
                "TextMapPropagator.inject failed",
                "TextMapPropagator.extract failed",
            ),
            handler.userCodeErrors.map { it.message },
        )
    }

    @Test
    fun `throwing setter passed to configured propagator is reported`() {
        val handler = FakeSdkErrorHandler()
        val propagator = createOpenTelemetry {
            errorHandler(handler)
            propagator { FakeTextMapPropagator(fields = listOf("a")) }
        }.also(sdks::track).propagator

        propagator.inject(rootContext(), mutableMapOf(), HostileSetter)
        assertEquals("TextMapPropagator.inject failed", handler.userCodeErrors.single().message)
    }

    private object HostileSetter : TextMapSetter<MutableMap<String, String>> {
        override fun set(carrier: MutableMap<String, String>, key: String, value: String): Unit = error("boom")
    }

    private object HostilePropagator : TextMapPropagator {
        override fun fields(): Collection<String> = error("boom")
        override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>): Unit = error("boom")
        override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = error("boom")
    }
}
