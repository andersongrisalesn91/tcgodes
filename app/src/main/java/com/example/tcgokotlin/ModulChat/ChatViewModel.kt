package com.example.tcgokotlin.ModulChat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.tcgokotlin.Helper.vo.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
class ChatViewModel(repo: ChatRepository): ViewModel() {

    val fetchInserMessage = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            repo.insertMessage().collect {
                emit(it)
            }
        } catch (e: Exception){
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchProcessConversation = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            repo.processConversation().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

}