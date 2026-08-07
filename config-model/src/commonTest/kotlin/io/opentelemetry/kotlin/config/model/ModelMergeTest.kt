package io.opentelemetry.kotlin.config.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ModelMergeTest {

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

    /**
     * Sums on merge, so a merge performed when only one layer supplied a node shows up as a wrong
     * value rather than silently passing.
     */
    private data class Node(val value: Int) : ConfigModel<Node> {
        override fun mergeWith(higher: Node) = Node(value + higher.value)
    }
}
