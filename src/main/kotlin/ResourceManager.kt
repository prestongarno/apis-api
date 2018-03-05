package com.prestongarno.apis.core

import kotlin.concurrent.thread

object ResourceManager {

  private val disposers = mutableListOf<() -> Unit>()

  init {
    Runtime.getRuntime().addShutdownHook(
        thread(start = false, name = "ResourceManagementHook") {
          disposers.forEach({ it() })
        })
  }


  @Synchronized
  fun addShutdownHook(action: () -> Unit) {
    disposers.add(action)
  }
}