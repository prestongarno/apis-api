package com.prestongarno.apis.core

import com.prestongarno.apis.logging.logger
import kotlin.concurrent.thread

object ResourceManager {

  private val disposers = mutableListOf<() -> Unit>()

  private val log by logger()

  init {
    Runtime.getRuntime().addShutdownHook(
        thread(start = false, name = "ResourceManagementHook") {
          log.info("Shutting down resources...")
          disposers.forEach({ it() })
          log.info("Finished closing resources...")
        })
  }


  @Synchronized
  fun addShutdownHook(action: () -> Unit) {
    disposers.add(action)
  }
}