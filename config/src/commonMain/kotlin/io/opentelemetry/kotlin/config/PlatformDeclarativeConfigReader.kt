package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.yaml.ConfigFileReaderImpl
import io.opentelemetry.kotlin.config.yaml.readInstrumentationNode
import io.opentelemetry.kotlin.config.yaml.toBehavior
import io.opentelemetry.kotlin.getFileSystem
import okio.Path.Companion.toPath

/**
 * The reader for declarative config files on the platform the SDK is running on, or `null` where
 * the SDK does not read them. Declarative config files are read on the JVM, Android, and Apple
 * targets. They are not read on Linux, nor on JS, which has no file system to read from.
 */
@ExperimentalApi
internal expect fun platformDeclarativeConfigReader(): DeclarativeConfigReader?

/**
 * The [DeclarativeConfigReader] shared by every platform that reads declarative config files.
 */
@ExperimentalApi
internal fun realDeclarativeConfigReader(): DeclarativeConfigReader {
    val fileReader = ConfigFileReaderImpl()
    return DeclarativeConfigReader { path ->
        fileReader.read(path).toBehavior().copy(
            instrumentation = readInstrumentationNode(getFileSystem(), path.toPath()),
        )
    }
}
