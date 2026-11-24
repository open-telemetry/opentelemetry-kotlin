package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class SdkGuardedTextMapPropagatorTest {

    private val handler = FakeSdkErrorHandler()
    private val ctx = FakeContext()

    @Test
    fun `delegates when nothing throws`() {
        val extracted = FakeContext()
        val delegate = FakeTextMapPropagator(fields = listOf("a"), onExtract = { extracted })
        val propagator = delegate.guarded(handler)
        val carrier = mutableMapOf<String, String>()

        assertEquals(listOf("a"), propagator.fields().toList())
        propagator.inject(ctx, carrier, FakeTextMapSetter)
        assertEquals(mapOf("a" to "set-by-a"), carrier)
        assertSame(extracted, propagator.extract(ctx, emptyMap(), FakeTextMapGetter))
        assertFalse(handler.hasErrors())
    }

    @Test
    fun `throwing setter is reported`() {
        val propagator = FakeTextMapPropagator(fields = listOf("a")).guarded(handler)
        propagator.inject(ctx, Unit) { _, _, _ -> error("boom") }
        assertSingleError("TextMapPropagator.inject failed")
    }

    @Test
    fun `throwing getter returns the supplied context`() {
        val propagator = GetterCallingPropagator.guarded(handler)
        assertSame(ctx, propagator.extract(ctx, Unit, ThrowingGetter))
        assertSingleError("TextMapPropagator.extract failed")
    }

    @Test
    fun `throwing propagator is reported`() {
        val propagator = ThrowingPropagator.guarded(handler)
        assertEquals(emptyList(), propagator.fields().toList())
        propagator.inject(ctx, mutableMapOf(), FakeTextMapSetter)
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
    fun `noop and already guarded propagators are not wrapped again`() {
        assertSame(createNoopPropagator(), createNoopPropagator().guarded(handler))
        val guarded = FakeTextMapPropagator().guarded(handler)
        assertIs<SdkGuardedTextMapPropagator>(guarded)
        assertSame(guarded, guarded.guarded(handler))
    }

    private fun assertSingleError(message: String) {
        assertEquals(1, handler.errors.size)
        val error = handler.userCodeErrors.single()
        assertEquals(message, error.message)
        assertEquals("boom", error.cause.message)
    }

    private object ThrowingGetter : TextMapGetter<Unit> {
        override fun keys(carrier: Unit): Collection<String> = error("boom")
        override fun get(carrier: Unit, key: String): String? = error("boom")
        override fun getAll(carrier: Unit, key: String): List<String> = error("boom")
    }

    private object GetterCallingPropagator : TextMapPropagator {
        override fun fields(): Collection<String> = emptyList()
        override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {}
        override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
            getter.get(carrier, "key")
            return FakeContext()
        }
    }

    private object ThrowingPropagator : TextMapPropagator {
        override fun fields(): Collection<String> = error("boom")
        override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>): Unit = error("boom")
        override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = error("boom")
    }
}
