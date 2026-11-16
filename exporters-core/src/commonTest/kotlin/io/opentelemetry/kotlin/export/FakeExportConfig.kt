package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.init.ExportConfigDsl

internal class FakeExportConfig(
    override val clock: Clock = FakeClock(),
    override val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) : ExportConfigDsl
