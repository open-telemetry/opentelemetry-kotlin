package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock

internal class LogExportConfigImpl(override val clock: Clock) : LogExportConfigDsl
