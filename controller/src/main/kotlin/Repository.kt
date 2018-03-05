package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api

/**
 * TODO make this generic for different aggregate roots
 */
interface Repository {

  fun getAllApis(): List<Api>

  fun getMetrics(): Metrics

  fun getById(apiId: Int): Api?

  fun searchByName(match: String): Iterable<Api>

}

interface ReadWriteRepository : Repository {

  fun updateOrCreateApi(api: Api): Api

  fun updateOrCreateAll(apis: Iterable<Api>): Iterable<Api>

  fun deleteApi(api: Api)

  fun deleteAll(predicate: (Api) -> Boolean = { true })

  fun updateMetrics(metrics: Metrics)
}