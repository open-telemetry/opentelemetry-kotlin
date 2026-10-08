package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.config.dsl.ResourceConfigDslImpl
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.Resource

internal class MeterProviderConfigImpl(
    private val sdkErrorHandler: SdkErrorHandler,
    private val resourceConfig: ResourceConfigDslImpl = ResourceConfigDslImpl()
) : MeterProviderConfigDsl, ResourceConfigDsl by resourceConfig {

    fun generateMetricsConfig(base: Resource): MetricsConfig = MetricsConfig(
        resource = base.merge(resourceConfig.toBehavior().toResource()),
        sdkErrorHandler = sdkErrorHandler,
    )
}
