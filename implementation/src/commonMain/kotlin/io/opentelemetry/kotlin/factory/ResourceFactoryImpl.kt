package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.NO_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.resource.Entity
import io.opentelemetry.kotlin.resource.EntityAttributesMutator
import io.opentelemetry.kotlin.resource.EntityAttributesMutatorImpl
import io.opentelemetry.kotlin.resource.EntityImpl
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceImpl

internal class ResourceFactoryImpl(
    private val sdkErrorHandler: SdkErrorHandler
) : ResourceFactory {

    override val empty: Resource = ResourceImpl(AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT), null)

    override fun create(schemaUrl: String?, attributes: AttributesMutator.() -> Unit): Resource {
        val attrs = AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT).apply { attributes() }
        return ResourceImpl(attrs, schemaUrl)
    }

    override fun createEntity(
        type: String,
        schemaUrl: String?,
        attributes: EntityAttributesMutator.() -> Unit
    ): Entity? =
        sdkErrorHandler.sdkGuardOrDefault(null, "ResourceFactory.createEntity failed") {
            if (type.isEmpty()) {
                sdkErrorHandler.reportError(
                    SdkError.ApiMisuse(
                        api = "ResourceFactory.createEntity",
                        message = "Entity type must not be empty",
                        severity = SdkErrorSeverity.WARNING,
                    )
                )
                return null
            }

            val mutator = EntityAttributesMutatorImpl()
            userCode { mutator.attributes() }

            if (mutator.idAttributes.isEmpty()) {
                sdkErrorHandler.reportError(
                    SdkError.ApiMisuse(
                        api = "ResourceFactory.createEntity",
                        message = "Entity id must contain at least one attribute",
                        severity = SdkErrorSeverity.WARNING,
                    )
                )
                return null
            }

            return EntityImpl(
                type = type,
                schemaUrl = schemaUrl,
                id = mutator.idAttributes,
                description = mutator.descriptionAttributes,
            )
        }
}
