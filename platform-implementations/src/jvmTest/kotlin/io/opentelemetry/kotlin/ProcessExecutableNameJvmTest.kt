package io.opentelemetry.kotlin

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProcessExecutableNameJvmTest {

    @Test
    fun testReturnsExecutableFileName() {
        val command = ProcessHandle.current().info().command().get()
        assertEquals(File(command).name, getProcessExecutableName())
    }
}
