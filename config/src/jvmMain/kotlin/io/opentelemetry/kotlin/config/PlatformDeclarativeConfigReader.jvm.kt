package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.yaml.ConfigFileReaderImpl
import io.opentelemetry.kotlin.config.yaml.readInstrumentationNode
import io.opentelemetry.kotlin.config.yaml.toBehavior
import io.opentelemetry.kotlin.config.yaml.toConfigProperties
import io.opentelemetry.kotlin.getFileSystem
import okio.Path.Companion.toPath

@ExperimentalApi
internal actual fun platformDeclarativeConfigReader(): DeclarativeConfigReader? {
    val fileReader = ConfigFileReaderImpl()
    return object : DeclarativeConfigReader {
        override fun read(path: String): OpenTelemetryBehavior = fileReader.read(path).toBehavior()

        override fun readInstrumentation(path: String): ConfigProperties =
            readInstrumentationNode(getFileSystem(), path.toPath()).toConfigProperties()
    }
}
