package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.tracing.SpanKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class RemoteSamplingStrategyTranslationTest {

    private val contextFactory = ContextFactoryImpl(SpanFactoryImpl(DefaultSpanContextFactory))

    private fun Sampler.sample(name: String): SamplingResult.Decision =
        shouldSample(
            context = contextFactory.root(),
            traceIdBytes = "000000000000000000ffffffffffffff".hexToByteArray(),
            name = name,
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        ).decision

    @Test
    fun probabilisticProducesSampler() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            probabilisticSampling = ProbabilisticSamplingStrategy(0.5),
        )

        val sampler = assertNotNull(strategy.toSampler(errors))

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, sampler.sample("span"))
        assertFalse(errors.hasErrors())
    }

    /** A rate of zero is a legitimate "never sample" strategy, not an invalid one. */
    @Test
    fun zeroRateProducesAlwaysOff() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            probabilisticSampling = ProbabilisticSamplingStrategy(0.0),
        )

        val sampler = assertNotNull(strategy.toSampler(errors))

        assertEquals(SamplingResult.Decision.DROP, sampler.sample("span"))
        assertFalse(errors.hasErrors())
    }

    @Test
    fun rateAboveOneReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            probabilisticSampling = ProbabilisticSamplingStrategy(1.5),
        )

        assertNull(strategy.toSampler(errors))
        assertEquals(1, errors.apiMisuses.size)
    }

    @Test
    fun negativeRateReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            probabilisticSampling = ProbabilisticSamplingStrategy(-0.5),
        )

        assertNull(strategy.toSampler(errors))
        assertTrue(errors.hasErrors())
    }

    @Test
    fun rateBelowMinimumReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            probabilisticSampling = ProbabilisticSamplingStrategy(1e-30),
        )

        assertNull(strategy.toSampler(errors))
        assertTrue(errors.hasErrors())
    }

    @Test
    fun rateLimitingReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            rateLimitingSampling = RateLimitingSamplingStrategy(5),
        )

        assertNull(strategy.toSampler(errors))
        assertEquals(1, errors.apiMisuses.size)
    }

    @Test
    fun emptyStrategyReturnsNull() {
        val errors = FakeSdkErrorHandler()

        assertNull(RemoteSamplingStrategy().toSampler(errors))
        assertFalse(errors.hasErrors())
    }

    @Test
    fun operationSamplingDispatchesByName() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            operationSampling = PerOperationSamplingStrategies(
                defaultSamplingProbability = 0.0,
                perOperationStrategies = listOf(
                    OperationSamplingStrategy("/sampled", ProbabilisticSamplingStrategy(1.0)),
                ),
            ),
        )

        val sampler = assertNotNull(strategy.toSampler(errors))

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, sampler.sample("/sampled"))
        assertEquals(SamplingResult.Decision.DROP, sampler.sample("/other"))
        assertFalse(errors.hasErrors())
    }

    @Test
    fun operationSamplingWithoutEntriesUsesDefault() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            operationSampling = PerOperationSamplingStrategies(defaultSamplingProbability = 1.0),
        )

        val sampler = assertNotNull(strategy.toSampler(errors))

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, sampler.sample("anything"))
        assertFalse(errors.hasErrors())
    }

    @Test
    fun operationSamplingWithoutDefaultReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            operationSampling = PerOperationSamplingStrategies(defaultSamplingProbability = null),
        )

        assertNull(strategy.toSampler(errors))
        assertTrue(errors.hasErrors())
    }

    @Test
    fun operationSamplingWithInvalidEntryRateReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            operationSampling = PerOperationSamplingStrategies(
                defaultSamplingProbability = 0.5,
                perOperationStrategies = listOf(
                    OperationSamplingStrategy("/broken", ProbabilisticSamplingStrategy(2.0)),
                ),
            ),
        )

        assertNull(strategy.toSampler(errors))
        assertTrue(errors.hasErrors())
    }

    @Test
    fun operationSamplingWithMissingEntryRateReturnsNull() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            operationSampling = PerOperationSamplingStrategies(
                defaultSamplingProbability = 0.5,
                perOperationStrategies = listOf(
                    OperationSamplingStrategy("/broken", null),
                ),
            ),
        )

        assertNull(strategy.toSampler(errors))
        assertTrue(errors.hasErrors())
    }

    @Test
    fun boundsWarnButStillTranslate() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            operationSampling = PerOperationSamplingStrategies(
                defaultSamplingProbability = 0.5,
                defaultLowerBoundTracesPerSecond = 2.0,
            ),
        )

        val sampler = assertNotNull(strategy.toSampler(errors))

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, sampler.sample("anything"))
        assertEquals(1, errors.apiMisuses.size)
    }

    /** Proto3 sends unset numeric fields as zero; that must not be mistaken for a real bound. */
    @Test
    fun zeroBoundsDoNotWarn() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            operationSampling = PerOperationSamplingStrategies(
                defaultSamplingProbability = 0.5,
                defaultLowerBoundTracesPerSecond = 0.0,
                defaultUpperBoundTracesPerSecond = 0.0,
            ),
        )

        assertNotNull(strategy.toSampler(errors))
        assertFalse(errors.hasErrors())
    }

    @Test
    fun operationSamplingTakesPrecedenceOverProbabilistic() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            probabilisticSampling = ProbabilisticSamplingStrategy(0.0),
            operationSampling = PerOperationSamplingStrategies(defaultSamplingProbability = 1.0),
        )

        val sampler = assertNotNull(strategy.toSampler(errors))

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, sampler.sample("anything"))
    }

    @Test
    fun probabilisticTakesPrecedenceOverRateLimiting() {
        val errors = FakeSdkErrorHandler()
        val strategy = RemoteSamplingStrategy(
            probabilisticSampling = ProbabilisticSamplingStrategy(0.0),
            rateLimitingSampling = RateLimitingSamplingStrategy(5),
        )

        val sampler = assertNotNull(strategy.toSampler(errors))

        assertEquals(SamplingResult.Decision.DROP, sampler.sample("anything"))
        assertFalse(errors.hasErrors())
    }
}
