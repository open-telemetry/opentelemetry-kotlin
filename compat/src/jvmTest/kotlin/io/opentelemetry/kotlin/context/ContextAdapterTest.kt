package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class ContextAdapterTest {

    @Test
    fun testScope() {
        val ctx = ContextAdapter(OtelJavaContext.root())
        val scope = ctx.attach()
        assertNotNull(scope)
        scope.detach()
    }
}
