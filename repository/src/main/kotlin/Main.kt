package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.graphql.GraphQlServer
import com.prestongarno.apis.logging.logger
import com.prestongarno.apis.net.NetworkClient
import com.prestongarno.apis.net.RemoteRepositoryImpl
import com.prestongarno.apis.persistence.LocalRepositoryImpl
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread

private object Main {
  val logger by this.logger()
}


fun main(args: Array<String>) {

  Logger.getGlobal().level = Level.ALL

  var controller: Controller? = null

  val init: (Controller) -> Unit = { controller = it }

  thread {
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
    }

    init(ctrl.start(refreshRate = Duration.ofHours(3L)))
  }

  val startTime = Instant.now()
  val endTime = startTime.plus(Duration.ofHours(3L))

  Main.logger.info("Started main thread @ ${Date(startTime.toEpochMilli())}")

  var lastUpdate = startTime

  while (Instant.now().isBefore(endTime)) {

    if (lastUpdate.plusSeconds(100L).isBefore(Instant.now())) {
      lastUpdate = Instant.now()
      Main.logger.info(Date.from(lastUpdate).toString() +
          " Local repository metrics: " + controller?.readWriteRepository?.getMetrics()?.toString())
      sampleGraphQlQuery(controller!!)
    }
  }

}


private fun sampleGraphQlQuery(controller: Controller) {
  for (i in 1..controller.readWriteRepository.getMetrics().numAPIs) {
    controller.graphqlQuery("""
      |{
      |  api(id: $i) {
      |    name,
      |    preferred,
      |    __typename,
      |    versions {
      |      name,
      |      id,
      |      updated
      |      swaggerUrl
      |    }
      |  }
      |}
      """.trimMargin("|"))
        .also(::println)
  }

}
