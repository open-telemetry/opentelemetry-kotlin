package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaMeter

@ExperimentalApi
internal class MeterAdapter(
    @Suppress("UnusedPrivateProperty") private val impl: OtelJavaMeter,
) : Meter
