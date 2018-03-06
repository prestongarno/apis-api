package com.prestongarno.apis.persistence

import com.prestongarno.apis.core.entities.Api
import com.prestongarno.apis.core.entities.ApiInfo
import com.prestongarno.apis.core.entities.ApiVersion
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import java.time.Instant

internal object ApiTable : Table(name = "apis") {

  init {
    JdbcConnection.conn
  }

  val id = integer("id")
      .primaryKey()
      .autoIncrement()

  val name = varchar("name", 128)

  val preferred = varchar("preferred", 64).nullable()


  fun all(): List<Api> = singleTransation {
    selectAll().map { it.toApi() }
  }

  fun put(api: Api): Api {

    val newId = singleTransation {
      (if (api.id >= 0)
        update({ id eq api.id }, limit = 1) { it[preferred] = api.preferred }
      else
        insert {
          it[name] = api.name
          it[preferred] = api.preferred
        }.generatedKey!!)
    }.toInt()

    // TODO fix this
    return Api(api.name, newId, api.preferred, let {
      ApiVersions.deleteAllWith(newId)
      api.versions.map { ApiVersions.put(newId, it) }
    })
  }

  fun getById(idMatch: Int) = select { id eq idMatch }
      .firstOrNull()?.toApi()

  // TODO do this in the DB builtin
  fun searchByName(match: String): Iterable<Api> = all().filter {
    it.preferred?.contains(match) == true ||
        it.versions.firstOrNull { it.name.contains(match) } != null
  }

  private fun ResultRow.toApi(): Api {
    val thisName = this[name]
    val thisId = this[id]
    val preferredValue = this[preferred]
    val transaction = TransactionManager.currentOrNew(TransactionManager.manager.defaultIsolationLevel)
    val versions = ApiVersions.select {
      ApiVersions.apiId eq thisId
    }.map {
      ApiVersions.createVersionFromRow(it)
    }
    transaction.commit()
    return Api(thisName,
        thisId, preferredValue, versions)
  }

}


internal object ApiVersions : Table() {

  val id = integer("id")
      .primaryKey()
      .autoIncrement()

  val name = varchar("name", length = 64)
      .index(isUnique = false)

  val added = date("added")

  val apiId = integer("api_id")
      .references(ApiTable.id)

  internal
  fun createVersionFromRow(row: ResultRow): ApiVersion = ApiVersion(
      row[name], row[ApiVersions.added].toDate().toInstant().toEpochMilli(),
      ApiInfo(emptyMap()), row[id])

  fun deleteAllWith(api: Int) {
    singleTransation { deleteWhere { apiId eq api } }
  }

  fun put(rootApiId: Int, version: ApiVersion): ApiVersion {

    val key = transaction {

      insert {
        it[name] = version.name
        it[added] = DateTime(java.util.Date.from(Instant.ofEpochMilli(version.added)))
        it[apiId] = rootApiId
      }.generatedKey ?: -1
    }


    return version.copy(id = key.toInt())
  }
}
