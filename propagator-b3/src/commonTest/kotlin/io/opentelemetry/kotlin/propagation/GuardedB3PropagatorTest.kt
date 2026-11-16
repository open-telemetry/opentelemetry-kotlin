package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.tracing.createSpanContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class GuardedB3PropagatorTest {

    private val errors = mutableListOf<SdkError>()
    private val handler = object : SdkErrorHandler {
        override fun onError(error: SdkError) {
            errors += error
        }
    }

    private val propagators = B3Format.entries.map(::createB3Propagator)

    @BeforeTest
    fun setUp() {
        setProcessSdkErrorHandler(null)
        check(setProcessSdkErrorHandler(handler))
    }

    @AfterTest
    fun tearDown() {
        setProcessSdkErrorHandler(null)
    }

    @Test
    fun `throwing setter does not escape and is reported`() {
        val ctx = rootContext()
            .storeSpanContext(createSpanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331"))
        propagators.forEach { it.inject(ctx, Unit, ThrowingSetter) }
        assertEquals(propagators.size, errors.size, "errors: $errors")
        errors.forEach { assertSame(Boom, assertIs<SdkError.UserCodeError>(it).cause) }
    }

    @Test
    fun `throwing getter returns the supplied context and is reported`() {
        val ctx = rootContext()
        propagators.forEach { assertSame(ctx, it.extract(ctx, Unit, ThrowingGetter)) }
        assertEquals(propagators.size, errors.size, "errors: $errors")
        errors.forEach { assertSame(Boom, assertIs<SdkError.UserCodeError>(it).cause) }
    }

    private object Boom : IllegalStateException("boom")

    private object ThrowingSetter : TextMapSetter<Unit> {
        override fun set(carrier: Unit, key: String, value: String): Unit = throw Boom
    }

    private object ThrowingGetter : TextMapGetter<Unit> {
        override fun keys(carrier: Unit): Collection<String> = throw Boom
        override fun get(carrier: Unit, key: String): String = throw Boom
        override fun getAll(carrier: Unit, key: String): List<String> = throw Boom
    }
}
