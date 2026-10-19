package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
data class OtlpHttpExporter(
    override val endpoint: String? = null,
    override val timeout: Long? = null,
    override val headers: Map<String, String?>? = null
) : OtlpExporter
