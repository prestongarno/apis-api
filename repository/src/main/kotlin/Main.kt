package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.graphql.GraphQlServer
import com.prestongarno.apis.logging.logger
import com.prestongarno.apis.net.NetworkClient
import com.prestongarno.apis.net.RemoteRepositoryImpl
import com.prestongarno.apis.persistence.LocalRepositoryImpl
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

private object Main {
  val logger by this.logger()
}


fun main(args: Array<String>) {

  Logger.getGlobal().level = Level.INFO


  val localRepository: ReadWriteRepository =
      LocalRepositoryImpl()

  val remoteRepo = NetworkClient.builder()
      .endpoint("https://api.apis.guru/v2/")
      .build()
      .let(::RemoteRepositoryImpl)

  remoteRepo.getMetrics()
      .also { Main.logger.info("Fetched remote metrics: $it") }

  val ctrl = Controller(localRepository, remoteRepo)
  var graphQlServer: GraphQlServer? = null

  Metrics.listen {
    Main.logger.info("Metrics daemon notified an update: $it")
    if (graphQlServer == null) graphQlServer = GraphQlServer(localRepository)
        .also(ctrl::useRemoteEndpoint)
        .also {
          val s = Server(it)
          s.start(wait = false)
        }
  }

  ctrl.start(refreshRate = Duration.ofMinutes(3L))
}

