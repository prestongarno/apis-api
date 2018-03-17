package com.prestongarno.apis

import com.prestongarno.apis.Main.log
import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.graphql.GraphQlServer
import com.prestongarno.apis.logging.logger
import com.prestongarno.apis.net.NetworkClient
import com.prestongarno.apis.net.RemoteRepositoryImpl
import com.prestongarno.apis.persistence.LocalRepositoryImpl
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

private object Main {
  val log by this.logger()
}


fun main(args: Array<String>) {

  LoggerFactory.getLogger("com.prestongarno")

  val localRepository: ReadWriteRepository =
      LocalRepositoryImpl()

  val remoteRepo = NetworkClient.builder()
      .endpoint("https://api.apis.guru/v2/")
      .build()
      .let(::RemoteRepositoryImpl)

  remoteRepo.getMetrics().also {
    Main.log.info("Successfully fetched remote metrics: $it")
  }

  var graphQlServer: GraphQlServer? = null

  Metrics.listen {
    log.info("Metrics daemon notified an update: $it")

    if (graphQlServer == null) graphQlServer = GraphQlServer(localRepository).also {
      server(it) {
        port = 8081
        host = "0.0.0.0"
        blockThread = false
        autoStart = true
      }
    }
  }

  Controller(localRepository, remoteRepo)
      .start(refreshRate = Duration.ofMinutes(3L))
}


fun server(
    gql: GraphQlServer,
    block: Server.Configuration.Builder.() -> Unit
) = Server(gql, Server.Configuration(block))
