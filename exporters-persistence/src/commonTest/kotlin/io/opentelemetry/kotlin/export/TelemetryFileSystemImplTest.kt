package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import okio.Buffer
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import okio.use
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TelemetryFileSystemImplTest {

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
    fun testCreatesDirectoryOnInit() {
        assertTrue(fakeFileSystem.exists(directory))
    }

    @Test
    fun testWriteCreatesFile() {
        val content = "test content"
        val filename = "test.txt"
        val expectedPath = directory / filename

        val source = Buffer().writeUtf8(content)
        assertTrue(impl.write(filename, source))
        assertTrue(fakeFileSystem.exists(expectedPath))
    }

    @Test
    fun testWriteReturnsFalseOnError() {
        fakeFileSystem.delete(directory)

        val source = Buffer().writeUtf8("content")
        assertFalse(impl.write("test.txt", source))
    }

    @Test
    fun testListReturnsEmptyList() {
        val files = impl.list()
        assertTrue(files.isEmpty())
    }

    @Test
    fun testListDirectoryNotPresent() {
        fakeFileSystem.delete(directory)
        val files = impl.list()
        assertTrue(files.isEmpty())
    }

    @Test
    fun testListReturnsFilenames() {
        fakeFileSystem.write(directory / "file1.txt") { writeUtf8("1") }
        fakeFileSystem.write(directory / "file2.txt") { writeUtf8("2") }

        val files = impl.list()
        assertEquals(2, files.size)
        assertTrue(files.contains("file1.txt"))
        assertTrue(files.contains("file2.txt"))
    }

    @Test
    fun testDeleteRemovesFile() {
        val filename = "test.txt"
        fakeFileSystem.write(directory / filename) { writeUtf8("content") }
        assertTrue(fakeFileSystem.exists(directory / filename))

        impl.delete(filename)
        assertFalse(fakeFileSystem.exists(directory / filename))
    }

    @Test
    fun testDeleteIgnoresNonExistentFile() {
        impl.delete("nonexistent.txt")
    }

    @Test
    fun testReadNonExistentFile() {
        val source = impl.read("nonexistent.txt")
        assertNull(source)
    }

    @Test
    fun testWriteReadRoundTrip() {
        val content = "round trip content"
        val filename = "roundtrip.txt"
        val writeBuffer = Buffer().writeUtf8(content)
        assertTrue(impl.write(filename, writeBuffer))

        val observed = checkNotNull(impl.read(filename)).use {
            it.readUtf8()
        }
        assertEquals(content, observed)
    }

    @Test
    fun testOverwriteExistingFile() {
        val filename = "test.txt"

        val oldSource = Buffer().writeUtf8("old content")
        assertTrue(impl.write(filename, oldSource))

        val newContent = "new content"
        val source = Buffer().writeUtf8(newContent)
        assertTrue(impl.write(filename, source))

        val observed = checkNotNull(impl.read(filename)).use {
            it.readUtf8()
        }
        assertEquals(newContent, observed)
    }
}
