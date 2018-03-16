package com.prestongarno.apis

import com.prestongarno.apis.graphql.GraphQlServer
import com.prestongarno.apis.persistence.LocalRepositoryImpl
import org.junit.Test
import org.kotlinq.api.Fragment
import org.kotlinq.dsl.query
import java.time.Duration
import java.time.Instant


class GraphQlServerTest : MockRemoteService() {

  @Test fun assertThatThisWorks() {
    val localRepository = LocalRepositoryImpl()
    // initialize local repository with remote data
    localRepository.updateMetrics(remoteRepository.getMetrics())
    localRepository.updateOrCreateAll(remoteRepository.getAllApis())
    // create GraphQl server
    val gqlServer = GraphQlServer(localRepository)

    Server(gqlServer).start()

    val now = Instant.now()
    val duration = Duration.ofSeconds(30L)
    while (now.plusSeconds(duration.seconds).isAfter(Instant.now())) {
      // nothing
    }
  }
}

fun GraphQlServer.execute(fragment: Fragment): String {
  return this.handleRequest(
      fragment.toGraphQl(
          pretty = false,
          idAndTypeName = false))
}

private fun queryById(id: Int): Fragment = query {
  "api"("id" to id) on def("Api") {
    "name"(!string)
    "preferred"(!string)
    "versions" on def("ApiVersion") {
      "name"(!string)
      "added"(!string)
      "swaggerUrl"(!string)
      "swaggerYamlUrl"(!string)
    }
  }


}
