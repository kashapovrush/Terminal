package com.kashapovrush.terminal.data

import com.kashapovrush.terminal.presentation.TimeFrame
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("aggs/ticker/AAPL/range/{timeFrame}/2022-01-09/2023-01-09?adjusted=true&sort=desc&limit=50000&apiKey=HelRjxIlhEDKZz6xvYGY2PSjQK9K36fP")
    suspend fun loadBar(
        @Path ("timeFrame") timeFrame: String
    ): Result
}