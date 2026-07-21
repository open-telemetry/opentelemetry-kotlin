// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GrpcTls(
  /**
   * Configure certificate used to verify a server's TLS credentials. 
   * Absolute path to certificate file in PEM format.
   * If omitted or null, system default certificate verification is used for secure connections.
   */
  @SerialName("ca_file")
  internal val caFile: String? = null,
  /**
   * Configure mTLS private client key. 
   * Absolute path to client key file in PEM format. If set, .client_certificate must also be set.
   * If omitted or null, mTLS is not used.
   */
  @SerialName("key_file")
  internal val keyFile: String? = null,
  /**
   * Configure mTLS client certificate. 
   * Absolute path to client certificate file in PEM format. If set, .client_key must also be set.
   * If omitted or null, mTLS is not used.
   */
  @SerialName("cert_file")
  internal val certFile: String? = null,
  /**
   * Configure client transport security for the exporter's connection. 
   * Only applicable when .endpoint is provided without http or https scheme. Implementations may choose to ignore .insecure.
   * If omitted or null, false is used.
   */
  internal val insecure: Boolean? = null,
)
