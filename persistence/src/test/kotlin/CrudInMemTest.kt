package com.prestongarno.apis.persistence

import com.prestongarno.apis.ReadWriteRepository
import com.prestongarno.apis.core.Metrics
import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.core.entities.ApiInfo
import com.prestongarno.apis.core.entities.ApiVersion
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.*
import kotlin.coroutines.experimental.buildSequence


class CrudInMemTest {

  lateinit var localRepo: ReadWriteRepository

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
    val version = ApiVersion("Hello", now, ApiInfo(emptyMap()))

    localRepo.updateOrCreateApi(
        Api(preferred = "v1", versions = emptyList()))
        .let {
          assert(it.hasValidId())
          localRepo.updateOrCreateApi(it.copy(versions = listOf(version)))
        }

    localRepo.getAllApis().first().versions.first() //Fix this time stuff
  }

  @Test fun versionDateAddedIsConsistent() {
    localRepo.updateOrCreateApi(Api(-1, null, emptyList())).also {
      require(it.hasValidId())
      require(it.id == 1)
    }

    val now = Instant.now().toEpochMilli()

    val version = ApiVersion("foo", now, noInfo())
        .let { api -> ApiVersions.put(1, api) }

    require(now == version.added)
    require(now.toJodaDate() == version.added.toJodaDate())
    require(now.toDate() == version.added.toDate())
  }


  @Test fun metricsAreCorrect() {
    val initMetrics = MetricsTable.getPersistentMetrics()

    require(initMetrics == Metrics(0, 0, 0))

    MetricsTable.updateMetrics(Metrics(100, 100, 100))

    val newMetrics = MetricsTable.getPersistentMetrics()
    require(newMetrics != initMetrics)

    use(newMetrics) {
      require(numApis == 100)
      require(numEndpoints == 100)
      require(numSpecs == 100)
    }
  }

  @Test fun metricsUpdatesAreDeterministic() {
    MetricsTable.updateMetrics(Metrics(2,2,2))

    buildSequence {
      for (i in 1..15)
        yield(MetricsTable.getPersistentMetrics() * 2)
    }.forEach { metric ->
      MetricsTable.updateMetrics(metric)
      require(MetricsTable.getPersistentMetrics() == metric)
    }
  }
}

operator fun Metrics.times(value: Int) = Metrics(
    numApis = numApis * value,
    numEndpoints = numEndpoints * value,
    numSpecs = numSpecs * value)