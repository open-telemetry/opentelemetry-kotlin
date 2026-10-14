package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RemoteSamplingStrategyTest {

    /**
     * Modern Jaeger sends the legacy `strategyType` as a string. It must be ignored in favor of
     * field presence.
     */
    @Test
    fun parsesStringStrategyType() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """{"strategyType":"PROBABILISTIC","probabilisticSampling":{"samplingRate":1}}""",
            errors,
        )

        assertEquals(1.0, assertNotNull(strategy).probabilisticSampling?.samplingRate)
        assertFalse(errors.hasErrors())
    }

    /**
     * Legacy Thrift-era responses send `strategyType` as a number. An enum-keyed parser would throw
     * or silently misread here; field-presence parsing cannot.
     */
    @Test
    fun parsesNumericStrategyType() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """{"strategyType":0,"probabilisticSampling":{"samplingRate":1}}""",
            errors,
        )

        assertEquals(1.0, assertNotNull(strategy).probabilisticSampling?.samplingRate)
        assertFalse(errors.hasErrors())
    }

    /** The OpenTelemetry Collector omits `strategyType` entirely. */
    @Test
    fun parsesAbsentStrategyType() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """{"probabilisticSampling":{"samplingRate":0.5}}""",
            errors,
        )

        assertEquals(0.5, assertNotNull(strategy).probabilisticSampling?.samplingRate)
        assertFalse(errors.hasErrors())
    }

    /**
     * Field presence wins over `strategyType`: a response labeled RATE_LIMITING but carrying a
     * probabilistic strategy resolves to the probabilistic field.
     */
    @Test
    fun fieldPresenceOverridesStrategyType() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """{"strategyType":"RATE_LIMITING","probabilisticSampling":{"samplingRate":0.25}}""",
            errors,
        )

        assertEquals(0.25, assertNotNull(strategy).probabilisticSampling?.samplingRate)
        assertNull(strategy.rateLimitingSampling)
        assertFalse(errors.hasErrors())
    }

    @Test
    fun parsesRateLimiting() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """{"strategyType":"RATE_LIMITING","rateLimitingSampling":{"maxTracesPerSecond":5}}""",
            errors,
        )

        assertEquals(5, assertNotNull(strategy).rateLimitingSampling?.maxTracesPerSecond)
        assertFalse(errors.hasErrors())
    }

    @Test
    fun parsesPerOperationSampling() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """
            {
              "operationSampling": {
                "defaultSamplingProbability": 0.1,
                "defaultLowerBoundTracesPerSecond": 2,
                "defaultUpperBoundTracesPerSecond": 3,
                "perOperationStrategies": [
                  {"operation":"/product","probabilisticSampling":{"samplingRate":1}},
                  {"operation":"/admin","probabilisticSampling":{"samplingRate":0.5}}
                ]
              }
            }
            """.trimIndent(),
            errors,
        )

        val operation = assertNotNull(assertNotNull(strategy).operationSampling)
        assertEquals(0.1, operation.defaultSamplingProbability)
        assertEquals(2.0, operation.defaultLowerBoundTracesPerSecond)
        assertEquals(3.0, operation.defaultUpperBoundTracesPerSecond)
        assertEquals(2, operation.perOperationStrategies.size)
        assertEquals("/product", operation.perOperationStrategies[0].operation)
        assertEquals(1.0, operation.perOperationStrategies[0].probabilisticSampling?.samplingRate)
        assertEquals("/admin", operation.perOperationStrategies[1].operation)
        assertEquals(0.5, operation.perOperationStrategies[1].probabilisticSampling?.samplingRate)
        assertFalse(errors.hasErrors())
    }

    @Test
    fun perOperationBoundsAreOptional() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """{"operationSampling":{"defaultSamplingProbability":0.1,"perOperationStrategies":[]}}""",
            errors,
        )

        val operation = assertNotNull(assertNotNull(strategy).operationSampling)
        assertNull(operation.defaultLowerBoundTracesPerSecond)
        assertNull(operation.defaultUpperBoundTracesPerSecond)
        assertFalse(errors.hasErrors())
    }

    /** A structurally valid response is a valid union with no member chosen. */
    @Test
    fun parsesEmptyObject() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy("{}", errors)

        assertNotNull(strategy)
        assertNull(strategy.probabilisticSampling)
        assertNull(strategy.rateLimitingSampling)
        assertNull(strategy.operationSampling)
        assertFalse(errors.hasErrors())
    }

    @Test
    fun malformedJsonReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy("{", errors)

        assertNull(strategy)
        assertTrue(errors.hasErrors())
    }

    @Test
    fun nonObjectRootReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy("[]", errors)

        assertNull(strategy)
        assertTrue(errors.hasErrors())
    }

    /** A present but broken union member is an error, unlike an absent one. */
    @Test
    fun presentButBrokenStrategyReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy("""{"probabilisticSampling":{}}""", errors)

        assertNull(strategy)
        assertEquals(1, errors.apiMisuses.size)
        assertEquals("parseRemoteSamplingStrategy", errors.apiMisuses.single().api)
    }

    @Test
    fun nonObjectStrategyReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy("""{"probabilisticSampling":"oops"}""", errors)

        assertNull(strategy)
        assertTrue(errors.hasErrors())
    }

    @Test
    fun nonStringOperationReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """
            {"operationSampling":{"defaultSamplingProbability":0.1,"perOperationStrategies":[
              {"operation":123,"probabilisticSampling":{"samplingRate":1}}
            ]}}
            """.trimIndent(),
            errors,
        )

        assertNull(strategy)
        assertTrue(errors.hasErrors())
    }

    @Test
    fun nonArrayPerOperationStrategiesReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = parseRemoteSamplingStrategy(
            """{"operationSampling":{"defaultSamplingProbability":0.1,"perOperationStrategies":{}}}""",
            errors,
        )

        assertNull(strategy)
        assertTrue(errors.hasErrors())
    }
}
