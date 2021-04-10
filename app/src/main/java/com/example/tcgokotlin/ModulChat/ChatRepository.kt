package com.example.tcgokotlin.ModulChat

import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.data.model.Chat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
class ChatRepository(private val dataSource: ChatDataSource): ChatInterfaces.ChatRepoI  {

    override suspend fun insertMessage(): Flow<Resource<Chat>> = dataSource.insertMessage()

    override suspend fun processConversation(): Flow<Resource<Chat>> = dataSource.processConversation()

}