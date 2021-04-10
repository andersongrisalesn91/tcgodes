package com.example.tcgokotlin.ModulChat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatViewModelFactory(private val chatRepoI: ChatInterfaces.ChatRepoI): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ChatInterfaces.ChatRepoI::class.java).newInstance(chatRepoI)
    }
}