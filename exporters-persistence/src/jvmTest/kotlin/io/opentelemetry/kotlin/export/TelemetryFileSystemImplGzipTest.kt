package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.Buffer
import okio.Path.Companion.toPath
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import okio.use
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TelemetryFileSystemImplGzipTest {

    private lateinit var fakeFileSystem: FakeFileSystem
    private lateinit var impl: TelemetryFileSystem
    private val directory = "/fake-telemetry".toPath()

    @BeforeTest
    fun setup() {
        fakeFileSystem = FakeFileSystem()
        impl = TelemetryFileSystemImpl(fakeFileSystem, directory)
    }

    @AfterTest
    fun tearDown() {
        fakeFileSystem.checkNoOpenFiles()
    }

    @Test
    fun testGzipRoundTrip() {
        val content = "test content"
        val filename = "data.gz"

        val source = Buffer().writeUtf8(content)
        assertTrue(impl.write(filename, source))

        fakeFileSystem.source(directory / filename).buffer().use { storedSource ->
            assertNotEquals(content, storedSource.readUtf8())
        }

        val observed = checkNotNull(impl.read(filename)).use {
            it.readUtf8()
        }
        assertEquals(content, observed)
    }
}
