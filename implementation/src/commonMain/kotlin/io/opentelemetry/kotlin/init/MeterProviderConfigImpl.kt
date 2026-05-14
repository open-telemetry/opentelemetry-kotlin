package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.Resource

internal class MeterProviderConfigImpl(
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : MeterProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    fun generateMetricsConfig(base: Resource): MetricsConfig = MetricsConfig(
        resource = base.merge(resourceConfigImpl.generateResource()),
    )
}
