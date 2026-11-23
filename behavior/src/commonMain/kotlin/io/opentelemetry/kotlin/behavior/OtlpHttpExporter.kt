package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
data class OtlpHttpExporter(
    override val endpoint: String? = null,
    val signalEndpoint: String? = null,
    override val timeout: Long? = null,
    override val headers: Map<String, String?>? = null
) : OtlpExporter
