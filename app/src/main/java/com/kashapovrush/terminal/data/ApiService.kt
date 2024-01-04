package com.kashapovrush.terminal.data

import retrofit2.http.GET

interface ApiService {

    @GET("aggs/ticker/AAPL/range/1/hour/2022-01-09/2023-01-09?adjusted=true&sort=desc&limit=50000&apiKey=HelRjxIlhEDKZz6xvYGY2PSjQK9K36fP")
    suspend fun loadBar(): Result
}