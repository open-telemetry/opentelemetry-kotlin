package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.FakeCurrentContextStorage
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.createThreadLocalCurrentContextStorage
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.framework.SdkTracker
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryContextStorageTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `single-slot storage passed to the DSL is used by the SDK`() {
        assertStorageUsed(createSingleSlotCurrentContextStorage())
    }

    @Test
    fun `thread-local storage passed to the DSL is used by the SDK`() {
        assertStorageUsed(createThreadLocalCurrentContextStorage())
    }

    @Test
    fun `custom storage passed to the DSL is used by the SDK`() {
        assertStorageUsed(FakeCurrentContextStorage())
    }

    private fun assertStorageUsed(storage: CurrentContextStorage) {
        val api = createOpenTelemetry {
            context { this.storage = storage }
        }.also(sdks::track)
        assertSame(storage, assertIs<ContextFactoryImpl>(api.context).storage)
    }
}
