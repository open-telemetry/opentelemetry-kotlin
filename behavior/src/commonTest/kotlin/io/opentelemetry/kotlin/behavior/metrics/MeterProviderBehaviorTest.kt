package io.opentelemetry.kotlin.behavior.metrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MeterProviderBehaviorTest {

    @Test
    fun readersStartUnset() {
        assertNull(MeterProviderBehavior().readers)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredReaders() {
        assertNull(MeterProviderBehavior().mergeWith(MeterProviderBehavior()).readers)
    }

    @Test
    fun adoptsReadersFromWhicheverLayerSuppliedThem() {
        val readers = listOf(MetricReaderBehavior())

        assertEquals(
            readers,
            MeterProviderBehavior().mergeWith(MeterProviderBehavior(readers = readers)).readers,
        )
        assertEquals(
            readers,
            MeterProviderBehavior(readers = readers).mergeWith(MeterProviderBehavior()).readers,
        )
    }

    @Test
    fun higherLayerReplacesTheReaderList() {
        val lower = listOf(MetricReaderBehavior(periodic = PeriodicMetricReaderBehavior(interval = 1_000)))
        val higher = listOf(MetricReaderBehavior(pull = PullMetricReaderBehavior()))

        assertEquals(
            higher,
            MeterProviderBehavior(readers = lower).mergeWith(MeterProviderBehavior(readers = higher)).readers,
        )
    }

    @Test
    fun explicitEmptyListClearsLowerReaders() {
        val lower = MeterProviderBehavior(readers = listOf(MetricReaderBehavior()))

        assertEquals(
            emptyList(),
            lower.mergeWith(MeterProviderBehavior(readers = emptyList())).readers,
        )
    }
}
