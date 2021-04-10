package com.example.tcgokotlin.ModulOptions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.tcgokotlin.Helper.vo.Resource
import kotlinx.coroutines.Dispatchers

class OptionsViewModel(private val repo: OptionsInterfaces.OptionsRepoI): ViewModel() {

    val fetchUpdateName = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            emit(repo.postUpdateName())
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchCurrentDates = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            emit(repo.currentDates())
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }


}