package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.internalInstallContextStorage
import io.opentelemetry.kotlin.context.internalUninstallContextStorage
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(TempContextStandInApi::class)
internal class MakeCurrentTest {

    private val storage = createSingleSlotCurrentContextStorage()

    private lateinit var contextFactory: ContextFactoryImpl

    @BeforeTest
    fun setUp() {
        internalInstallContextStorage(storage)
        contextFactory = ContextFactoryImpl()
    }

    @AfterTest
    fun tearDown() {
        internalUninstallContextStorage(storage)
    }

    @Test
    fun testMakeCurrent() {
        val span = FakeSpan()
        val scope = contextFactory.currentContext().storeSpan(span).attach()
        try {
            assertSame(span, contextFactory.currentContext().extractSpan())
        } finally {
            scope.detach()
        }
    }

    @Test
    fun testMakeCurrentDetach() {
        val root = contextFactory.rootContext()
        val span = FakeSpan()
        val scope = contextFactory.currentContext().storeSpan(span).attach()
        scope.detach()
        assertSame(root, contextFactory.currentContext())
    }

    @Test
    fun testNestedUnwind() {
        val span1 = FakeSpan("a")
        val span2 = FakeSpan("b")

        val scope1 = contextFactory.currentContext().storeSpan(span1).attach()
        assertSame(span1, contextFactory.currentContext().extractSpan())

        val scope2 = contextFactory.currentContext().storeSpan(span2).attach()
        assertSame(span2, contextFactory.currentContext().extractSpan())

        scope2.detach()
        assertSame(span1, contextFactory.currentContext().extractSpan())

        scope1.detach()
        assertSame(contextFactory.rootContext(), contextFactory.currentContext())
    }
}
