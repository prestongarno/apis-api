package com.prestongarno.apis

import com.prestongarno.apis.core.ResourceManager
import com.prestongarno.apis.logging.logger
import io.ktor.application.call
import io.ktor.request.queryString
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.TimeUnit


class Server(private val endpoint: GraphQlEndpoint) : AutoCloseable {

  private val log by logger()

  private val engine by lazy {
    embeddedServer(Netty, port = 8081) {
      routing {
        post("/graphql") {
          call.respondText {
            endpoint.handleRequest(call.request.queryString())
          }
        }
      }
    }
  }

  init { ResourceManager.addShutdownHook(::close) }

  fun start(wait: Boolean = false) { engine.start(wait) }

  override fun close() {
    engine.stop(gracePeriod = 3L, timeout = 5L, timeUnit = TimeUnit.SECONDS)
  }
}
