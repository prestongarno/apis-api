package com.prestongarno.apis.net

import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.core.entities.ApiInfo
import com.prestongarno.apis.core.entities.ApiVersion
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import retrofit2.Call
import retrofit2.http.GET
import java.text.DateFormat


interface ApiDataCall {

  @GET("/v2/list.json")
  fun getApisFromRemote(): Call<Token>
}

class Token(val values: List<Api>) {
  init {
    println("")
  }
}

class ApiAdapter : JsonAdapter<Token>() {

  @ToJson override fun toJson(writer: JsonWriter?, value: Token?) {
    TODO("not implemented")
  }

  @Suppress("UNCHECKED_CAST")
  @FromJson override fun fromJson(reader: JsonReader?): Token? {
    reader ?: return null
    val next = reader.readJsonValue()
    val values = next as? Map<String, Any?> ?: return null
    return values.entries.mapNotNull { (name, value) ->
      val fields = value as? Map<String, Any> ?: return@mapNotNull null
      Api(name = name,
          preferred = fields["preferred"].toString(),
          versions = (fields["versions"] as? Map<String, Any?>)
              ?.let(this::toApiVersion)
              ?: emptyList())
    }.let(::Token)
  }

  @Suppress("UNCHECKED_CAST")
  private fun toApiVersion(values: Map<String, Any?>): List<ApiVersion> =
      values.filter { (_, values) ->
        values is Map<*, *>
      }.map { (name, value) ->
        val map = value as Map<String, Any?>
        val fuckThis = map["info"] as? Map<String, Any?> ?: emptyMap()
        ApiVersion(name, toLongInstant(map["added"].toString()), ApiInfo(fuckThis))
      }
}

private fun toLongInstant(str: String): Long {
  return try {
    DateFormat.getInstance().parse(str).toInstant().toEpochMilli()
  } catch (ex: Exception) {
    0L
  }
}
