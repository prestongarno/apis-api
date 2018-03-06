package com.prestongarno.apis.core

import com.prestongarno.apis.logging.logger
import java.time.Duration
import java.util.*
import kotlin.concurrent.timerTask


class SynchServiceComponent {

  val INITIAL_REFRESH_RATE = Duration.ofSeconds(10L)

  val log by this.logger()

  @Volatile
  private var apiFetcher: ApiFetcher = ::emptyList

  @Volatile
  private var metricFetcher: MetricFetcher = { Metrics(0, 0, 0) }

  @Volatile
  private var localMetricFetcher: MetricFetcher = { Metrics(0, 0, 0) }

  private val metricFetchTimer: Timer by lazy {
    kotlin.concurrent.timer(
        name = SynchServiceComponent::metricFetchTimer.let {
          SynchServiceComponent::class.qualifiedName ?: ""+".${it.name}"
        },
        daemon = true,
        period = INITIAL_REFRESH_RATE.toMillis(),
        action = { })
  }

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
    metricFetchTimer.purge()
    metricFetchTimer.scheduleAtFixedRate(
        timerTask {
          val current = localMetricFetcher()
          val remoteMetrics = metricFetcher()
          if (remoteMetrics != current) Metrics.updateApiMetrics(remoteMetrics)
        },
        1L, duration.toMillis()
    )
  }
}