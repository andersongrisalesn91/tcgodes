package com.example.tcgokotlin.ModulOptions

import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.data.model.CurrentDates


class OptionsRepo(private val dataSource: OptionsInterfaces.OptionsDataSourceI): OptionsInterfaces.OptionsRepoI {

    override suspend fun postUpdateName() = dataSource.postUpdateName()

    override suspend fun currentDates(): Resource.Success<CurrentDates> = dataSource.currentDates()
}