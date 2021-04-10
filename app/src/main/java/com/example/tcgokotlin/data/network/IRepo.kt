package com.example.tcgokotlin.data.network

import com.example.tcgokotlin.Helper.vo.Resource
import kotlinx.coroutines.flow.Flow
import java.util.ArrayList

interface IRepo {

    suspend fun getArrayServicesFin(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesPen(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesRec(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesPro(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesPen1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesRec1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesPro1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesClose(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesClose1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>

}