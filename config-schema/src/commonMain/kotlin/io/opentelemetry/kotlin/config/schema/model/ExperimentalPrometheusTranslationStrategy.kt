// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class ExperimentalPrometheusTranslationStrategy {
  @SerialName("underscore_escaping_with_suffixes")
  UNDERSCORE_ESCAPING_WITH_SUFFIXES,
  @SerialName("underscore_escaping_without_suffixes/development")
  UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES_DEVELOPMENT,
  @SerialName("no_utf8_escaping_with_suffixes/development")
  NO_UTF8_ESCAPING_WITH_SUFFIXES_DEVELOPMENT,
  @SerialName("no_translation/development")
  NO_TRANSLATION_DEVELOPMENT,
}
