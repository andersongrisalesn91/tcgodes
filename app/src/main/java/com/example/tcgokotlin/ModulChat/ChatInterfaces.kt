package com.example.tcgokotlin.ModulChat

import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.data.model.Chat
import kotlinx.coroutines.flow.Flow

class ChatInterfaces {

    interface ChatRepoI {
        suspend fun insertMessage(): Flow<Resource<Chat>>
        suspend fun processConversation(): Flow<Resource<Chat>>
    }

    interface ChatDataSourceI {
        suspend fun insertMessage(): Flow<Resource<Chat>>
        suspend fun processConversation(): Flow<Resource<Chat>>
    }
}