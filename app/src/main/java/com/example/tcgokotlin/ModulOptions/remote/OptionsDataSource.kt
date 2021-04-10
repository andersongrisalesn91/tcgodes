package com.example.tcgokotlin.ModulOptions.remote

import android.content.Context
import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.Helper.vo.RetrofitClient
import com.example.tcgokotlin.ModulOptions.OptionsInterfaces
import com.example.tcgokotlin.data.model.CurrentDates
import com.example.tcgokotlin.data.model.ResponseUpdateName
import com.example.tcgokotlin.utils.sharedPreferences.SesionManager

class OptionsDataSource(context: Context): OptionsInterfaces.OptionsDataSourceI {
    private var sm = SesionManager(context)

    override suspend fun postUpdateName():Resource.Success<ResponseUpdateName> = Resource.Success(RetrofitClient.webService.updateNames(sm.getRequestUpdateName(), sm.getBasicAuthorization()?:""))

    override suspend fun currentDates(): Resource.Success<CurrentDates> = Resource.Success(RetrofitClient.webService.getCurrentDates(sm.getUserUid()?:"",sm.getBasicAuthorization()?:""))


}