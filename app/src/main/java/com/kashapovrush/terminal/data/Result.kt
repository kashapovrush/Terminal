package com.kashapovrush.terminal.data

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("results") val results: List<Bar>
)
