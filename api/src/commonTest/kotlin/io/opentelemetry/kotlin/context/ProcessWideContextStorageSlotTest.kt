package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class ProcessWideContextStorageSlotTest {

    private class FakeStorage : CurrentContextStorage {
        override fun current(): Context = rootContext()
        override fun attach(context: Context): Scope = UnattachedScope
    }

    private val original = FakeStorage()
    private val effective = FakeStorage()

    @AfterTest
    fun tearDown() {
        ProcessWideContextStorageSlot.reset()
    }

    @Test
    fun testEmptyByDefault() {
        assertNull(ProcessWideContextStorageSlot.installed())
    }

    @Test
    fun testFirstInstallSucceeds() {
        assertTrue(ProcessWideContextStorageSlot.install(original, effective))
        assertSame(effective, ProcessWideContextStorageSlot.installed())
    }

    @Test
    fun testSecondInstallFailsAndKeepsFirst() {
        assertTrue(ProcessWideContextStorageSlot.install(original, effective))
        val other = FakeStorage()
        assertFalse(ProcessWideContextStorageSlot.install(other, other))
        assertSame(effective, ProcessWideContextStorageSlot.installed())
    }

    @Test
    fun testUninstallByNonOwnerFails() {
        ProcessWideContextStorageSlot.install(original, effective)
        assertFalse(ProcessWideContextStorageSlot.uninstall(FakeStorage()))
        assertSame(effective, ProcessWideContextStorageSlot.installed())
    }

    @Test
    fun testUninstallByEffectiveFails() {
        ProcessWideContextStorageSlot.install(original, effective)
        assertFalse(ProcessWideContextStorageSlot.uninstall(effective))
        assertSame(effective, ProcessWideContextStorageSlot.installed())
    }

    @Test
    fun testUninstallByOriginalSucceeds() {
        ProcessWideContextStorageSlot.install(original, effective)
        assertTrue(ProcessWideContextStorageSlot.uninstall(original))
        assertNull(ProcessWideContextStorageSlot.installed())
    }

    @Test
    fun testUninstallWhenEmptyFails() {
        assertFalse(ProcessWideContextStorageSlot.uninstall(original))
    }

    @Test
    fun testInstallAfterUninstallSucceeds() {
        ProcessWideContextStorageSlot.install(original, effective)
        ProcessWideContextStorageSlot.uninstall(original)
        val other = FakeStorage()
        assertTrue(ProcessWideContextStorageSlot.install(other, other))
        assertSame(other, ProcessWideContextStorageSlot.installed())
    }

    @Test
    fun testResetEmptiesSlot() {
        ProcessWideContextStorageSlot.install(original, effective)
        ProcessWideContextStorageSlot.reset()
        assertNull(ProcessWideContextStorageSlot.installed())
        assertTrue(ProcessWideContextStorageSlot.install(original, effective))
    }
}
