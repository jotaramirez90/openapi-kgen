package com.kroegerama.kgen.poet

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

object PoetConstants {

    private const val PKG_GSON = "com.google.gson.annotations"
    private const val PKG_RETROFIT = "retrofit2"
    private const val PKG_RETROFIT_HTTP = "retrofit2.http"
    private const val PKG_OK = "okhttp3"

    val GSON_SERIALIZED = ClassName(PKG_GSON, "SerializedName")

    val RETROFIT_GET = ClassName(PKG_RETROFIT_HTTP, "GET")
    val RETROFIT_POST = ClassName(PKG_RETROFIT_HTTP, "POST")
    val RETROFIT_PUT = ClassName(PKG_RETROFIT_HTTP, "PUT")
    val RETROFIT_DELETE = ClassName(PKG_RETROFIT_HTTP, "DELETE")
    val RETROFIT_PATCH = ClassName(PKG_RETROFIT_HTTP, "PATCH")
    val RETROFIT_HEAD = ClassName(PKG_RETROFIT_HTTP, "HEAD")
    val RETROFIT_OPTIONS = ClassName(PKG_RETROFIT_HTTP, "OPTIONS")
    val RETROFIT_TRACE = ClassName(PKG_RETROFIT_HTTP, "TRACE")

    val RETROFIT_MULTIPART = ClassName(PKG_RETROFIT_HTTP, "Multipart")
    val RETROFIT_FORM_ENCODED = ClassName(PKG_RETROFIT_HTTP, "FormUrlEncoded")
    val RETROFIT_HEADERS = ClassName(PKG_RETROFIT_HTTP, "Headers")

    val RETROFIT_PART = ClassName(PKG_RETROFIT_HTTP, "Part")
    val RETROFIT_FIELD = ClassName(PKG_RETROFIT_HTTP, "Field")
    val RETROFIT_BODY = ClassName(PKG_RETROFIT_HTTP, "Body")

    val RETROFIT_PARAM_HEADER = ClassName(PKG_RETROFIT_HTTP, "Header")
    val RETROFIT_PARAM_PATH = ClassName(PKG_RETROFIT_HTTP, "Path")
    val RETROFIT_PARAM_QUERY = ClassName(PKG_RETROFIT_HTTP, "Query")

    val RETROFIT_RESPONSE = ClassName(PKG_RETROFIT, "Response")

    val OK_REQUEST_BODY = ClassName(PKG_OK, "RequestBody")
    val OK_RESPONSE_BODY = ClassName(PKG_OK, "ResponseBody")
    val OK_MULTIPART_PART = ClassName(PKG_OK, "MultipartBody", "Part")

    val LIST_OF = MemberName("kotlin.collections", "listOf")
}