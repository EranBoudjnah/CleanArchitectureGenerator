package com.mitteloupe.cag.cli.request

data class DataSourceRequest(
    val dataSourceName: String,
    val useKtor: Boolean,
    val useRetrofit: Boolean
)
