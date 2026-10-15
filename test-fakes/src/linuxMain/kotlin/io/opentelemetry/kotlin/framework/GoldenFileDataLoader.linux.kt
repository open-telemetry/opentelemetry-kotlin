package io.opentelemetry.kotlin.framework

import okio.FileSystem
import okio.Path.Companion.toPath

actual fun loadTestFixture(fixtureName: String): String =
    FileSystem.SYSTEM.read("resources/$fixtureName".toPath()) { readUtf8() }
