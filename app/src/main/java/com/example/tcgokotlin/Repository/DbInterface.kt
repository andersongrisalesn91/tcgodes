package com.example.tcgokotlin.Repository

import android.net.Uri

interface DbInterface {
    fun create(strTable: String?, obj: Any, mCallback: ICallback<*>)
    fun read(strTable: String?, mCallback: ICallback<*>)
    fun update(strTable: String?, strQuery: String?, `object`: Any?, mCallback: ICallback<*>)
    fun drop(strTable: String?, `object`: Any?, mCallback: ICallback<*>)
    fun select(
        strTable: String?,
        conditions: Map<String?, Any?>,
        onlySize: Boolean,
        mCallback: ICallback<*>
    )

    fun selectByDocument(strTable: String?, strDocument: String?, mCallback: ICallback<*>)
    fun selectWithListener(
        dbTable: String?,
        strKey: String?,
        conditions: Map<String?, Any?>?,
        mCallback: ICallback<*>
    )
    fun uploadFile(path: String?, uri: Uri?, mCallback: ICallback<*>)
}