package io.opentelemetry.kotlin.behavior.metrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MetricReaderBehaviorTest {

    @Test
    fun periodicStartsUnset() {
        assertNull(MetricReaderBehavior().periodic)
    }

    @Test
    fun pullStartsUnset() {
        assertNull(MetricReaderBehavior().pull)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredEitherKind() {
        val merged = MetricReaderBehavior().mergeWith(MetricReaderBehavior())

        assertNull(merged.periodic)
        assertNull(merged.pull)
    }

    @Test
    fun adoptsPeriodicFromWhicheverLayerSuppliedIt() {
        val periodic = PeriodicMetricReaderBehavior(interval = 5_000)

        assertEquals(
            periodic,
            MetricReaderBehavior().mergeWith(MetricReaderBehavior(periodic = periodic)).periodic,
        )
        assertEquals(
            periodic,
            MetricReaderBehavior(periodic = periodic).mergeWith(MetricReaderBehavior()).periodic,
        )
    }

    @Test
    fun adoptsPullFromWhicheverLayerSuppliedIt() {
        val pull = PullMetricReaderBehavior()

        assertEquals(
            pull,
            MetricReaderBehavior().mergeWith(MetricReaderBehavior(pull = pull)).pull,
        )
        assertEquals(
            pull,
            MetricReaderBehavior(pull = pull).mergeWith(MetricReaderBehavior()).pull,
        )
    }

    @Test
    fun keepsPeriodicWhenHigherLayerOnlySetsPull() {
        val periodic = PeriodicMetricReaderBehavior(interval = 5_000)
        val merged = MetricReaderBehavior(periodic = periodic).mergeWith(
            MetricReaderBehavior(pull = PullMetricReaderBehavior()),
        )

        assertEquals(periodic, merged.periodic)
        assertEquals(PullMetricReaderBehavior(), merged.pull)
    }
}
