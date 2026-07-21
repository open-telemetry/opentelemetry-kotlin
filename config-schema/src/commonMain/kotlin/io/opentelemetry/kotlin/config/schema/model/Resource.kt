// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Resource(
  /**
   * Configure resource attributes. Entries have higher priority than entries from .resource.attributes_list.
   * If omitted, no resource attributes are added.
   */
  internal val attributes: List<AttributeNameValue>? = null,
  /**
   * Configure resource detection.
   * If omitted, resource detection is disabled.
   */
  @SerialName("detection/development")
  internal val detectionDevelopment: ExperimentalResourceDetection? = null,
  /**
   * Configure resource schema URL.
   * If omitted or null, no schema URL is used.
   */
  @SerialName("schema_url")
  internal val schemaUrl: String? = null,
  /**
   * Configure resource attributes. Entries have lower priority than entries from .resource.attributes.
   * The value is a list of comma separated key-value pairs matching the format of OTEL_RESOURCE_ATTRIBUTES. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration for details.
   * If omitted or null, no resource attributes are added.
   */
  @SerialName("attributes_list")
  internal val attributesList: String? = null,
)
