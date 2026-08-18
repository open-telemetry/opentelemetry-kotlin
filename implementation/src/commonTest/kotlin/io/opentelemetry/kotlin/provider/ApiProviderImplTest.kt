package io.opentelemetry.kotlin.provider

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import kotlin.test.Test
import kotlin.test.assertSame

internal class ApiProviderImplTest {

    @Test
    fun getOrCreateReturnsSameInstanceForSameKey() {
        val provider = ApiProviderImpl { Any() }
        val key = InstrumentationScopeInfoImpl("name", null, null, emptyMap())
        val first = provider.getOrCreate(key)
        val second = provider.getOrCreate(key)
        assertSame(first, second)
    }
}
