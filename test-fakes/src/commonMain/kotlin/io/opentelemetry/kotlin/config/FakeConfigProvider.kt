package io.opentelemetry.kotlin.config

class FakeConfigProvider(override var instrumentationConfig: ConfigProperties = FakeConfigProperties()) : ConfigProvider
