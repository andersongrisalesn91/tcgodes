package com.example.tcgokotlin.utils.sharedPreferences

import android.content.Context
import android.provider.ContactsContract
import com.example.tcgokotlin.data.model.DataUser
import com.example.tcgokotlin.data.model.Northeast
import com.example.tcgokotlin.data.model.RequestUpdateName
import com.example.tcgokotlin.data.model.Ubication
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

class SesionManager(context: Context) {

    private val sp = context.getSharedPreferences(sharedAndroid, Context.MODE_PRIVATE)
    private val isConnect = true


    fun setDataUser(du: DataUser) {
                val gson = Gson()
                val jsonString = gson.toJson(du)
                val editor = sp.edit()
                editor.putString(KEY_DATA_USER, jsonString)
                editor.apply()
            }

        fun getInfo(): DataUser? {
            val json_data_user = sp.getString(KEY_DATA_USER, null)
            return if (json_data_user == null) {
                null
            } else {
                val gson = Gson()
                gson.fromJson(json_data_user, DataUser::class.java)
            }
        }

    fun setInitWorkDay(value: Boolean) {
        val editor = sp.edit()
        editor.putBoolean(KEY_INIT_WORK, value)
        editor.apply()
    }

    fun getInitWorkDay() = sp.getBoolean(KEY_INIT_WORK, false)

    fun setIntTasks(value: Int) {
        val editor = sp.edit()
        editor.putInt(KEY_INT_TASKS, value)
        editor.apply()
    }

    fun getIntTasks() = sp.getInt(KEY_INT_TASKS, 0)

    fun setLatLng(ubication: Ubication) {
        val gson = Gson()
        val jsonString = gson.toJson(ubication)
        val editor = sp.edit()
        editor.putString(KEY_UBICATION, jsonString)
        editor.apply()
    }

    fun getLatLng(): Ubication? {
        val json_ubication = sp.getString(KEY_UBICATION, null)
        return if (json_ubication == null) {
            null
        } else {
            val gson = Gson()
            gson.fromJson(json_ubication, Ubication::class.java)
        }
    }

    fun clearLatLng() {
        val editor = sp.edit()
        editor.remove(KEY_UBICATION).apply()
        editor.apply()
    }

    fun getBasicAuthorization() = sp.getString(KEY_BASIC_AUTHORIZATION, "")

    fun setBasicAuthorization(basicAuthorization: String) {
        val editor = sp.edit()
        editor.putString(KEY_BASIC_AUTHORIZATION, basicAuthorization)
        editor.apply()
    }

    fun getRequestUpdateName(): RequestUpdateName? {
        val json_data_user = sp.getString(KEY_REQUEST_UPDATE_NAME, null)
        return if (json_data_user == null) {
            null
        } else {
            val gson = Gson()
            gson.fromJson(json_data_user, RequestUpdateName::class.java)
        }
    }

    fun setRequestUpdateName(requestUpdateName: RequestUpdateName) {
        val gson = Gson()
        val jsonString = gson.toJson(requestUpdateName)
        val editor = sp.edit()
        editor.putString(KEY_REQUEST_UPDATE_NAME, jsonString)
        editor.apply()
    }

    fun getUserUid() = sp.getString(KEY_USER_UID, "")

    fun setUserUid(userUid: String) {
        val editor = sp.edit()
        editor.putString(KEY_USER_UID, userUid)
        editor.apply()
    }

        companion object {
            val sharedAndroid = "sharedAndroid"
            private const val KEY_DATA_USER = "key_data_user"
            private const val KEY_UBICATION = "key_ubication"
            private const val KEY_INIT_WORK = "key_init_work"
            private const val KEY_CONNECT_INTERNET = "key_connect_internet"
            private const val KEY_INT_TASKS = "key_int_tasks"
            private const val KEY_BASIC_AUTHORIZATION = "key_basic_authorization"
            private const val KEY_REQUEST_UPDATE_NAME = "key_request_update_name"
            private const val KEY_USER_UID = "key_user_uid"

        }
}