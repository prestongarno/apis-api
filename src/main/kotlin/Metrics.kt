package com.prestongarno.apis.core

import com.prestongarno.apis.logging.logger


data class Metrics(
    val numApis: Int,
    val numEndpoints: Int,
    val numSpecs: Int) {

  companion object {

    private val log by logger()

    private val listeners = mutableListOf<(Metrics) -> Unit>()

    @Volatile
    private var metrics: Metrics = Metrics(0, 0, 0)

    internal
    fun updateApiMetrics(metrics: Metrics) {

      if (metrics == this.metrics) return

      log.debug("Updating global metrics value: $metrics")
      synchronized(this) { this.metrics = metrics }
      listeners.forEach { it(metrics) }
    }

    internal
    fun currentMetrics() = metrics

    fun listen(block: (Metrics) -> Unit) =
        listeners.add(block)

    fun unsubscribe(block: (Metrics) -> Unit) =
        listeners.remove(block)
  }
}