package com.example.tcgokotlin.domain

import com.example.tcgokotlin.data.model.CurrentDates
import com.example.tcgokotlin.data.model.RequestUpdateName
import com.example.tcgokotlin.data.model.ResponseUpdateName
import retrofit2.http.*

interface WebService {

    @GET("datescurrent/{id}")
    suspend fun getCurrentDates(@Path("id") id:String, @Header("Authorization") authHeader: String): CurrentDates

    @POST("updatename")
    suspend fun updateNames(@Body requestUpdateName: RequestUpdateName?, @Header("Authorization") authHeader: String): ResponseUpdateName

}