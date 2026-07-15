package io.opentelemetry.kotlin.config.schema

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

/**
 * Generates Kotlin data classes, enums and type aliases from the
 * `opentelemetry-configuration` JSON schema.
 *
 * The schema is passed in as the nested [Map]/[List] structure produced by Groovy's `JsonSlurper`.
 * Every generated type is `internal` and annotated with kotlinx.serialization annotations.
 */
internal class ConfigModelGenerator(private val packageName: String) {

    private val serializable = ClassName("kotlinx.serialization", "Serializable")
    private val serialName = ClassName("kotlinx.serialization", "SerialName")
    private val contextual = ClassName("kotlinx.serialization", "Contextual")

    private val contextualAnyNullable: TypeName =
        ANY.copy(nullable = true, annotations = listOf(AnnotationSpec.builder(contextual).build()))

    fun generate(schema: Map<String, Any?>): List<FileSpec> {
        val defs = asMap(schema["\$defs"])
        val files = mutableListOf<FileSpec>()
        files += buildDataClass(rootName(schema), schema)
        for ((name, def) in defs) {
            files += buildType(name, asMap(def))
        }
        return files
    }

    private fun rootName(schema: Map<String, Any?>): String =
        schema["title"] as? String ?: "OpenTelemetryConfiguration"

    private fun buildType(name: String, def: Map<String, Any?>): FileSpec = when {
        def.containsKey("enum") -> buildEnum(name, def)
        hasProperties(def) -> buildDataClass(name, def)
        isMapType(def) -> buildMapTypeAlias(name, def)
        else -> buildMarkerClass(name, def)
    }

    private fun buildDataClass(name: String, def: Map<String, Any?>): FileSpec {
        val properties = asMap(def["properties"])
        val required = asStringList(def["required"]).toSet()

        val constructor = FunSpec.constructorBuilder()
        val typeBuilder = TypeSpec.classBuilder(name)
            .addModifiers(KModifier.DATA, KModifier.INTERNAL)
            .addAnnotation(serializable)
            .apply { kdoc(def)?.let { addKdoc("%L", it) } }

        for ((wireName, rawSchema) in properties) {
            val propSchema = asMap(rawSchema)
            val kotlinName = toCamelCase(wireName)
            val nullable = wireName !in required || includesNull(propSchema)
            val type = baseType(propSchema).copy(nullable = nullable)

            val param = ParameterSpec.builder(kotlinName, type).apply {
                if (kotlinName != wireName) {
                    addAnnotation(
                        AnnotationSpec.builder(serialName).addMember("%S", wireName).build()
                    )
                }
                if (nullable) {
                    defaultValue("null")
                }
            }.build()

            constructor.addParameter(param)
            typeBuilder.addProperty(
                PropertySpec.builder(kotlinName, type, KModifier.INTERNAL)
                    .initializer(kotlinName)
                    .apply { kdoc(propSchema)?.let { addKdoc("%L", it) } }
                    .build()
            )
        }

        return fileOf(name, typeBuilder.primaryConstructor(constructor.build()).build())
    }

    private fun buildEnum(name: String, def: Map<String, Any?>): FileSpec {
        val builder = TypeSpec.enumBuilder(name)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(serializable)
            .apply { kdoc(def)?.let { addKdoc("%L", it) } }

        for (value in asStringList(def["enum"])) {
            builder.addEnumConstant(
                sanitizeEnumConstant(value),
                TypeSpec.anonymousClassBuilder()
                    .addAnnotation(
                        AnnotationSpec.builder(serialName).addMember("%S", value).build()
                    )
                    .build()
            )
        }
        return fileOf(name, builder.build())
    }

    private fun buildMapTypeAlias(name: String, def: Map<String, Any?>): FileSpec {
        val alias = TypeAliasSpec.builder(name, MAP.parameterizedBy(STRING, contextualAnyNullable))
            .addModifiers(KModifier.INTERNAL)
            .apply { kdoc(def)?.let { addKdoc("%L", it) } }
            .build()
        return FileSpec.builder(packageName, name).addFileComment(HEADER).addTypeAlias(alias)
            .build()
    }

    private fun buildMarkerClass(name: String, def: Map<String, Any?>): FileSpec {
        val marker = TypeSpec.classBuilder(name)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(serializable)
            .apply { kdoc(def)?.let { addKdoc("%L", it) } }
            .build()
        return fileOf(name, marker)
    }

    private fun baseType(schema: Map<String, Any?>): TypeName {
        (schema["\$ref"] as? String)?.let { return refType(it) }
        if (schema.containsKey("oneOf")) {
            return contextualAnyNullable.copy(nullable = false)
        }

        return when (primaryType(schema["type"])) {
            "string" -> STRING
            "integer" -> LONG
            "number" -> DOUBLE
            "boolean" -> BOOLEAN
            "array" -> {
                val items = asMap(schema["items"])
                LIST.parameterizedBy(baseType(items).copy(nullable = includesNull(items)))
            }
            "object" -> MAP.parameterizedBy(STRING, contextualAnyNullable)
            else -> contextualAnyNullable.copy(nullable = false)
        }
    }

    private fun refType(ref: String): ClassName =
        ClassName(packageName, ref.substringAfterLast('/'))

    private fun fileOf(name: String, type: TypeSpec): FileSpec =
        FileSpec.builder(packageName, name).addFileComment(HEADER).addType(type).build()

    private fun hasProperties(def: Map<String, Any?>): Boolean =
        asMap(def["properties"]).isNotEmpty()

    private fun isMapType(def: Map<String, Any?>): Boolean {
        val additional = def["additionalProperties"]
        return additional is Map<*, *> && additional.isNotEmpty()
    }

    private fun primaryType(typeNode: Any?): String? = when (typeNode) {
        is String -> typeNode
        is List<*> -> typeNode.firstOrNull { it != "null" } as? String
        else -> null
    }

    private fun includesNull(schema: Map<String, Any?>): Boolean {
        val type = schema["type"]
        if (type is List<*> && type.contains("null")) {
            return true
        }
        val oneOf = schema["oneOf"] as? List<*> ?: return false
        return oneOf.any { asMap(it)["type"] == "null" }
    }

    private fun kdoc(schema: Map<String, Any?>): String? {
        val description = (schema["description"] as? String)?.trim().orEmpty()
        if (description.isEmpty()) {
            return null
        }
        return description.replace("*/", "* /")
    }

    @Suppress("UNCHECKED_CAST")
    private fun asMap(value: Any?): Map<String, Any?> = value as? Map<String, Any?> ?: emptyMap()

    private fun asStringList(value: Any?): List<String> =
        (value as? List<*>).orEmpty().mapNotNull { it as? String }

    private companion object {
        const val HEADER =
            "Generated from the opentelemetry-configuration JSON schema. Do not edit manually."

        fun toCamelCase(name: String): String {
            val parts = name.split('_', '/', '-').filter { it.isNotEmpty() }
            if (parts.isEmpty()) {
                return name
            }
            return parts.first() + parts.drop(1).joinToString("") { part ->
                part.replaceFirstChar { it.uppercaseChar() }
            }
        }

        fun sanitizeEnumConstant(value: String): String =
            value.map { if (it.isLetterOrDigit()) it.uppercaseChar() else '_' }.joinToString("")
    }
}
