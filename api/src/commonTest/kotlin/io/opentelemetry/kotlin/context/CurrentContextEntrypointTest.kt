/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.RecordingSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CurrentContextEntrypointTest {

    private class SlotStorage : CurrentContextStorage {
        private var slot: Context? = null
        var detachCount = 0

        override fun current(): Context = slot ?: rootContext()

        override fun attach(context: Context): Scope {
            val previous = slot
            slot = context
            return object : Scope {
                override fun detach(): Boolean {
                    detachCount++
                    slot = previous
                    return true
                }
            }
        }
    }

    private val key = createContextKey<String>("key")
    private val installed = mutableListOf<CurrentContextStorage>()
    private var errorHandler: RecordingSdkErrorHandler? = null

    @AfterTest
    fun tearDown() {
        installed.forEach { uninstallContextStorage(it) }
        errorHandler?.uninstall()
    }

    private fun install(storage: CurrentContextStorage = SlotStorage()): Boolean {
        val result = installContextStorage(storage)
        if (result) {
            installed += storage
        }
        return result
    }

    @Test
    fun testNoStorageInstalled() {
        assertSame(rootContext(), currentContext())
        val ctx = rootContext().set(key, "value")
        val scope = ctx.attach()
        assertSame(UnattachedScope, scope)
        assertSame(rootContext(), currentContext())
        assertTrue(scope.detach())
    }

    @Test
    fun testRunWithContextWithoutStorageStillRunsBlock() {
        val result = rootContext().set(key, "value").runWithContext {
            assertSame(rootContext(), currentContext())
            42
        }
        assertEquals(42, result)
    }

    @Test
    fun testAttachAndDetach() {
        assertTrue(install())

        val ctx = rootContext().set(key, "value")
        val scope = ctx.attach()
        assertSame(ctx, currentContext())
        assertTrue(scope.detach())
        assertSame(rootContext(), currentContext())
    }

    @Test
    fun testRunWithContext() {
        install()

        val ctx = rootContext().set(key, "value")
        val result = ctx.runWithContext {
            assertSame(ctx, currentContext())
            "done"
        }
        assertEquals("done", result)
        assertSame(rootContext(), currentContext())
    }

    @Test
    fun testRunWithContextRestoresWhenBlockThrows() {
        install()

        val ctx = rootContext().set(key, "value")
        assertFailsWith<IllegalStateException> {
            ctx.runWithContext { error("boom") }
        }
        assertSame(rootContext(), currentContext())
    }

    @Test
    fun testSecondInstallFails() {
        val first = SlotStorage()
        assertTrue(install(first))
        assertFalse(install())

        val ctx = rootContext().set(key, "value")
        ctx.attach()
        assertSame(ctx, first.current())
    }

    @Test
    fun testUninstall() {
        val storage = SlotStorage()
        install(storage)

        assertFalse(uninstallContextStorage(SlotStorage()))
        assertFalse(uninstallContextStorage(assertIs<CurrentContextStorage>(ProcessWideContextStorageSlot.installed())))
        assertTrue(uninstallContextStorage(storage))
        assertNull(ProcessWideContextStorageSlot.installed())
        assertSame(rootContext(), currentContext())
        assertFalse(uninstallContextStorage(storage))
    }

    @Test
    fun testInstalledStorageIsWrapped() {
        val storage = SlotStorage()
        install(storage)
        assertIs<MisuseReportingCurrentContextStorage>(ProcessWideContextStorageSlot.installed())
        assertNotSame(storage, ProcessWideContextStorageSlot.installed())

        val scope = rootContext().set(key, "value").attach()
        assertTrue(scope.detach())
        assertFalse(scope.detach())
        assertEquals(1, storage.detachCount)
    }

    @Test
    fun testOutOfOrderDetachIsReported() {
        val handler = RecordingSdkErrorHandler().also { errorHandler = it }
        handler.install()
        install()

        val outer = rootContext().set(key, "outer").attach()
        val inner = rootContext().set(key, "inner").attach()
        outer.detach()

        val error = assertIs<SdkError.ApiMisuse>(handler.errors.single())
        inner.detach()
        assertEquals("Scope.detach", error.api)
    }
}
