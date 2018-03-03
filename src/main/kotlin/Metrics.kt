package com.prestongarno.apis.core

data class Metrics(
    val numApis: Int,
    val numEndpoints: Int,
    val numSpecs: Int) {

  companion object {

    private val listeners = mutableListOf<(Metrics) -> Unit>()

    @Volatile
    private var metrics: Metrics = Metrics(0, 0, 0)

    internal fun updateApiMetrics(metrics: Metrics) {
      if (metrics == this.metrics) return
      synchronized(this) { this.metrics = metrics }
      listeners.forEach { it(metrics) }
    }

    internal fun currentMetrics() = metrics

    fun listen(block: (Metrics) -> Unit) =
        listeners.add(block)

    fun unsubscribe(block: (Metrics) -> Unit) =
        listeners.remove(block)
  }
}