package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.installContextStorage
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.context.uninstallContextStorage
import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

internal class MakeCurrentTest {

    private val storage = createSingleSlotCurrentContextStorage()

    @BeforeTest
    fun setUp() {
        installContextStorage(storage)
    }

    @AfterTest
    fun tearDown() {
        uninstallContextStorage(storage)
    }

    @Test
    fun testMakeCurrent() {
        val span = FakeSpan()
        val scope = currentContext().storeSpan(span).attach()
        try {
            assertSame(span, currentContext().extractSpan())
        } finally {
            scope.detach()
        }
    }

    @Test
    fun testMakeCurrentDetach() {
        val root = rootContext()
        val span = FakeSpan()
        val scope = currentContext().storeSpan(span).attach()
        scope.detach()
        assertSame(root, currentContext())
    }

    @Test
    fun testNestedUnwind() {
        val span1 = FakeSpan("a")
        val span2 = FakeSpan("b")

        val scope1 = currentContext().storeSpan(span1).attach()
        assertSame(span1, currentContext().extractSpan())

        val scope2 = currentContext().storeSpan(span2).attach()
        assertSame(span2, currentContext().extractSpan())

        scope2.detach()
        assertSame(span1, currentContext().extractSpan())

        scope1.detach()
        assertSame(rootContext(), currentContext())
    }
}
