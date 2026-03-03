package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.init.TraceExportConfigDsl

internal class FakeTraceExportConfig(
    override val clock: Clock = FakeClock()
) : TraceExportConfigDsl
