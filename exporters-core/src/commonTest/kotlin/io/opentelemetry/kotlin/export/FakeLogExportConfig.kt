package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.init.LogExportConfigDsl

internal class FakeLogExportConfig(
    override val clock: Clock = FakeClock()
) : LogExportConfigDsl
