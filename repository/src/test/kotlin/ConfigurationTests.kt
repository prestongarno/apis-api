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
}
