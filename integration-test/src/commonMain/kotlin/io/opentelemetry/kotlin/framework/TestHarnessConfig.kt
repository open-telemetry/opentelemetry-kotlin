package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.init.LogLimitsConfigDsl
import io.opentelemetry.kotlin.init.SpanLimitsConfigDsl
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

data class TestHarnessConfig(
    var schemaUrl: String? = null,
    var attributes: (AttributesMutator.() -> Unit)? = null,
    val spanProcessors: MutableList<SpanProcessor> = mutableListOf(),
    val logRecordProcessors: MutableList<LogRecordProcessor> = mutableListOf(),
    var spanLimits: SpanLimitsConfigDsl.() -> Unit = {},
    var logLimits: LogLimitsConfigDsl.() -> Unit = {},
)
