package com.prestongarno.apis.core.entities

data class Api(val preferred: String?, val versions: List<ApiVersion>)

data class ApiVersion(
    val name: String,
    val added: java.time.Instant,
    val info: ApiInfo
)

data class ApiInfo(val values: Map<String, Any?>)
