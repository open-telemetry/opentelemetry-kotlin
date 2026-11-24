package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BehaviorMergeTest {

    @Test
    fun usesHigherWhenLowerIsUnset() {
        assertEquals(Node(2), mergeNode(null, Node(2)))
    }

    @Test
    fun usesLowerWhenHigherIsUnset() {
        assertEquals(Node(1), mergeNode(Node(1), null))
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredIt() {
        assertNull(mergeNode<Node>(null, null))
    }

    @Test
    fun combinesWhenBothLayersConfiguredIt() {
        assertEquals(Node(3), mergeNode(Node(1), Node(2)))
    }

    @Test
    fun usesWhicheverMapTheOtherLayerLeftUnset() {
        val map = mapOf("a" to 1)
        assertEquals(map, mergeMap(null, map))
        assertEquals(map, mergeMap(map, null))
        assertNull(mergeMap<String, Int>(null, null))
    }

    @Test
    fun combinesMapsWithHigherEntriesWinningOnKeyCollision() {
        val merged = mergeMap(mapOf("a" to 1, "b" to 2), mapOf("b" to 99, "c" to 3))
        assertEquals(mapOf("a" to 1, "b" to 99, "c" to 3), merged)
    }

    /**
     * Sums on merge, so a merge performed when only one layer supplied a node shows up as a wrong
     * value rather than silently passing.
     */
    private data class Node(val value: Int) : Behavior<Node> {
        override fun mergeWith(higher: Node) = Node(value + higher.value)
    }
}
