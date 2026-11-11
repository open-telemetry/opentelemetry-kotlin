package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.FakeCurrentContextStorage
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.createThreadLocalCurrentContextStorage
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.framework.SdkTracker
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
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

    @Test
    fun `throwing storage supplier falls back to the default storage`() {
        val handler = FakeSdkErrorHandler()
        val api = createOpenTelemetry {
            errorHandler(handler)
            context { storage { error("boom") } }
        }.also(sdks::track)
        assertNotNull(assertIs<ContextFactoryImpl>(api.context).installedStorage)
        assertEquals("boom", handler.userCodeErrors.single().cause.message)
    }

    private fun assertStorageUsed(storage: CurrentContextStorage) {
        val api = createOpenTelemetry {
            context { storage { storage } }
        }.also(sdks::track)
        assertSame(storage, assertIs<ContextFactoryImpl>(api.context).installedStorage)
    }
}
