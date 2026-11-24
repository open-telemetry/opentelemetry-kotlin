package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.propagation.TextMapGetter
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapSetter
import io.opentelemetry.kotlin.propagation.createCompositePropagator
import io.opentelemetry.kotlin.propagation.createW3CBaggagePropagator
import io.opentelemetry.kotlin.propagation.createW3CTraceContextPropagator

// region propagation-custom-propagator
// Carries a tenant ID between services in an "x-tenant-id" header.
object TenantPropagator : TextMapPropagator {
    private const val HEADER = "x-tenant-id"
    private val FIELDS = listOf(HEADER)

    val TENANT_ID: ContextKey<String> = createContextKey("tenant.id")

    override fun fields(): Collection<String> = FIELDS

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        val tenant = context.get(TENANT_ID) ?: return // nothing to send
        setter.set(carrier, HEADER, tenant)
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        val tenant = getter.get(carrier, HEADER)
        if (tenant.isNullOrBlank()) {
            return context // missing or invalid: return the input context unchanged
        }
        return context.set(TENANT_ID, tenant)
    }
}

fun createSdkWithTenantPropagator(): OpenTelemetry = createOpenTelemetry {
    propagator {
        // A composite also stops an exception thrown by one propagator from reaching your app.
        createCompositePropagator(
            createW3CTraceContextPropagator(),
            createW3CBaggagePropagator(),
            TenantPropagator,
        )
    }
}
// end-region
