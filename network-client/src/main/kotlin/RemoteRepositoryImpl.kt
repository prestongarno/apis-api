package com.prestongarno.apis.net

import com.prestongarno.apis.Repository
import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api


class RemoteRepositoryImpl(private val networkClient: NetworkClient): Repository {

  override fun getAllApis(): List<Api> {
    TODO("not implemented")
  }

  override fun getMetrics(): Metrics {
    TODO("not implemented")
  }
}