package com.prestongarno.apis


interface Endpoint {
  fun handleRequest(value: String): String
}
