// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ExperimentalResourceDetector(
  /**
   * Enable the container resource detector, which populates container.* attributes.
   * If omitted, ignore.
   */
  internal val container: ExperimentalContainerResourceDetector? = null,
  /**
   * Enable the host resource detector, which populates host.* and os.* attributes.
   * If omitted, ignore.
   */
  internal val host: ExperimentalHostResourceDetector? = null,
  /**
   * Enable the process resource detector, which populates process.* attributes.
   * If omitted, ignore.
   */
  internal val process: ExperimentalProcessResourceDetector? = null,
  /**
   * Enable the service detector, which populates service.name based on the OTEL_SERVICE_NAME environment variable and service.instance.id.
   * If omitted, ignore.
   */
  internal val service: ExperimentalServiceResourceDetector? = null,
)
