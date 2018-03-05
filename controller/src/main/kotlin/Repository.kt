package com.prestongarno.apis

import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api

interface Repository {

  fun getAllApis(): List<Api>

  fun getMetrics(): Metrics

}

interface LocalRepository : Repository {

  fun updateOrCreateApi(api: Api): Api

  fun updateOrCreateAll(apis: Iterable<Api>): Iterable<Api>

  fun deleteApi(api: Api)

  fun deleteAll(predicate: (Api) -> Boolean = { true })
}