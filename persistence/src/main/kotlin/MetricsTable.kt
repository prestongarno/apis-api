package com.prestongarno.apis.persistence

import com.prestongarno.apis.core.Metrics
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant


internal
object MetricsTable : Table() {

  private val id = integer("id")
      .uniqueIndex()
      .autoIncrement()
      .primaryKey()

  private val updatedAt = date("updated_at")

  private val numApis = integer("num_apis")

  private val numEndpoints = integer("num_endpoints")

  private val numSpecs = integer("num_specs")


  fun getPersistentMetrics(): Metrics = selectAll().first().let {
    Metrics(it[numApis], it[numEndpoints], it[numSpecs])
  }

  fun updateMetrics(metrics: Metrics) {
    transaction {
      update({ id eq 0 }) {
        it[updatedAt] = org.joda.time.DateTime(Instant.now().toEpochMilli())
        it[numApis] = metrics.numApis
        it[numEndpoints] = metrics.numEndpoints
        it[numSpecs] = metrics.numSpecs
      }
    }
  }
}