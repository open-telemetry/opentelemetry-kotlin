// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalInstrumentation(
  /**
   * Configure general SemConv options that may apply to multiple languages and instrumentations.
   * Instrumenation may merge general config options with the language specific configuration at .instrumentation.<language>.
   * If omitted, default values as described in ExperimentalGeneralInstrumentation are used.
   */
  internal val general: ExperimentalGeneralInstrumentation? = null,
  /**
   * Configure C++ language-specific instrumentation libraries.
   * If omitted, instrumentation defaults are used.
   */
  internal val cpp: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure .NET language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val dotnet: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Erlang language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val erlang: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Go language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val go: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Java language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val java: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure JavaScript language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val js: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure PHP language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val php: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Python language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val python: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Ruby language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val ruby: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Rust language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val rust: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Swift language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  internal val swift: ExperimentalLanguageSpecificInstrumentation? = null,
)
