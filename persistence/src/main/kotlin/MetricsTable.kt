package com.prestongarno.apis.persistence

import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.logging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant


internal
object MetricsTable : Table() {

  private val log by logger()


  init {
    JdbcConnection.conn
  }

  private val id = integer("id")
      .uniqueIndex()
      .autoIncrement()
      .primaryKey()

  private val updatedAt = date("updated_at")

  private val numApis = integer("num_apis")

  private val numEndpoints = integer("num_endpoints")

  private val numSpecs = integer("num_specs")


  fun getPersistentMetrics(): Metrics = transaction {
    log.debug("Getting local metrics; transation: " + this.toString())
    selectAll()
        .also { log.debug("SQL statements: " + statements.toString()) }
        .firstOrNull()
        ?.let { Metrics(it[numApis], it[numEndpoints], it[numSpecs]) }
        ?: Metrics(0, 0, 0)
  }.also { log.debug("local metrics query result: $it") }

  fun updateMetrics(metrics: Metrics) {
    singleTransation({ deleteAll() })
    singleTransation {
      log.debug("Storing local metrics input: $metrics")
      log.debug("transation: " + this.toString())
      insert {
        it[numApis] = metrics.numAPIs
        it[numEndpoints] = metrics.numEndpoints
        it[numSpecs] = metrics.numSpecs
        it[updatedAt] = org.joda.time.DateTime(Instant.now().toEpochMilli())
      }
    }
  }
}

internal fun <T> singleTransation(block: Transaction.() -> T): T {
  val trans = TransactionManager.currentOrNew(TransactionManager.manager.defaultIsolationLevel)
  val result = trans.block()
  trans.commit()
  return result
}