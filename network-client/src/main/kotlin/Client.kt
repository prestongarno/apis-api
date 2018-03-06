package com.prestongarno.apis.net

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.core.entities.ApiVersion
import com.prestongarno.apis.logging.logger
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.Locale


class Client(private val networkClient: NetworkClient) {


  private val logger by logger()

  val httpClient = OkHttpClient.Builder()
      .addInterceptor {
        logger.info(it.request().toString())
        it.proceed(it.request())
      }
      .addInterceptor { interceptor ->
        interceptor.request().newBuilder()
            .also {
              for (header in networkClient.defaultHeaders)
                it.header(header.key, header.value)
            }
            .build()
            .let(interceptor::proceed)
      }
      .build()


  private val retrofitClient = httpClient
      .let(Retrofit.Builder()::client)
      .apply {
        HttpUrl.get(networkClient.endpoint)?.let(::baseUrl)
            ?: networkClient.endpoint.toString().let(::baseUrl)
      }
      .build()


  fun refreshMetrics(): Metrics? =
      httpClient.newCall(Request.Builder()
          .url(networkClient.endpoint.toURL().toString() + "metrics.json")
          .also {
            for ((name, value) in networkClient.defaultHeaders) it.header(name, value)
          }.build())
          .execute()
          .let {
            if (it.isSuccessful) it.body()
                ?.string()
                ?.let { metricsFromString(it) }
            else {
              it.apply {
                logger.warn("Unsuccessful metrics call:" +
                    "\n\tCode: ${code()}" +
                    "\n\tMessage: ${it.message()}" +
                    "\n\tError Message: ${it.message()?.toString()}")
              }
              null
            }
          }

  fun getAllRemoteApis(): List<Api> = httpClient.newCall(Request.Builder()
      .url(networkClient.endpoint.toURL().toString() + "list.json")
      .also { for ((name, value) in networkClient.defaultHeaders) it.header(name, value) }
      .build())
      .execute()
      .let { it.body()!!.charStream() }
      .let { Klaxon().parseJsonObject(it) }
      .let(::createFromMap)
}

@Suppress("UNCHECKED_CAST")
private fun createFromMap(map: JsonObject): List<Api> {
  return map.map { (name, values) ->
    name to values as? JsonObject
  }.mapNotNull {
    if (it.second is JsonObject) it as? Pair<String, JsonObject> else null
  }.map { (name, obj) ->
    Api(name, -1, obj.string("preferred"), obj.obj("versions").let(::toApiVersions))
  }
}

@Suppress("UNCHECKED_CAST")
private fun toApiVersions(obj: JsonObject?): List<ApiVersion> {
  obj ?: return emptyList()

  return obj.entries.filter {
    it.value is JsonObject
  }.map {
    it.key to it.value
  }.map {
    it as Pair<String, JsonObject>
  }.map { (name, obj) ->
    ApiVersion(
        name,
        parseDate(obj.string("added")!!).toEpochMilli(),
        obj.string("swaggerUrl") ?: "",
        obj.string("swaggerYamlUrl") ?: "",
        parseDate(obj.string("updated")!!).toEpochMilli())
  }
}

private fun parseDate(str: String): Instant {
  val format = SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  format.timeZone = TimeZone.getTimeZone("UTC")
  return format.parse(str).toInstant()
}

private fun metricsFromString(value: String) =
    Klaxon().parse<Metrics>(value) ?: Metrics(0, 0, 0)

