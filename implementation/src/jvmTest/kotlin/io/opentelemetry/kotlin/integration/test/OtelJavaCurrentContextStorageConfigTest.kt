package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.ContextStorageOwner
import io.opentelemetry.kotlin.context.createOtelJavaCurrentContextStorage
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.framework.SdkTracker
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class OtelJavaCurrentContextStorageConfigTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `java bridge storage passed to the DSL is used by the SDK`() {
        val storage = createOtelJavaCurrentContextStorage()
        val api = createOpenTelemetry {
            context { this.storage = storage }
        }.also(sdks::track)
        assertSame(storage, assertIs<ContextStorageOwner>(api).installedStorage)
    }
}
