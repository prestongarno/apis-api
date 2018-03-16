package com.prestongarno.apis

import com.prestongarno.apis.core.ResourceManager
import com.prestongarno.apis.logging.logger
import io.ktor.application.call
import io.ktor.request.queryString
import io.ktor.request.receiveChannel
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.TimeUnit


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
          call.respondText {
            endpoint.handleRequest(call.request.queryString())
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

  override fun close() {
    log.info("stopping http server...")
    engine.stop(gracePeriod = 3L, timeout = 5L, timeUnit = TimeUnit.SECONDS)
    log.info("Successfully stopped http server.")
  }


  class Configuration(builder: Builder) {

    constructor(block: Builder.() -> Unit) : this(Builder(block))

    val port: Int = builder.port
    val host: String = builder.host
    val autoStart: Boolean = builder.autoStart
    val blockThread: Boolean = builder.blockThread

    override fun toString() =
        "Server.Configuration(host='$host', port='$port'"


    class Builder(block: Builder.() -> Unit) {

      var port: Int = 8081
      var host: String = "http://127.0.0.1"
      var autoStart: Boolean = false
      var blockThread: Boolean = false

      init { this.block() }

    }
  }

  companion object {
    val DEFAULT_CONFIG = Configuration(Configuration.Builder { /*nothing*/ })
  }
}
