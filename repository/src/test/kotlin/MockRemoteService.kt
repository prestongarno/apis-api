package com.prestongarno.apis

import com.prestongarno.apis.logging.logger
import com.prestongarno.apis.net.NetworkClient
import com.prestongarno.apis.net.RemoteRepositoryImpl
import io.ktor.application.call
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.junit.After
import org.junit.Before
import java.io.File
import java.util.concurrent.TimeUnit

open class MockRemoteService {

  private val baseResourceDir = File("./src/test/resources")

  private val log by logger()

  val engine = embeddedServer(Netty, port = 8080) {
    routing {

      get("/") {
        call.respondText("404 Nothing Is Here")
      }

      get("/list.json") {
        call.respondFile(
            baseDir = baseResourceDir,
            fileName = "apis.json")
      }

      get("/metrics.json") {
        call.respondFile(
            baseDir = baseResourceDir,
            fileName = "metrics.json")
      }
    }
  }

  val remoteRepository: Repository = NetworkClient.builder()
      .endpoint("http://localhost:8080/")
      .build()
      .let(::RemoteRepositoryImpl)

  @Before fun startEmbeddedServer() {
    engine.start()
  }

  @After fun killEmbeddedServer() {
    engine.stop(gracePeriod = 1L, timeout = 10L, timeUnit = TimeUnit.MILLISECONDS)
  }
}