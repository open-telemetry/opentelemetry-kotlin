
package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

/**
 * Initializes the OpenTelemetry SDK.
 */
@OptIn(ExperimentalApi::class)
fun initializeOtelSdk(): OpenTelemetry {
    val config: OpenTelemetryConfigDsl.() -> Unit = {
        tracerProvider {
            sampler {
                // Sample 10% of root spans; propagate the parent's sampling threshold for child spans.
                composite {
                    composableParentThreshold(
                        root = composableProbability(0.1)
                    )
                }
            }
            export { createSpanProcessor(AppConfig.url).also { AppConfig.spanProcessor = it } }
        }
        loggerProvider {
            export { createLogRecordProcessor(AppConfig.url).also { AppConfig.logRecordProcessor = it } }
        }
    }
    return createPlatformOpenTelemetry(AppConfig.sdkMode, config)
}
