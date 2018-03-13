package com.prestongarno.apis

import com.prestongarno.apis.graphql.GraphQlServer
import com.prestongarno.apis.persistence.LocalRepositoryImpl
import org.junit.Test


class GraphQlServerTest : MockRemoteService() {

  @Test fun assertThatThisWorks() {
    val localRepository = LocalRepositoryImpl()
    // initialize local repository with remote data
    localRepository.updateMetrics(remoteRepository.getMetrics())
    localRepository.updateOrCreateAll(remoteRepository.getAllApis())
    // create GraphQl server
    val gqlServer = GraphQlServer(localRepository)

    gqlServer.handleRequest("""
      |{
      |  api(id: 1) {
      |    name
      |  }
      |}
    """.trimMargin())
        .also(::println)

    """
      |{
      |  apiSearch(query: "") {
      |    name
      |  }
      |}
      """.trimMargin("|")
        .let(gqlServer::handleRequest)
        .also(::println)

  }
}
