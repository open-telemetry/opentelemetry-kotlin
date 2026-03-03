package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock

internal class TraceExportConfigImpl(override val clock: Clock) : TraceExportConfigDsl
