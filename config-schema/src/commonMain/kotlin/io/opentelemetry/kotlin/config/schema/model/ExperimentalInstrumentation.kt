// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
public data class ExperimentalInstrumentation(
  /**
   * Configure general SemConv options that may apply to multiple languages and instrumentations.
   * Instrumenation may merge general config options with the language specific configuration at .instrumentation.<language>.
   * If omitted, default values as described in ExperimentalGeneralInstrumentation are used.
   */
  public val general: ExperimentalGeneralInstrumentation? = null,
  /**
   * Configure C++ language-specific instrumentation libraries.
   * If omitted, instrumentation defaults are used.
   */
  public val cpp: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure .NET language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val dotnet: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Erlang language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val erlang: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Go language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val go: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Java language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val java: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure JavaScript language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val js: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure PHP language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val php: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Python language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val python: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Ruby language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val ruby: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Rust language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val rust: ExperimentalLanguageSpecificInstrumentation? = null,
  /**
   * Configure Swift language-specific instrumentation libraries.
   * Each entry's key identifies a particular instrumentation library. The corresponding value configures it.
   * If omitted, instrumentation defaults are used.
   */
  public val swift: ExperimentalLanguageSpecificInstrumentation? = null,
)
