package io.opentelemetry.kotlin.context
internal object NoopContext : Context {

    override fun <T> set(key: ContextKey<T>, value: T?): Context {
        return this
    }

    override fun <T> get(key: ContextKey<T>): T? {
        return null
    }

    override fun attach(): Scope = NoopScope
}
