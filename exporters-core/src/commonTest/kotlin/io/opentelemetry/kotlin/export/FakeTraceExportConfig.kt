package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.init.TraceExportConfigDsl

internal class FakeTraceExportConfig(
    override val clock: Clock = FakeClock(),
    override val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) : TraceExportConfigDsl
