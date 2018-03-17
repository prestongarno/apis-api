package com.prestongarno.apis

import com.prestongarno.apis.core.ResourceManager
import com.prestongarno.apis.logging.logger
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.features.toLogString
import io.ktor.http.RequestConnectionPoint
import io.ktor.request.queryString
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.declaredMemberProperties


class Server(
    private val endpoint: GraphQlEndpoint,
    private val conf: Configuration = DEFAULT_CONFIG
) : AutoCloseable {

  private val log by logger()

  private val engine by lazy {

    embeddedServer(
        factory = Netty,
        port = conf.port,
        host = conf.host) {

      routing {
        post("/graphql") {
          val code = call.request.hashCode()

          log.info("#$code - " +
              call.request.toLogString() +
              "; LOCAL: " +
              call.request.origin.toLogString())

          call.respondText {
            val str = call.request.queryParameters.run {
              this["query"] ?: this["mutation"]
            } ?: call.request.queryString()
            endpoint.handleRequest(str).also {
              log.info("#$code completed.")
            }
          }
        }
      }
    }
  }

  init {
    ResourceManager.addShutdownHook(::close)
    if (conf.autoStart) start(conf.blockThread)
  }

  fun start(wait: Boolean = false) {
    log.info("Starting http server with configuration host='${conf.host}',port='${conf.port}'")
    engine.start(wait)
  }

  fun close(now: Boolean = false) {
    log.info("stopping http server" + if (now) " instantly" else " with grace period & timeout")

    if (!now) engine.stop(
        gracePeriod = 3L,
        timeout = 5L,
        timeUnit = TimeUnit.SECONDS)
    else engine.stop(0L, 0L, TimeUnit.NANOSECONDS)

    log.info("Successfully stopped http server.")
  }

  override fun close() {
    this.close(conf.closeImmediately)
  }


  class Configuration(builder: Builder) {

    constructor(block: Builder.() -> Unit) : this(Builder(block))

    val port: Int = builder.port
    val host: String = builder.host
    val autoStart: Boolean = builder.autoStart
    val blockThread: Boolean = builder.blockThread
    val closeImmediately = builder.closeImmediately

    override fun toString() =
        "Server.Configuration(host='$host', port='$port'"


    class Builder(block: Builder.() -> Unit) {

      var port: Int = 8081
      var host: String = "127.0.0.1"
      var autoStart: Boolean = false
      var blockThread: Boolean = false
      var closeImmediately = false

      init {
        this.block()
      }

    }
  }

  companion object {
    val DEFAULT_CONFIG = Configuration(Configuration.Builder { /*nothing*/ })
  }
}

private fun RequestConnectionPoint.toLogString() =
    RequestConnectionPoint::class.declaredMemberProperties
        .map { it.name to it(this) }
        .toMap()
        .toString()

