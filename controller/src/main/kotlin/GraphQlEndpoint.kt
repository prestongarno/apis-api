package com.prestongarno.apis


interface GraphQlEndpoint {
  fun handleRequest(value: String): String
}
