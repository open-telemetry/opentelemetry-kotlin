// Generated from the opentelemetry-configuration JSON schema. Do not edit manually.
package io.opentelemetry.kotlin.config.schema.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ViewSelector(
  /**
   * Configure instrument name selection criteria.
   * If omitted or null, all instrument names match.
   */
  @SerialName("instrument_name")
  public val instrumentName: String? = null,
  /**
   * Configure instrument type selection criteria.
   * Values include:
   * * counter: Synchronous counter instruments.
   * * gauge: Synchronous gauge instruments.
   * * histogram: Synchronous histogram instruments.
   * * observable_counter: Asynchronous counter instruments.
   * * observable_gauge: Asynchronous gauge instruments.
   * * observable_up_down_counter: Asynchronous up down counter instruments.
   * * up_down_counter: Synchronous up down counter instruments.
   * If omitted, all instrument types match.
   */
  @SerialName("instrument_type")
  public val instrumentType: InstrumentType? = null,
  /**
   * Configure the instrument unit selection criteria.
   * If omitted or null, all instrument units match.
   */
  public val unit: String? = null,
  /**
   * Configure meter name selection criteria.
   * If omitted or null, all meter names match.
   */
  @SerialName("meter_name")
  public val meterName: String? = null,
  /**
   * Configure meter version selection criteria.
   * If omitted or null, all meter versions match.
   */
  @SerialName("meter_version")
  public val meterVersion: String? = null,
  /**
   * Configure meter schema url selection criteria.
   * If omitted or null, all meter schema URLs match.
   */
  @SerialName("meter_schema_url")
  public val meterSchemaUrl: String? = null,
)
