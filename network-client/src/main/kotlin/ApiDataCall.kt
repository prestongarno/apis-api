package com.prestongarno.apis.net

import retrofit2.Call
import retrofit2.http.GET


interface ApiDataCall {

  @GET("/v2/list.json")
  fun getApisFromRemote(): Call<String> // I am extremely disappointed in Retrofit & Moshi
}


