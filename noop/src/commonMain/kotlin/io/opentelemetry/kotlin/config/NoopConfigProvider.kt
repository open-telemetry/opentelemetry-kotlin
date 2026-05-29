package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
internal object NoopConfigProvider : ConfigProvider {
    override val instrumentationConfig: ConfigProperties = NoopConfigProperties
}
