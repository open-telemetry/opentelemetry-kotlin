package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.context.FakeContext
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class PropagatorsEntrypointTest {

    private val propagators = createPropagators()

    @Test
    fun `each call returns a new instance`() {
        assertNotSame(createPropagators(), createPropagators())
    }

    @Test
    fun `none returns the same propagator on every call`() {
        assertSame(propagators.none(), propagators.none())
        assertSame(propagators.none(), createPropagators().none())
    }

    @Test
    fun `none declares no fields`() {
        assertTrue(propagators.none().fields().isEmpty())
    }

    @Test
    fun `none injects nothing`() {
        val carrier = mutableMapOf<String, String>()
        propagators.none().inject(FakeContext(), carrier, FakeTextMapSetter)
        assertTrue(carrier.isEmpty())
    }

    @Test
    fun `none extracts nothing`() {
        val context = FakeContext()
        val carrier = mapOf("traceparent" to "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01")
        assertSame(context, propagators.none().extract(context, carrier, FakeTextMapGetter))
    }
}
