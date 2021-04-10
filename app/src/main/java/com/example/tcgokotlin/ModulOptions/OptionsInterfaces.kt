package com.example.tcgokotlin.ModulOptions

import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.data.model.CurrentDates
import com.example.tcgokotlin.data.model.ResponseUpdateName


class OptionsInterfaces {

    interface OptionsRepoI {
        suspend fun postUpdateName(): Resource.Success<ResponseUpdateName>
        suspend fun currentDates(): Resource.Success<CurrentDates>
    }

    interface OptionsDataSourceI {
        suspend fun postUpdateName(): Resource.Success<ResponseUpdateName>
        suspend fun currentDates(): Resource.Success<CurrentDates>
    }

}