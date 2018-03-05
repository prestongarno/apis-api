package com.prestongarno.apis.persistence

import com.prestongarno.apis.LocalRepository
import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api
import org.jetbrains.exposed.sql.deleteWhere


class LocalRepositoryImpl : LocalRepository {

  init {
    JdbcConnection.conn
    MetricsTable.updateMetrics(Metrics(0,0,0))
  }

  override fun getAllApis() = ApiTable.all()

  override fun getMetrics(): Metrics =
      MetricsTable.getPersistentMetrics()

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

}