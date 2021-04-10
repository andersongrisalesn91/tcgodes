package com.example.tcgokotlin.domain

import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.data.network.IRepo
import kotlinx.coroutines.flow.Flow
import java.util.ArrayList

class UseCase(private val repo: IRepo): IUseCase {

    override suspend fun getArrayServicesFin(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = repo.getArrayServicesFin()
    override suspend fun getRegServicesPen(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = repo.getRegServicesPen()
    override suspend fun getRegServicesPro(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = repo.getRegServicesPro()
    override suspend fun getRegServicesPen1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = repo.getRegServicesPen1()
    override suspend fun getRegServicesPro1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = repo.getRegServicesPro1()
    override suspend fun getRegServicesClose(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = repo.getRegServicesClose()
    override suspend fun getRegServicesClose1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = repo.getRegServicesClose1()

}