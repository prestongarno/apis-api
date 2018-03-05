package com.prestongarno.apis.persistence

import com.prestongarno.apis.ReadWriteRepository
import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.logging.logger
import org.jetbrains.exposed.sql.deleteWhere


class LocalRepositoryImpl : ReadWriteRepository {

  init {
    JdbcConnection.conn
    MetricsTable.updateMetrics(Metrics(0,0,0))
  }

  val log by logger()

  override fun getAllApis() = ApiTable.all()

  override fun getMetrics(): Metrics {
    val persistentMetrics = MetricsTable.getPersistentMetrics()
    log.debug("Got local metrics from table: $persistentMetrics")
    return persistentMetrics
  }

  override fun updateOrCreateApi(api: Api): Api =
      ApiTable.put(api)

  override fun updateOrCreateAll(apis: Iterable<Api>): Iterable<Api> =
      apis.map(ApiTable::put)

  override fun deleteApi(api: Api) {
    ApiTable.deleteWhere { ApiTable.id eq api.id }
  }

  override fun deleteAll(predicate: (Api) -> Boolean) =
      getAllApis()
          .filter(predicate)
          .forEach(this::deleteApi)

  override fun getById(apiId: Int) =
      ApiTable.getById(apiId)

  override fun searchByName(match: String): Iterable<Api> =
      ApiTable.searchByName(match)

  override fun updateMetrics(metrics: Metrics) =
      MetricsTable.updateMetrics(metrics)
}