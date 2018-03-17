package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
import org.junit.Test


// TODO use HTTP server test env so that doesn't take so long to run
class ConfigurationTests : MockRemoteService() {
  @Test fun getMetricsLocallyWorks() {
    require(remoteRepository.getAllApis().size > 100)
    require(remoteRepository.getMetrics() == Metrics(
        numAPIs = 1016,
        numEndpoints = 24805,
        numSpecs = 1419))
    Server.Configuration {
      port = 1080
      host = "hell"
    }.also { conf ->
      require(conf.port == 1080)
      require(conf.host == "hell")
    }
  }
}
