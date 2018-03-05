package com.prestongarno.apis.net

import com.prestongarno.apis.logging.logger
import java.net.URI


interface NetworkClient {

  val endpoint: java.net.URI

  val defaultHeaders: Map<String, String>



  class Builder internal constructor() {

    private val logger by this.logger()

    private var endpoint = URI("http://localhost")
    private var defaultHeaders = mutableMapOf<String, String>()

    fun endpoint(uri: String) = apply { this.endpoint = URI(uri) }

    fun endpoint(uri: URI) = apply { this.endpoint = uri }

    fun addDefaultHeader(pair: Pair<String, String>) =
        apply { defaultHeaders[pair.first] = pair.second }

    fun build(): NetworkClient {
      val uri = this@Builder.endpoint.copy()
      val defHeaders = this@Builder.defaultHeaders.toMap()

      logger.info("Network client created; endpoint: $uri")

      return object : NetworkClient {
        override val endpoint: URI = uri
        override val defaultHeaders = defHeaders
      }
    }
  }

  companion object {

    fun builder(): NetworkClient.Builder = Builder()
  }
}

/**
 * public URI(String scheme,
 *  userInfo,
 *  host,
 *  port,
 *  path,
 *  query,
 *  fragment)
 */
fun URI.copy(): URI = let {
  URI(scheme, userInfo, host, port, path, query, fragment)
}
