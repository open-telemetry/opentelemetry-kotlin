package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.internalCurrentContext
import io.opentelemetry.kotlin.context.internalInstallContextStorage
import io.opentelemetry.kotlin.context.internalRootContext
import io.opentelemetry.kotlin.context.internalUninstallContextStorage
import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(TempContextStandInApi::class)
internal class MakeCurrentTest {

    private val storage = createSingleSlotCurrentContextStorage()

    @BeforeTest
    fun setUp() {
        internalInstallContextStorage(storage)
    }

    @AfterTest
    fun tearDown() {
        internalUninstallContextStorage(storage)
    }

    @Test
    fun testMakeCurrent() {
        val span = FakeSpan()
        val scope = internalCurrentContext().storeSpan(span).attach()
        try {
            assertSame(span, internalCurrentContext().extractSpan())
        } finally {
            scope.detach()
        }
    }

    @Test
    fun testMakeCurrentDetach() {
        val root = internalRootContext()
        val span = FakeSpan()
        val scope = internalCurrentContext().storeSpan(span).attach()
        scope.detach()
        assertSame(root, internalCurrentContext())
    }

    @Test
    fun testNestedUnwind() {
        val span1 = FakeSpan("a")
        val span2 = FakeSpan("b")

        val scope1 = internalCurrentContext().storeSpan(span1).attach()
        assertSame(span1, internalCurrentContext().extractSpan())

        val scope2 = internalCurrentContext().storeSpan(span2).attach()
        assertSame(span2, internalCurrentContext().extractSpan())

        scope2.detach()
        assertSame(span1, internalCurrentContext().extractSpan())

        scope1.detach()
        assertSame(internalRootContext(), internalCurrentContext())
    }
}
