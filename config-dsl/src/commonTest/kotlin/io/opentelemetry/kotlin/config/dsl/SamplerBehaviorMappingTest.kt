package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.SamplerBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SamplerBehaviorMappingTest {

    private fun SamplerBehavior.map(): String = mapToSampler(
        alwaysOn = { "AlwaysOn" },
        alwaysOff = { "AlwaysOff" },
        parentBased = { root, remoteParentSampled, remoteParentNotSampled, localParentSampled, localParentNotSampled ->
            "ParentBased{" +
                "root:$root," +
                "remoteParentSampled:$remoteParentSampled," +
                "remoteParentNotSampled:$remoteParentNotSampled," +
                "localParentSampled:$localParentSampled," +
                "localParentNotSampled:$localParentNotSampled" +
                "}"
        },
    )

    @Test
    fun mapsAlwaysOn() {
        assertEquals("AlwaysOn", SamplerBehavior.AlwaysOn.map())
    }

    @Test
    fun mapsAlwaysOff() {
        assertEquals("AlwaysOff", SamplerBehavior.AlwaysOff.map())
    }

    @Test
    fun emptyParentBasedUsesSpecChildDefaults() {
        assertEquals(
            parentBased(root = "AlwaysOn"),
            SamplerBehavior.ParentBased().map(),
        )
    }

    @Test
    fun omittedParentBasedChildrenKeepDefaults() {
        assertEquals(
            parentBased(root = "AlwaysOff"),
            SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff).map(),
        )
    }

    @Test
    fun invertedParentBasedChildrenAreMapped() {
        assertEquals(
            parentBased(
                root = "AlwaysOff",
                remoteParentSampled = "AlwaysOff",
                remoteParentNotSampled = "AlwaysOn",
                localParentSampled = "AlwaysOff",
                localParentNotSampled = "AlwaysOn",
            ),
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.AlwaysOff,
                remoteParentSampled = SamplerBehavior.AlwaysOff,
                remoteParentNotSampled = SamplerBehavior.AlwaysOn,
                localParentSampled = SamplerBehavior.AlwaysOff,
                localParentNotSampled = SamplerBehavior.AlwaysOn,
            ).map(),
        )
    }

    @Test
    fun nestedParentBasedMapsRecursively() {
        assertEquals(
            parentBased(root = parentBased(root = "AlwaysOff")),
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff)
            ).map(),
        )
    }

    private companion object {
        fun parentBased(
            root: String,
            remoteParentSampled: String = "AlwaysOn",
            remoteParentNotSampled: String = "AlwaysOff",
            localParentSampled: String = "AlwaysOn",
            localParentNotSampled: String = "AlwaysOff",
        ): String =
            "ParentBased{" +
                "root:$root," +
                "remoteParentSampled:$remoteParentSampled," +
                "remoteParentNotSampled:$remoteParentNotSampled," +
                "localParentSampled:$localParentSampled," +
                "localParentNotSampled:$localParentNotSampled" +
                "}"
    }
}
