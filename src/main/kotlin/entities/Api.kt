package com.prestongarno.apis.core.entities

data class Api(val name: String, val id: Int = -1, val preferred: String?, val versions: List<ApiVersion>) {
  fun hasValidId() = id > 0
}

data class ApiVersion(
    val name: String,
    val added: Long,
    val info: ApiInfo,
    val id: Int = -1)

data class ApiInfo(val info: Map<String, Any?>)
