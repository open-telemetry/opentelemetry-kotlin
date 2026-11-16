package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal object NoopConfigProvider : ConfigProvider {
    override val instrumentationConfig: ConfigProperties = NoopConfigProperties
}
