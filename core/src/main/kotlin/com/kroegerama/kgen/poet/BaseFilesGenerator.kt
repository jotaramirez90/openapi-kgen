package com.kroegerama.kgen.poet

import com.kroegerama.kgen.OptionSet
import com.kroegerama.kgen.Util
import com.kroegerama.kgen.asBaseUrl
import com.kroegerama.kgen.openapi.OpenAPIAnalyzer
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.swagger.v3.oas.models.OpenAPI

interface IBaseFilesGenerator {
    fun getBaseFiles(): List<FileSpec>
}

class BaseFilesGenerator(
    openAPI: OpenAPI,
    options: OptionSet,
    analyzer: OpenAPIAnalyzer
) : IBaseFilesGenerator,
    IPoetGeneratorBase by PoetGeneratorBase(openAPI, options, analyzer) {

    override fun getBaseFiles() = listOf(getMetadataFile())

    private fun getMetadataFile() = prepareFileSpec(options.packageName, "Metadata") {
        val tBuildConfig = poetObject(ClassName(options.packageName, "ApiBuildConfig")) {
            val info = openAPI.info
            addProperty(
                poetProperty("API_VERSION", STRING, KModifier.CONST) {
                    addKdoc("Value of **OpenAPI.info.version**")
                    initializer("%S", info.version)
                }
            )
            addProperty(
                poetProperty("API_TITLE", STRING, KModifier.CONST) {
                    addKdoc("Value of **OpenAPI.info.title**")
                    initializer("%S", info.title)
                }
            )
            addProperty(
                poetProperty("API_DESCRIPTION", STRING.nullable(true)) {
                    addKdoc("Value of **OpenAPI.info.description**")
                    initializer("%S", info.description)
                }
            )
            addProperty(
                poetProperty("GEN_FORMATTED", STRING, KModifier.CONST) {
                    addKdoc("Time of code generation. Formatted as **RFC 1123** date time.")
                    initializer("%S", Util.formattedDateTime)
                }
            )
            addProperty(
                poetProperty("GEN_TIMESTAMP", LONG, KModifier.CONST) {
                    addKdoc("Time of code generation. **Unix timestamp** in seconds since 1970-01-01T00:00:00Z.")
                    initializer("%L", Util.unixDateTime)
                }
            )
        }

        val serverList = poetProperty("serverList", LIST.parameterizedBy(STRING)) {
            val servers = openAPI.servers

            val block = CodeBlock.builder().apply {
                servers.forEachIndexed { index, server ->
                    val baseUrl = server.url.asBaseUrl()
                    add("%S", baseUrl)
                    if (index < servers.size - 1) add(", ")
                }
            }.build()

            initializer("%M(%L)", PoetConstants.LIST_OF, block)
        }

        addType(tBuildConfig)
        addProperty(serverList)
    }
}