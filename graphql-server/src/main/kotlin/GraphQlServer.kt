package com.prestongarno.apis.graphql

import com.github.pgutkowski.kgraphql.KGraphQL
import com.prestongarno.apis.Endpoint
import com.prestongarno.apis.Repository
import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.core.entities.ApiVersion


class GraphQlServer(private val localRepository: Repository) : Endpoint {

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

    type<Api>()
    type<ApiVersion>()
  }

  override fun handleRequest(value: String): String = schema.execute(value)
}