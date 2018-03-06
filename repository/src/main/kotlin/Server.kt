package com.prestongarno.apis

import com.prestongarno.apis.core.ResourceManager
import com.prestongarno.apis.logging.logger
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


class Server(private val endpoint: GraphQlEndpoint) : AutoCloseable {

  private val log by logger()

  private val socket = ServerSocket(0)

  // TODO change endpoint interface to use input stream
  private fun onInput(stream: InputStream): InputStream =
      stream.reader()
          .readText()
          .also { log.info("Got GraphQL query request: \n$it") }
          .let(endpoint::handleRequest)
          .byteInputStream()

  override fun close() {
    try {
      socket.close()
    } catch (ioex: IOException) {
      log.warn("Error closing socket: " + ioex.localizedMessage + " ($socket)", ioex)
    }
  }

  fun start() {
    SocketListenerThread(socket::accept, { input: InputStream -> this.onInput(input) })
        .listener.start()
    log.info("Listening on local inet address " + socket.localSocketAddress.toString())
    log.info("Listening on port " + socket.localPort)
  }


  private class SocketListenerThread(
      private val init: () -> Socket?,
      private val onInput: (InputStream) -> InputStream) {


    private var isKill = AtomicBoolean(false)

    val listener = thread(start = false, priority = -20) {
      val log by logger()
      log.info("Socket listener thread started: " + Thread.currentThread().run {
        "Name: $name ID: $id State: $state"
      })

      while (!isKill.get()) {
        run {
          val socket = init() ?: return@run
          val remoteAddress = socket.remoteSocketAddress
          val instream = socket.getInputStream() ?: return@run
          try {
            log.info("Received request, attempting to reply to $remoteAddress")
            onInput(instream).use { resultStream ->
              socket.getOutputStream().use { outStream ->
                log.info("Got out stream $outStream")
                val str = resultStream.reader().readText()
                log.info("Got query result:\n$str")
                outStream.bufferedWriter().append(str)
                log.info("Wrote to $outStream")
                outStream.flush()
                outStream.close()
              }
            }
          } catch (ex: Exception) {
            log.info("Failed to accept request from $remoteAddress", ex)
            if (!socket.isClosed) socket.close()
          } finally {
            if (!socket.isClosed) socket.close()
          }
        }
      }
    }

    init {
      ResourceManager.addShutdownHook { isKill.set(true) }
    }

  }
}