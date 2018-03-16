package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
import org.junit.Test


class ConfigurationTests : MockRemoteService() {

  @Test fun listOfApisFromLocalWorks() {
    require(remoteRepository.getAllApis().size > 100)
  }

  @Test fun getMetricsLocallyWorks() {
    require(remoteRepository.getMetrics() == Metrics(
        numAPIs = 1016,
        numEndpoints = 24805,
        numSpecs = 1419))
  }

  @Test fun confWorks() {
    Server.Configuration {
      port = 1080
      host = "hell"
    }.also { conf ->
      require(conf.port == 1080)
      require(conf.host == "hell")
    }
  }
}
