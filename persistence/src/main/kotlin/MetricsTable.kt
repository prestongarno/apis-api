package com.prestongarno.apis.persistence

import com.prestongarno.apis.core.Metrics
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant


internal
object MetricsTable : Table() {


  init { JdbcConnection.conn }

  private val id = integer("id")
      .uniqueIndex()
      .autoIncrement()
      .primaryKey()

  private val updatedAt = date("updated_at")

  private val numApis = integer("num_apis")

  private val numEndpoints = integer("num_endpoints")

  private val numSpecs = integer("num_specs")


  fun getPersistentMetrics(): Metrics = selectAll().firstOrNull()?.let {
    Metrics(it[numApis], it[numEndpoints], it[numSpecs])
  } ?: Metrics(0,0,0)

  fun updateMetrics(metrics: Metrics) {
    transaction {
      deleteAll()
      commit()
    }
    transaction {
      insert {
        it[numApis] = metrics.numApis
        it[numEndpoints] = metrics.numEndpoints
        it[numSpecs] = metrics.numSpecs
        it[updatedAt] = org.joda.time.DateTime(Instant.now().toEpochMilli())
      }
    }
  }
}