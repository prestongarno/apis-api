package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.SynchServiceComponent
import java.time.Duration


class Controller(
    val localRepository: LocalRepository,
    val remoteRepository: Repository) {


  val synchService by lazy(::SynchServiceComponent)

  init {
    Metrics.listen {
      if (!updateLocalDataFromRemoteRepository(it)) {
        println("Data saved incorrectly:\n\tremote metrics: " + // TODO logging
            "$it\n\tlocal metrics: ${localRepository.getMetrics()}")
      }
    }
  }

  fun start(refreshRate: Duration) {
    synchService.provideApiFetcher(remoteRepository::getAllApis)
    synchService.provideMetricFetcher(remoteRepository::getMetrics)
    synchService.updateEvery(refreshRate.toHours())
  }

  private
  fun updateLocalDataFromRemoteRepository(newMetrics: Metrics): Boolean {
    val newRepos = remoteRepository.getAllApis()
    localRepository.deleteAll()
    localRepository.updateOrCreateAll(newRepos)
    return localRepository.getMetrics() == newMetrics
  }
}