package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.SamplerBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SamplerConfigDslImplTest {

    @Test
    fun startsUnset() {
        assertNull(SamplerConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsAlwaysOn() {
        val dsl = SamplerConfigDslImpl()
        dsl.alwaysOn()
        assertEquals(SamplerBehavior.AlwaysOn, dsl.toBehavior())
    }

    @Test
    fun mapsAlwaysOff() {
        val dsl = SamplerConfigDslImpl()
        dsl.alwaysOff()
        assertEquals(SamplerBehavior.AlwaysOff, dsl.toBehavior())
    }

    @Test
    fun lastSuccessfulCallWins() {
        val dsl = SamplerConfigDslImpl()
        dsl.alwaysOn()
        dsl.alwaysOff()
        assertEquals(SamplerBehavior.AlwaysOff, dsl.toBehavior())
    }

    @Test
    fun mapsEveryParentBasedChild() {
        val dsl = SamplerConfigDslImpl()
        dsl.parentBased(
            root = dsl.alwaysOff(),
            remoteParentSampled = dsl.alwaysOn(),
            remoteParentNotSampled = dsl.alwaysOff(),
            localParentSampled = dsl.alwaysOn(),
            localParentNotSampled = dsl.alwaysOff(),
        )
        assertEquals(
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.AlwaysOff,
                remoteParentSampled = SamplerBehavior.AlwaysOn,
                remoteParentNotSampled = SamplerBehavior.AlwaysOff,
                localParentSampled = SamplerBehavior.AlwaysOn,
                localParentNotSampled = SamplerBehavior.AlwaysOff,
            ),
            dsl.toBehavior()
        )
    }

    @Test
    fun emptyParentBasedLeavesChildrenUnset() {
        val dsl = SamplerConfigDslImpl()
        dsl.parentBased()
        assertEquals(SamplerBehavior.ParentBased(), dsl.toBehavior())
    }
}
