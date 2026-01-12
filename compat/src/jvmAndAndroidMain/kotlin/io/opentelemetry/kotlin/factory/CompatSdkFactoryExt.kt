package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates a factory that constructs objects that work when the SDK is backed by the OTel Java SDK.
 */
@ExperimentalApi
internal fun createCompatSdkFactory(): SdkFactory = CompatSdkFactory()
