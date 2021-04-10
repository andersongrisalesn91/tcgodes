package com.example.tcgokotlin.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.data.network.Repo
import com.example.tcgokotlin.domain.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

class MainViewModel(useCase: IUseCase): ViewModel() {


    val fetchArrayServicesFin = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            useCase.getArrayServicesFin().collect {
                emit(it)
            }
        } catch (e: Exception){
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchArrayServicesPen = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            useCase.getRegServicesPen().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchArrayServicesPro = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            useCase.getRegServicesPro().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchArrayServicesPen1 = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            useCase.getRegServicesPen1().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchArrayServicesPro1 = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            useCase.getRegServicesPro1().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchArrayServicesClose = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            useCase.getRegServicesClose().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchArrayServicesClose1 = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            useCase.getRegServicesClose1().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }
}