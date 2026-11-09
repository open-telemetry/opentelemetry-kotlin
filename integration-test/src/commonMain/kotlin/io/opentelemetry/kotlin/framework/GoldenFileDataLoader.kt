package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.resource.SDK_DEFAULT_SERVICE_NAME
import kotlinx.serialization.json.Json

/**
 * Placeholder in golden files for the SDK's default `service.name`, which contains the process
 * executable name and so differs per platform
 */
internal const val SDK_DEFAULT_SERVICE_NAME_PLACEHOLDER = "SDK_DEFAULT_SERVICE_NAME"

internal inline fun <reified T> compareGoldenFile(
    observed: List<T>,
    goldenFileName: String
) {
    val fixture = loadTestFixture(goldenFileName).replace(
        Json.encodeToString(SDK_DEFAULT_SERVICE_NAME_PLACEHOLDER),
        Json.encodeToString(SDK_DEFAULT_SERVICE_NAME),
    )
    val expected = Json.decodeFromString<List<T>>(fixture)
    if (expected != observed) {
        error(
            "Observed data does not match expected data. Observed=${
                Json.encodeToString(observed)
            }"
        )
    }
}
