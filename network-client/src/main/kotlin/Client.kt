package com.prestongarno.apis.net

import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.logging.logger
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.reflect.KProperty


class Client(networkClient: NetworkClient) {


  private val logger by logger()

  private val retrofitClient = OkHttpClient.Builder()
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
      .let(Retrofit.Builder()::client)
      .apply {
        HttpUrl.get(networkClient.endpoint)?.let(::baseUrl)
            ?: networkClient.endpoint.toString().let(::baseUrl)
      }
      .addConverterFactory(Moshi.Builder()
          .add(MetricsAdapter())
          .build()
          .let(MoshiConverterFactory::create)
      ).build()


  fun refreshMetrics(): Metrics? =
      retrofitClient.create(MetricsCall::class.java)
          .getMetrics()
          .execute().let {
            if (it.isSuccessful) it.body().also {
              logger.info("Refresh metrics: $it")
            } else {
              it.apply {
                logger.warn("Unsuccessful metrics call:" +
                    "\n\tCode: ${code()}" +
                    "\n\tMessage: ${it.message()}" +
                    "\n\tError Message: ${it.errorBody()?.toString()}")
              }
              null
            }
          }
}

private class MetricsAdapter : JsonAdapter<Metrics>() {

  private val logger by this.logger()

  @FromJson
  override fun fromJson(reader: JsonReader?): Metrics? {
    return try {
      reader?.let {
        var apiCount: Int = 0
        var endpointsCount: Int = 0
        var specCount: Int = 0
        it.beginObject()
        while (it.hasNext()) {
          val name = it.nextName()
          when (name) {
            "numAPIs" -> apiCount = it.nextInt()
            Metrics::numEndpoints.name -> endpointsCount = it.nextInt()
            Metrics::numSpecs.name -> specCount = it.nextInt()
            else -> Unit
          }
        }
        Metrics(
            numApis = apiCount,
            numEndpoints = endpointsCount,
            numSpecs = specCount)
            .also { logger.info("Parsed metrics: $it") }
      }
    } catch (ex: Exception) {
      logger.warn("Error parsing metrics: ${ex.message}")
      null
    }
  }

  @ToJson
  override fun toJson(writer: JsonWriter?, value: Metrics?) {
    writer ?: return
    value ?: return
    writer.name("numAPIs")
        .value(value.numApis)
    writer.name(Metrics::numEndpoints)
        .value(value.numEndpoints)
    writer.name(Metrics::numSpecs)
        .value(value.numSpecs)
  }
}

fun JsonWriter.name(property: KProperty<*>) =
    apply { name(property.name) }
