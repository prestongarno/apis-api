package com.prestongarno.apis.persistence

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.TransactionManager.Companion.manager


object JdbcConnection {

  var conn = Database.connect("jdbc:h2:mem:regular", "org.h2.Driver")

  private var root = TransactionManager.manager.newTransaction()

  init {
    restart()
  }

  private fun init() {
    root = TransactionManager.currentOrNew(manager.defaultIsolationLevel)
    //root.logger.addLogger(StdOutSqlLogger)
    root.logger.removeLogger(StdOutSqlLogger)
    SchemaUtils.create(ApiTable, ApiVersions, MetricsTable)
    root.commit()
  }

  fun restart() {
    TransactionManager.removeCurrent()
    root.close()
    root.connection.close()
    init()
  }
}
