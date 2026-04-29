package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.NoopBaggage

internal object NoopContext : Context {

    override fun <T> set(key: ContextKey<T>, value: T?): Context {
        return this
    }

    override fun <T> get(key: ContextKey<T>): T? {
        return null
    }

    override fun attach(): Scope = NoopScope

    override fun storeBaggage(baggage: Baggage): Context = this

    override fun extractBaggage(): Baggage = NoopBaggage

    override fun clearBaggage(): Context = this
}
