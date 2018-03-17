package com.prestongarno.apis.graphql

import com.github.pgutkowski.kgraphql.KGraphQL
import com.prestongarno.apis.GraphQlEndpoint
import com.prestongarno.apis.Repository
import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.core.entities.ApiVersion
import com.prestongarno.apis.logging.logger


class GraphQlServer(private val localRepository: Repository) : GraphQlEndpoint {

  private val schema = KGraphQL.schema {

    configure {
      useDefaultPrettyPrinter = true
    }

    query("apiSearch") {
      resolver { query: String ->
        localRepository.searchByName(query)
      }
    }

    query("api") {
      resolver { id: Int -> localRepository.getById(id) }
    }

    query("metrics") {
      resolver(localRepository::getMetrics)
    }

    type<Api>()
    type<ApiVersion>()
    type<Metrics>()
  }

  private val log by logger()

  override fun handleRequest(value: String): String =
      try {
        schema.execute(value)
      } catch (ex: kotlin.Exception) {
        log.warn("Error with query", ex)
        """{"data": {},"errors":[{"message":"${ex.localizedMessage}"}]}"""
      }

}

