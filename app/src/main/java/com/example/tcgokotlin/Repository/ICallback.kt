package com.example.tcgokotlin.Repository

interface ICallback<T> {
    fun onSuccess(T: Any?)
    fun onFailed(T: Any?)
}