package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.propagation.utils.safely

@OptIn(ExperimentalApi::class)
internal class OtelJavaTextMapPropagatorAdapter(
    private val impl: TextMapPropagator,
) : OtelJavaTextMapPropagator {

    override fun fields(): Collection<String> = safely(emptyList()) { impl.fields() }

    override fun <C : Any> inject(
        context: OtelJavaContext,
        carrier: C?,
        setter: OtelJavaTextMapSetter<C>,
    ) {
        if (carrier == null) {
            return
        }
        safely { impl.inject(context.toOtelKotlinContext(), carrier, TextMapSetterAdapter(setter)) }
    }

    override fun <C : Any> extract(
        context: OtelJavaContext,
        carrier: C?,
        getter: OtelJavaTextMapGetter<C>,
    ): OtelJavaContext {
        if (carrier == null) {
            return context
        }
        return safely(context) {
            impl.extract(context.toOtelKotlinContext(), carrier, TextMapGetterAdapter(getter)).toOtelJavaContext()
        }
    }
}

@OptIn(ExperimentalApi::class)
internal fun TextMapPropagator.toOtelJavaTextMapPropagator(): OtelJavaTextMapPropagator =
    (this as? TextMapPropagatorAdapter)?.impl ?: OtelJavaTextMapPropagatorAdapter(this)
