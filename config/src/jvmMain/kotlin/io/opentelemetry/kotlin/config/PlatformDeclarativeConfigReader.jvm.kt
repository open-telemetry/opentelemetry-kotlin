package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.yaml.ConfigFileReaderImpl
import io.opentelemetry.kotlin.config.yaml.toBehavior

@ExperimentalApi
internal actual fun platformDeclarativeConfigReader(): DeclarativeConfigReader? {
    val fileReader = ConfigFileReaderImpl()
    return DeclarativeConfigReader { path -> fileReader.read(path).toBehavior() }
}
