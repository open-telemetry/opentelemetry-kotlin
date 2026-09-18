package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.schema.model.AlwaysOffSampler
import io.opentelemetry.kotlin.config.schema.model.AlwaysOnSampler
import io.opentelemetry.kotlin.config.schema.model.ParentBasedSampler
import io.opentelemetry.kotlin.config.schema.model.Sampler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SamplerMapperTest {

    /**
     * Verifies that declaring `always_on: {}` in YAML maps cleanly to [SamplerBehavior.AlwaysOn].
     */
    @Test
    fun mapsAlwaysOn() {
        assertEquals(
            SamplerBehavior.AlwaysOn,
            Sampler(alwaysOn = AlwaysOnSampler()).toBehavior(),
        )
    }

    /**
     * Verifies that declaring `always_off: {}` in YAML maps cleanly to [SamplerBehavior.AlwaysOff].
     */
    @Test
    fun mapsAlwaysOff() {
        assertEquals(
            SamplerBehavior.AlwaysOff,
            Sampler(alwaysOff = AlwaysOffSampler()).toBehavior()
        )
    }

    /**
     * Verifies that all 5 distinct delegates of `ParentBased` are correctly
     * mapped to their corresponding IR fields without copy-paste or swapped-parameter bugs.
     */
    @Test
    fun mapsEveryParentBasedChild() {
        val schema = Sampler(
            parentBased = ParentBasedSampler(
                root = Sampler(alwaysOff = AlwaysOffSampler()),
                remoteParentSampled = Sampler(alwaysOn = AlwaysOnSampler()),
                remoteParentNotSampled = Sampler(alwaysOff = AlwaysOffSampler()),
                localParentSampled = Sampler(alwaysOn = AlwaysOnSampler()),
                localParentNotSampled = Sampler(alwaysOff = AlwaysOffSampler())
            )
        )

        assertEquals(
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.AlwaysOff,
                remoteParentSampled = SamplerBehavior.AlwaysOn,
                remoteParentNotSampled = SamplerBehavior.AlwaysOff,
                localParentSampled = SamplerBehavior.AlwaysOn,
                localParentNotSampled = SamplerBehavior.AlwaysOff
            ),
            schema.toBehavior()
        )
    }

    /**
     * Verifies that declaring `parent_based: {}` is recognized as explicitly choosing the ParentBased
     * strategy, while leaving all 5 sub-delegates unset (`null`) so SDK defaults can apply at runtime.
     */
    @Test
    fun emptyParentBasedLeavesChildrenUnset() {
        assertEquals(
            SamplerBehavior.ParentBased(),
            Sampler(parentBased = ParentBasedSampler()).toBehavior()
        )
    }

    /**
     * Verifies that an empty `sampler: {}` block (no sampler specified) cleanly evaluates to unset (`null`).
     */
    @Test
    fun leavesOmittedSamplerUnset() {
        assertNull(Sampler().toBehavior())
    }

    /**
     * Verifies that if YAML contains 2 or more conflicting sampler keys,
     * the mapper safely degrades to unset (`null`) rather than crashing the application.
     */
    @Test
    fun leavesMultipleSamplersUnset() {
        assertNull(
            Sampler(
                alwaysOff = AlwaysOffSampler(),
                alwaysOn = AlwaysOnSampler()
            ).toBehavior()
        )
    }

    /**
     * Verifies that a `parent_based` sampler holding another one is mapped all the way down, so
     * the inner sampler is neither dropped nor flattened into its parent.
     */
    @Test
    fun mapsNestedParentBased() {
        assertEquals(
            nestedParentBasedBehavior(depth = 2),
            nestedParentBasedSampler(depth = 2).toBehavior(),
        )
    }

    /**
     * Verifies that nesting right up to the deepest supported level is still mapped in full, so
     * the limit does not reject configs it is meant to allow.
     */
    @Test
    fun mapsParentBasedAtNestingLimit() {
        assertEquals(
            nestedParentBasedBehavior(depth = MAX_PARENT_BASED_DEPTH),
            nestedParentBasedSampler(depth = MAX_PARENT_BASED_DEPTH).toBehavior(),
        )
    }

    /**
     * Verifies that one level past the limit degrades to unset (`null`), so the SDK falls back to
     * its default sampler rather than to a partly mapped version of what the file asked for.
     */
    @Test
    fun leavesParentBasedPastNestingLimitUnset() {
        assertNull(nestedParentBasedSampler(depth = MAX_PARENT_BASED_DEPTH + 1).toBehavior())
    }

    /**
     * Verifies that nesting deep enough to exhaust the stack is rejected too. Without a limit this
     * throws [StackOverflowError] while the SDK is starting up, taking the host app down with it.
     */
    @Test
    fun leavesParentBasedNestedBeyondTheStackUnset() {
        assertNull(nestedParentBasedSampler(depth = 10_000).toBehavior())
    }

    /**
     * Builds a sampler that nests [depth] `parent_based` samplers, with an `always_off` sampler as
     * the root of the innermost one.
     */
    private fun nestedParentBasedSampler(depth: Int): Sampler {
        var sampler = Sampler(alwaysOff = AlwaysOffSampler())
        repeat(depth) {
            sampler = Sampler(parentBased = ParentBasedSampler(root = sampler))
        }
        return sampler
    }

    /** The behavior a [nestedParentBasedSampler] of the same [depth] is expected to map onto. */
    private fun nestedParentBasedBehavior(depth: Int): SamplerBehavior {
        var behavior: SamplerBehavior = SamplerBehavior.AlwaysOff
        repeat(depth) {
            behavior = SamplerBehavior.ParentBased(root = behavior)
        }
        return behavior
    }
}
