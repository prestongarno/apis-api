package com.prestongarno.apis.net

import com.prestongarno.apis.Repository
import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.logging.logger


class RemoteRepositoryImpl(networkClient: NetworkClient): Repository {

  private val client = Client(networkClient)

  private val log by logger()

  override fun getById(apiId: Int): Api? = null

  override fun searchByName(match: String): Iterable<Api> = emptyList()

  override fun getAllApis() = client.getAllRemoteApis()

  /**
   * @throws RuntimeException if the network call was unsuccessful
   */
  override fun getMetrics(): Metrics {
    return client.refreshMetrics() ?: throw RuntimeException()
  }
}