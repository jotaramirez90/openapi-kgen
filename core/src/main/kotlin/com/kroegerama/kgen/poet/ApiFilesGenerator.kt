package com.kroegerama.kgen.poet

import com.kroegerama.kgen.Constants
import com.kroegerama.kgen.OptionSet
import com.kroegerama.kgen.language.asClassFileName
import com.kroegerama.kgen.language.asFieldName
import com.kroegerama.kgen.language.asFunctionName
import com.kroegerama.kgen.language.asTypeName
import com.kroegerama.kgen.model.OperationWithInfo
import com.kroegerama.kgen.model.ResponseInfo
import com.kroegerama.kgen.model.SchemaWithMime
import com.kroegerama.kgen.openapi.OpenAPIAnalyzer
import com.kroegerama.kgen.openapi.OperationRequestType
import com.kroegerama.kgen.openapi.mapMimeToRequestType
import com.kroegerama.kgen.openapi.mapToParameterType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.parameters.Parameter

interface IApiFilesGenerator {
    fun getApiFiles(): List<FileSpec>
}

class ApiFilesGenerator(
    openAPI: OpenAPI,
    options: OptionSet,
    analyzer: OpenAPIAnalyzer
) : IApiFilesGenerator,
    IPoetGeneratorBase by PoetGeneratorBase(openAPI, options, analyzer),
    IPoetGeneratorSchemaHandler by PoetGeneratorSchemaHandler(openAPI, options, analyzer) {

    override fun getApiFiles(): List<FileSpec> = analyzer.apis.map { (name, operations) ->
        val apiName = "$name api"
        prepareFileSpec(options.apiPackage, apiName.asClassFileName()) {
            val className = ClassName(options.apiPackage, apiName.asTypeName())
            val apiInterface = poetInterface(className) {
                operations.forEach { opInfo ->
                    handleOperationInfo(this@poetInterface, opInfo)
                }
            }
            addType(apiInterface)
        }
    }

    private fun handleOperationInfo(
        apiInterface: TypeSpec.Builder,
        operationInfo: OperationWithInfo
    ) {
        val funName = operationInfo.createOperationName().asFunctionName()
        val request = operationInfo.getRequest()
        val response = operationInfo.getResponse()

        val baseParameters = collectParameters(operationInfo)
        val additionalParams = request?.let { getAdditionalParameters(it) }.orEmpty()

        val ifaceFun = poetFunSpec("$funName") {
            val methodAnnotation =
                createHttpMethodAnnotation(operationInfo.method, operationInfo.path)
            addAnnotation(methodAnnotation)

            if (operationInfo.securityNames.isNotEmpty()) {
                val cnInterceptor = ClassName(options.packageName, "ApiAuthInterceptor")
                val mnAuthHeader = MemberName(cnInterceptor, Constants.AUTH_HEADER_NAME)

                val secHeader = poetAnnotation(PoetConstants.RETROFIT_HEADERS) {
                    operationInfo.securityNames.forEach { name ->
                        //val secStr = "${Constants.AUTH_HEADER_VALUE}: ${scheme.name}"
                        val block = buildCodeBlock {
                            add("\${%M}: %L", mnAuthHeader, name)
                        }

                        addMember("\"%L\"", block)
                    }
                }
                addAnnotation(secHeader)
            }

            when (request?.mime) {
                Constants.MIME_TYPE_MULTIPART_FORM_DATA -> addAnnotation(PoetConstants.RETROFIT_MULTIPART)
                Constants.MIME_TYPE_URL_ENCODED -> addAnnotation(PoetConstants.RETROFIT_FORM_ENCODED)
            }

            addModifiers(KModifier.SUSPEND, KModifier.ABSTRACT)
            addParameters(baseParameters.map { it.ifaceParam })
            addParameters(additionalParams.map { it.ifaceParam })
            addReturns(response, false)
        }

        apiInterface.addFunction(ifaceFun)
    }

    private fun collectParameters(operationInfo: OperationWithInfo) =
        operationInfo.operation.parameters.orEmpty()
            .filter { it.name.asFieldName() != "accessToken" }
            .map { parameter ->
                createParameterSpecPair(parameter)
            }

    private fun createParameterSpecPair(parameter: Parameter): ParameterSpecPair {
        val rawName = parameter.name
        val name = rawName.asFieldName()
        val schema = parameter.schema
        val paramType = parameter.mapToParameterType()

        val type = analyzer.findNameFor(schema).let { typeName ->
            if (parameter.required) {
                typeName
            } else {
                typeName.copy(nullable = true)
            }
        }

        val ifaceParam = poetParameter(name, type) {
            addAnnotation(createParameterAnnotation(paramType, rawName))
        }
        val delegateParam = poetParameter(name, type) {
            //TODO add schema.default as defaultValue
            parameter.description?.let {
                addKdoc("%L", it)
            }
            if (!parameter.required) {
                defaultValue("null")
            }
        }
        return ParameterSpecPair(ifaceParam, delegateParam)
    }

    private fun getAdditionalParameters(request: SchemaWithMime): List<ParameterSpecPair> {
        val (mime, required, schema) = request
        return when (mime.mapMimeToRequestType()) {
            OperationRequestType.Default -> {
                val typeName = analyzer.findNameFor(schema)
                val ifaceBodyParam = poetParameter("body", typeName.nullable(!required)) {
                    addAnnotation(PoetConstants.RETROFIT_BODY)
                }
                val delegateBodyParam = poetParameter("body", typeName.nullable(!required)) {
                    if (!required) defaultValue("null")
                    addAnnotation(PoetConstants.RETROFIT_BODY)
                }
                listOf(ParameterSpecPair(ifaceBodyParam, delegateBodyParam))
            }
            OperationRequestType.Multipart -> {
                schema.convertToParameters(required, true)
            }
            OperationRequestType.UrlEncoded -> {
                schema.convertToParameters(required, false)
            }
            OperationRequestType.Unknown -> {
                //TODO!!
                emptyList()
            }
        }
    }

    private fun FunSpec.Builder.addReturns(responseInfo: ResponseInfo?, withDescription: Boolean) {
        responseInfo?.let { (_, description, schemaWithMime) ->
            val descriptionBlock = if (withDescription)
                CodeBlock.Builder().apply {
                    description?.let { add("%L", it) }
                }.build() else CodeBlock.builder().build()

            schemaWithMime?.let { (mime, _, schema) ->
                val typeName = analyzer.findNameFor(schema)
                val responseType = PoetConstants.RETROFIT_RESPONSE.parameterizedBy(typeName)

                if (mime == Constants.MIME_TYPE_JSON) {
                    returns(responseType, descriptionBlock)
                } else {
                    returns(PoetConstants.OK_RESPONSE_BODY, descriptionBlock)
                }
            } ?: returns(PoetConstants.RETROFIT_RESPONSE.parameterizedBy(UNIT), descriptionBlock)
        }
    }
}