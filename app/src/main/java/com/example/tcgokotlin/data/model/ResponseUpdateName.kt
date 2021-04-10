package com.example.tcgokotlin.data.model

data class ResponseUpdateName(
    var data: MutableList<String>,
    var status: String,
    var status_code: Int
)