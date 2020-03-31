package com.kroegerama.kgen.poet

import com.kroegerama.kgen.OptionSet
import com.kroegerama.kgen.asFileHeader
import com.kroegerama.kgen.openapi.OpenAPIAnalyzer
import com.kroegerama.kgen.openapi.ParameterType
import com.kroegerama.kgen.openapi.mapToName
import com.kroegerama.kgen.openapi.mapToTypeName
import com.kroegerama.kgen.sanitizePath
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem

interface IPoetGeneratorBase {
    val openAPI: OpenAPI
    val options: OptionSet
    val analyzer: OpenAPIAnalyzer

    fun prepareFileSpec(pkg: String, name: String, block: FileSpec.Builder.() -> Unit): FileSpec
    fun createParameterAnnotation(parameterType: ParameterType, name: String): AnnotationSpec
    fun createPartAnnotation(name: String?): AnnotationSpec
    fun createFieldAnnotation(name: String): AnnotationSpec
    fun createHttpMethodAnnotation(method: PathItem.HttpMethod, path: String): AnnotationSpec
    fun createJsonAnnotation(name: String): AnnotationSpec
}

class PoetGeneratorBase(
    override val openAPI: OpenAPI,
    override val options: OptionSet,
    override val analyzer: OpenAPIAnalyzer
) : IPoetGeneratorBase {

    private fun FileSpec.Builder.addHeader(): FileSpec.Builder {
        addComment("%L", openAPI.info.asFileHeader())
        return this
    }

    override fun prepareFileSpec(
        pkg: String,
        name: String,
        block: FileSpec.Builder.() -> Unit
    ): FileSpec =
        poetFile(pkg, name) {
            indent(" ".repeat(4))
            addHeader()
            apply(block)
        }

    override fun createParameterAnnotation(parameterType: ParameterType, name: String) =
        poetAnnotation(parameterType.mapToTypeName()) {
            addMember("%S", name)
        }

    override fun createPartAnnotation(name: String?) =
        poetAnnotation(PoetConstants.RETROFIT_PART) {
            name?.let { addMember("%S", it) }
        }

    override fun createFieldAnnotation(name: String) =
        poetAnnotation(PoetConstants.RETROFIT_FIELD) {
            addMember("%S", name)
        }

    override fun createHttpMethodAnnotation(method: PathItem.HttpMethod, path: String) =
        poetAnnotation(method.mapToName()) {
            addMember("%S", path.sanitizePath())
        }

    override fun createJsonAnnotation(name: String) =
        poetAnnotation(PoetConstants.GSON_SERIALIZED) {
            addMember("%S", name)
        }
}