package com.prestongarno.apis.core

import com.prestongarno.apis.logging.logger


data class Metrics(
    val numAPIs: Int,
    val numEndpoints: Int,
    val numSpecs: Int) {

  companion object {

    private val log by logger()

    private val listeners = mutableListOf<(Metrics) -> Unit>()

    internal
    fun updateApiMetrics(metrics: Metrics) {
      log.debug("Updating global metrics value: $metrics")
      listeners.forEach { it(metrics) }
    }

    fun listen(block: (Metrics) -> Unit) =
        listeners.add(block)

    fun unsubscribe(block: (Metrics) -> Unit) =
        listeners.remove(block)
  }
}