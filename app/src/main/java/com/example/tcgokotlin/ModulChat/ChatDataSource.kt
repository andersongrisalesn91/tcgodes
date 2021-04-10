package com.example.tcgokotlin.ModulChat

import android.content.Context
import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.data.model.Chat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
class ChatDataSource(context: Context): ChatInterfaces.ChatDataSourceI {

    override suspend fun insertMessage(): Flow<Resource<Chat>> = callbackFlow {

    }

    override suspend fun processConversation(): Flow<Resource<Chat>> = callbackFlow {

    }
}