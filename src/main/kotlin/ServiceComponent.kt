package com.prestongarno.apis.core

import java.time.Duration
import java.util.*
import kotlin.concurrent.timerTask


class ServiceComponent {

  @Volatile
  private var apiFetcher: ApiFetcher = ::emptyList

  @Volatile
  private var metricFetcher: MetricFetcher = { Metrics(0, 0, 0) }

  private val metricFetchTimer: Timer by lazy {
    kotlin.concurrent.timer(
        name = ServiceComponent::metricFetchTimer.let {
          ServiceComponent::class.qualifiedName ?: ""+".${it.name}"
        },
        daemon = true,
        period = 0,
        action = { })
  }

  init {
    updateEvery(1L)
    ResourceManager.addShutdownHook {
      metricFetchTimer.purge()
      metricFetchTimer.cancel()
    }
  }

  fun provideApiFetcher(apiFetcher: ApiFetcher) = apply {
    synchronized(this) { this.apiFetcher = apiFetcher }
  }

  fun provideMetricFetcher(metricFetcher: MetricFetcher) = apply {
    synchronized(this) { this.metricFetcher = metricFetcher }
  }

  fun updateEvery(hours: Long) = apply {
    metricFetchTimer.purge()
    metricFetchTimer.schedule(timerTask {
      Metrics.updateApiMetrics(metricFetcher())
    }, 1L, Duration.ofHours(hours).toMillis())
  }
}