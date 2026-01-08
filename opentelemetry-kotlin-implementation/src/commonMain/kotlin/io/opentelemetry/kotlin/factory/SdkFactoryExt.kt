package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
internal fun createSdkFactory(): SdkFactory = SdkFactoryImpl()
