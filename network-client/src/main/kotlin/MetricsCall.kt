package com.prestongarno.apis.net

import com.prestongarno.apis.core.Metrics
import retrofit2.Call
import retrofit2.http.GET


interface MetricsCall {

  @GET("/v2/metrics.json")
  fun getMetrics(): Call<Metrics>

}
