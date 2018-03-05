package com.prestongarno.apis.persistence

import com.prestongarno.apis.LocalRepository
import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.core.entities.ApiInfo
import com.prestongarno.apis.core.entities.ApiVersion
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.*
import kotlin.coroutines.experimental.buildSequence


class CrudInMemTest {

  lateinit var localRepo: LocalRepository

  @Before fun initRepo() {
    JdbcConnection.restart()
    localRepo = LocalRepositoryImpl()
  }

  @Test fun putsApi() {
    val api = localRepo.updateOrCreateApi(
        Api(preferred = "v1", versions = emptyList()))

    assert(api.id >= 0)

    localRepo.deleteAll()

    buildSequence {
      for (i in 1..100) yield(api.copy(id = -1, preferred = UUID.randomUUID().toString().split("-").first()))
    }.toList()
        .let(localRepo::updateOrCreateAll)
        .toList()
        .also {
          require(it.size == localRepo.getAllApis().size)
        }
  }

  @Test fun putsAndUpdatesApi() {

    val now = Date.from(Instant.now()).toInstant().toEpochMilli()
    val version = ApiVersion("Hello", Instant.ofEpochMilli(now), ApiInfo(emptyMap()))

    val updatedApi = localRepo.updateOrCreateApi(
        Api(preferred = "v1", versions = emptyList()))
        .let {
          assert(it.hasValidId())
          localRepo.updateOrCreateApi(it.copy(versions = listOf(version)))
        }

    val persisted = localRepo.getAllApis().first().versions.first()
  }
}