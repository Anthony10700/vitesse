package com.vitesse.hr.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeApi {

    @GET("latest")
    suspend fun getRate(
        @Query("from") from: String,
        @Query("to") to: String
    ): ExchangeResponse
}

data class ExchangeResponse(
    val rates: Map<String, Double>
)
