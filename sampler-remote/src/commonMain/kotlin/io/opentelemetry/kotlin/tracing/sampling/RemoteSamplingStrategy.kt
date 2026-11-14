package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

internal data class RemoteSamplingStrategy(
    val rateLimitingSampling: RateLimitingSamplingStrategy? = null,
    val probabilisticSampling: ProbabilisticSamplingStrategy? = null,
    val operationSampling: PerOperationSamplingStrategies? = null,
)

internal data class PerOperationSamplingStrategies(
    val defaultSamplingProbability: Double? = null,
    val defaultLowerBoundTracesPerSecond: Double? = null,
    val defaultUpperBoundTracesPerSecond: Double? = null,
    val perOperationStrategies: List<OperationSamplingStrategy> = emptyList(),
)

internal data class OperationSamplingStrategy(
    val operation: String,
    val probabilisticSampling: ProbabilisticSamplingStrategy? = null,
)

internal data class ProbabilisticSamplingStrategy(
    val samplingRate: Double? = null,
)

internal data class RateLimitingSamplingStrategy(
    val maxTracesPerSecond: Int? = null,
)

/**
 * Parses a Jaeger remote sampling [SamplingStrategyResponse][response] from its JSON encoding.
 *
 * The response is a union where only one strategy field is present, so field presence
 * determines which strategy applies (and not strategyType, as per the proto). A field that is absent is valid
 * data (that strategy was not chosen); a field that is present but malformed makes the whole
 * response unusable, so `null` is returned and the caller keeps its previous sampler.
 *
 * Never throws.
 *
 * [response]: https://github.com/jaegertracing/jaeger-idl/blob/main/proto/api_v2/sampling.proto
 */
internal fun parseRemoteSamplingStrategy(
    json: String,
    sdkErrorHandler: SdkErrorHandler,
): RemoteSamplingStrategy? {
    val root = try {
        Json.parseToJsonElement(json)
    } catch (e: SerializationException) {
        return malformed(sdkErrorHandler, "invalid JSON: ${e.message}")
    }
    if (root !is JsonObject) {
        return malformed(sdkErrorHandler, "root is not a JSON object")
    }
    return try {
        parseStrategy(root)
    } catch (e: MalformedStrategyResponse) {
        malformed(sdkErrorHandler, e.message ?: "malformed response")
    } catch (e: Exception) {
        malformed(sdkErrorHandler, "unexpected parsing failure: ${e.message}")
    }
}

private fun parseStrategy(root: JsonObject): RemoteSamplingStrategy =
    RemoteSamplingStrategy(
        probabilisticSampling = root.optionalObject("probabilisticSampling")?.let {
            ProbabilisticSamplingStrategy(it.requiredDouble("samplingRate"))
        },
        rateLimitingSampling = root.optionalObject("rateLimitingSampling")?.let {
            RateLimitingSamplingStrategy(it.requiredInt("maxTracesPerSecond"))
        },
        operationSampling = root.optionalObject("operationSampling")?.let { operation ->
            PerOperationSamplingStrategies(
                defaultSamplingProbability = operation.requiredDouble("defaultSamplingProbability"),
                defaultLowerBoundTracesPerSecond = operation.optionalDouble("defaultLowerBoundTracesPerSecond"),
                defaultUpperBoundTracesPerSecond = operation.optionalDouble("defaultUpperBoundTracesPerSecond"),
                perOperationStrategies = operation.requiredArray("perOperationStrategies").map { entry ->
                    val strategy = entry as? JsonObject
                        ?: throw MalformedStrategyResponse("'perOperationStrategies' contains a non-object entry")
                    OperationSamplingStrategy(
                        operation = strategy.requiredString("operation"),
                        probabilisticSampling = strategy.optionalObject("probabilisticSampling")?.let { nested ->
                            ProbabilisticSamplingStrategy(nested.requiredDouble("samplingRate"))
                        },
                    )
                },
            )
        },
    )

private fun JsonObject.requiredObject(key: String): JsonObject =
    this[key] as? JsonObject ?: throw MalformedStrategyResponse("'$key' is missing or not an object")

private fun JsonObject.optionalObject(key: String): JsonObject? =
    this[key]?.let { requiredObject(key) }

private fun JsonObject.requiredArray(key: String): JsonArray =
    this[key] as? JsonArray ?: throw MalformedStrategyResponse("'$key' is missing or not an array")

private fun JsonObject.requiredDouble(key: String): Double =
    (this[key] as? JsonPrimitive)?.doubleOrNull
        ?: throw MalformedStrategyResponse("'$key' is missing or not a number")

private fun JsonObject.optionalDouble(key: String): Double? =
    (this[key] as? JsonPrimitive)?.doubleOrNull

private fun JsonObject.requiredInt(key: String): Int =
    (this[key] as? JsonPrimitive)?.intOrNull
        ?: throw MalformedStrategyResponse("'$key' is missing or not an integer")

private fun JsonObject.requiredString(key: String): String {
    val primitive = this[key] as? JsonPrimitive
    return primitive?.takeIf { it.isString }?.content
        ?: throw MalformedStrategyResponse("'$key' is missing or not a string")
}

private class MalformedStrategyResponse(message: String) : IllegalArgumentException(message)

private fun malformed(sdkErrorHandler: SdkErrorHandler, message: String): RemoteSamplingStrategy? {
    sdkErrorHandler.reportError(
        SdkError.ApiMisuse(
            api = "parseRemoteSamplingStrategy",
            message = message,
            severity = SdkErrorSeverity.WARNING,
        )
    )
    return null
}
