package com.prestongarno.apis.net

import org.junit.Test


class MetricsTest {

  @Test fun metricsCallIsSuccessful() {

    val remoteRepo = NetworkClient.builder()
        .endpoint("https://api.apis.guru/v2/")
        .build()
        .let(::RemoteRepositoryImpl)

    remoteRepo.getMetrics().also {
      println(it)
      require(it.numAPIs > 0)
    }
  }
}