package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.SynchServiceComponent
import com.prestongarno.apis.logging.logger
import java.time.Duration


class Controller(
    val readWriteRepository: ReadWriteRepository,
    private val remoteRepository: Repository) {


  val syncService by lazy(::SynchServiceComponent)

  @Volatile
  private var graphqlEndpoint: GraphQlEndpoint? = null

  private val log by logger()

  fun start(refreshRate: Duration = syncService.INITIAL_REFRESH_RATE) = apply {

    syncService.provideApiFetcher(remoteRepository::getAllApis)
        .provideMetricFetcher(remoteRepository::getMetrics)
        .provideLocalMetricFetcher(readWriteRepository::getMetrics)

    Metrics.listen {
      log.debug("Metrics listener (changed metrics: $it)")
      if (!updateLocalDataFromRemoteRepository(it)) {
        log.warn("Data saved incorrectly:\n\tremote metrics: $it\n\tlocal metrics: ${readWriteRepository.getMetrics()}")
      }
    }
    syncService.updateEvery(refreshRate)
  }

  fun useRemoteEndpoint(endpoint: GraphQlEndpoint) {
    synchronized(this) { this.graphqlEndpoint = endpoint }
  }

  fun graphqlQuery(query: String) = try {
    graphqlEndpoint?.handleRequest(query)!!
  } catch (ex: Exception) {
    log.warn("Error fetching graphql: " + ex.message)
    "{\"data\": {}, \"errors\": { \"message\": \"${ex.message}\" }"
  }

  private
  fun updateLocalDataFromRemoteRepository(newMetrics: Metrics): Boolean {
    log.debug("Updating local data from remote because of metrics: $newMetrics")
    readWriteRepository.updateMetrics(newMetrics)
    val newRepos = remoteRepository.getAllApis()
    readWriteRepository.deleteAll()
    readWriteRepository.updateOrCreateAll(newRepos)
    return readWriteRepository.getMetrics() == newMetrics
  }
}
