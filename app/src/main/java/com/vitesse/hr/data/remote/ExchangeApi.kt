package com.vitesse.hr.data.remote

import retrofit2.http.GET

interface ExchangeApi {

    @GET("currencies/eur.json")
    suspend fun getEurRates(): ExchangeResponse
}


data class ExchangeResponse(
    val date: String,
    val eur: Map<String, Double>
)
