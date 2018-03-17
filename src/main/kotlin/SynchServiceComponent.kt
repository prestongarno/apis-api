package com.prestongarno.apis.core

import com.prestongarno.apis.logging.logger
import java.time.Duration
import java.util.*
import kotlin.concurrent.timer


class SynchServiceComponent {

  val INITIAL_REFRESH_RATE = Duration.ofSeconds(10L)

  val log by this.logger()

  @Volatile
  private var apiFetcher: ApiFetcher = ::emptyList

  @Volatile
  private var metricFetcher: MetricFetcher = { Metrics(0, 0, 0) }

  @Volatile
  private var localMetricFetcher: MetricFetcher = { Metrics(0, 0, 0) }

  private var metricFetchTimer: Timer =
      createTimer(INITIAL_REFRESH_RATE) { /* nothing */ }

  init {
    updateEvery(INITIAL_REFRESH_RATE)
    ResourceManager.addShutdownHook {
      log.info("Attempting to shut down " + metricFetchTimer.toString())
      metricFetchTimer.purge()
      metricFetchTimer.cancel()
      log.info("Successfully shut down " + metricFetchTimer.toString())
    }
  }

  fun provideApiFetcher(apiFetcher: ApiFetcher) = apply {
    synchronized(this) { this.apiFetcher = apiFetcher }
  }

  fun provideMetricFetcher(metricFetcher: MetricFetcher) = apply {
    synchronized(this) { this.metricFetcher = metricFetcher }
  }

  fun provideLocalMetricFetcher(metricFetcher: MetricFetcher) = apply {
    synchronized(this) { this.localMetricFetcher = metricFetcher }
  }

  fun updateEvery(duration: Duration) = apply {
    log.info("Updating metrics every ${duration.seconds} seconds")
    metricFetchTimer.purge()
    metricFetchTimer.cancel()
    metricFetchTimer = createTimer(duration) {
      val current = localMetricFetcher()
      val remoteMetrics = metricFetcher()
      if (remoteMetrics != current) Metrics.updateApiMetrics(remoteMetrics)
    }
  }

  private fun createTimer(refreshRate: Duration = INITIAL_REFRESH_RATE, task: TimerTask.() -> Unit): Timer =
      timer(name = SynchServiceComponent::metricFetchTimer.name + "-0",
          daemon = false,
          period = refreshRate.toMillis(),
          action = task)
}