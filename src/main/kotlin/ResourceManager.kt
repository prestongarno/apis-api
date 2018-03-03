package com.prestongarno.apis.core

import kotlin.concurrent.thread

object ResourceManager {

  private val disposers = mutableListOf<() -> Unit>()

  init {
    Runtime.getRuntime()
        .addShutdownHook(thread {
          disposers.forEach({ it() })
        })
  }


  @Synchronized
  fun addShutdownHook(action: () -> Unit) {
    disposers.add(action)
  }
}