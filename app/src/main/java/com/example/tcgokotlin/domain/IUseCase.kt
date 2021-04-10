package com.example.tcgokotlin.domain

import com.example.tcgokotlin.Helper.vo.Resource
import kotlinx.coroutines.flow.Flow
import java.util.ArrayList

interface IUseCase {

    suspend fun getArrayServicesFin(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesPen(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesPro(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesPen1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesPro1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesClose(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>
    suspend fun getRegServicesClose1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>>

}