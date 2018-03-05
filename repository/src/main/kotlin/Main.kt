package com.prestongarno.apis

import com.prestongarno.apis.net.NetworkClient
import com.prestongarno.apis.net.RemoteRepositoryImpl
import com.prestongarno.apis.persistence.LocalRepositoryImpl
import java.time.Duration


fun main(args: Array<String>) {

  val localRepository: LocalRepository =
      LocalRepositoryImpl()

  val remoteRepository: Repository =
      NetworkClient.builder()
          .endpoint("http://127.0.0.1:1337")
          .addDefaultHeader("Hello" to "World")
          .build()
          .let(::RemoteRepositoryImpl)

  Controller(localRepository, remoteRepository)
      .start(refreshRate = Duration.ofSeconds(10L))
}
