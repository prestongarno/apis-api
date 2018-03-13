package com.prestongarno.apis.core.entities

data class Api(
    val name: String,
    val id: Int = -1,
    val preferred: String?,
    val versions: List<ApiVersion>) {

  fun hasValidId() = id > 0
}

data class ApiVersion(
    val name: String,
    val added: Long,
    val swaggerUrl: String,
    val swaggerYamlUrl: String,
    val updated: Long,
    //val info: ApiInfo,
    val id: Int = -1)

// TODO add logos, contact info, etc in these classes
data class Foo(
    val title: String,
    val description: String,
    val contact: ContactInfo?,
    val logo: Logo?)

data class ContactInfo(
    val email: String,
    val name: String,
    val url: String)

data class Logo(val url: String)
