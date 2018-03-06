package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
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

  var foo: Controller? = null

  val init: (Controller) -> Unit = { foo = it }

  val mainThread = thread {
    val localRepository: ReadWriteRepository =
        LocalRepositoryImpl()

    val remoteRepo = NetworkClient.builder()
        .endpoint("https://api.apis.guru/v2/")
        .build()
        .let(::RemoteRepositoryImpl)

    remoteRepo.getMetrics().also {
      Main.logger.info("Fetched metrics remotely: $it")
    }

    Metrics.listen {
      Main.logger.info("Metrics daemon notified an update: $it")
    }

    val controller = Controller(localRepository, remoteRepo)
        .start(refreshRate = Duration.ofSeconds(10L))

    init(controller)
  }

  val startTime = Instant.now()
  val endTime = startTime.plus(Duration.ofSeconds(20))

  Main.logger.info("Started main thread @ ${Date(startTime.toEpochMilli())}")

  var lastUpdate = startTime

  while (Instant.now().isBefore(endTime)) {

    if (lastUpdate.plusSeconds(10L).isBefore(Instant.now())) {
      lastUpdate = Instant.now()
      Main.logger.info(Date.from(lastUpdate).toString() +
      " Local repository metrics: " + foo?.readWriteRepository?.getMetrics()?.toString()
          ?: "NO CONTROLLER")
      val localCount = foo?.readWriteRepository?.getAllApis()?.count() ?: 0
      Main.logger.info(" Local repository count: $localCount")
    }

  }

  foo!!.readWriteRepository.getAllApis().forEach {
    println("${it.id} ${it.name} -> " + it.versions.joinToString(separator = "\n\t") {
      val d = Instant.ofEpochMilli(it.added)
      "${it.id} ${it.name} ${Date.from(d)}"
    })
  }
}
