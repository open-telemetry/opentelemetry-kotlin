package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.tracing.createSpanContext
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class PropagatorUserCodeIsolationTest {

    private val contextFactory = ContextFactoryImpl()

    private val propagators = listOf(
        createW3CTraceContextPropagator(),
        createW3CBaggagePropagator(),
        createB3Propagator(B3Format.SINGLE),
        createB3Propagator(B3Format.MULTI),
    )

    @Test
    fun `inject swallows a throwing setter`() {
        val context = populatedContext()
        propagators.forEach { propagator ->
            propagator.inject(context, mutableMapOf(), ThrowingTextMapSetter)
        }
    }

    @Test
    fun `extract returns the input context when the getter throws`() {
        val context = contextFactory.root()
        propagators.forEach { propagator ->
            assertSame(context, propagator.extract(context, emptyMap(), ThrowingTextMapGetter))
        }
    }

    private fun populatedContext(): Context {
        val spanContext = createSpanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331") {
            isSampled = true
        }
        return contextFactory.root()
            .storeSpanContext(spanContext)
            .storeBaggage(createBaggage().set("k", "v"))
    }
}
