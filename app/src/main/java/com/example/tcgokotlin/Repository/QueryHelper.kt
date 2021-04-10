package com.example.tcgokotlin.Repository

import android.net.Uri
import android.util.Log
import com.example.tcgokotlin.Helper.Tools
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import java.util.*
import java.util.concurrent.ExecutionException

class QueryHelper : DbInterface {
    private val db = FirebaseFirestore.getInstance()
    var dbUser = "usersInformation"
    val dbServices = "Service"
    val dbGeoLocations = "geolocations"
    val dbAllocationPoints = "allocationPoints"
    val dbUserCampScoresbase = "userCampScoresbase"
    val dbConversation = "conversation"

    override fun create(dbTable: String?, obj: Any, mCallback: ICallback<*>) {
        val mpEntity: Map<String, Any> = Tools.convertObjToMapReflection(obj)
        db.collection(dbTable.toString())
            .add(mpEntity)
            .addOnSuccessListener { documentReference -> //Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                mCallback.onSuccess(documentReference.id)
            }
            .addOnFailureListener { e -> mCallback.onFailed(e.message) }
    }

    override fun select(
        dbTable: String?,
        conditions: Map<String?, Any?>,
        onlySize: Boolean,
        mCallback: ICallback<*>
    ) {
        var queryStore: Query = db.collection(dbTable.toString())
        if (conditions != null) {
            for ((key, value) in conditions) {
                queryStore = if (key!!.contains(">=")) {
                    queryStore.whereGreaterThanOrEqualTo(key.replace(">=", ""), value.toString())
                } else if (key.contains("<=")) {
                    queryStore.whereLessThanOrEqualTo(key.replace("<=", ""), value.toString())
                } else {
                    queryStore.whereEqualTo(key.toString(), value)
                }
            }
        }
        queryStore.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (onlySize) {
                        mCallback.onSuccess(task.result.size())
                    } else {
                        val lstData: MutableList<Map<String, Any>> =
                            ArrayList()
                        for (document in task.result) {
                            val data = document.data
                            lstData.add(data)
                        }
                        mCallback.onSuccess(lstData)
                    }
                } else {
                    mCallback.onFailed(task.exception!!.message)
                }
            }.addOnFailureListener { e -> mCallback.onFailed(e.message) }
    }

    override fun selectByDocument(
        dbTable: String?,
        strDocument: String?,
        mCallback: ICallback<*>
    ) {
        db.collection(dbTable!!).document(strDocument!!).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mCallback.onSuccess(task.result.data)
                }
            }
    }

    override fun selectWithListener(
        dbTable: String?,
        strKey: String?,
        conditions: Map<String?, Any?>?,
        mCallback: ICallback<*>
    ) {
        var queryStore: Query = db.collection(dbTable!!)
        if (conditions != null) {
            for ((key, value) in conditions) {
                queryStore = queryStore.whereEqualTo(key!!, value)
            }
        }
        queryStore.addSnapshotListener { value, e ->
            if (value != null) {
                val lstData: MutableList<Map<String, Any>?> =
                    ArrayList()
                for (document in value) {
                    val data = document.data
                    if (data.containsKey("date")) {
                        val t = data["date"] as Timestamp?
                        if (t != null) data["date"] = t.toDate().toString()
                    }
                    lstData.add(data)
                }
                val strJson = JSONArray(lstData).toString()
              //  shared.setDataListener(strKey, strJson)
                mCallback.onSuccess(lstData)
                // Log.d(TAG, "Current cites in CA: " + cities);
            }
        }
        // [END listen_multiple]
    }


    override fun read(dbTable: String?, mCallback: ICallback<*>) {
        db.collection(dbTable!!)
            .whereEqualTo("userUID", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("type_user", "Campo")
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val data = task.result.documents[0].data
                    data!!["key"] = task.result.documents[0].id
                    mCallback.onSuccess(data)
                } else {
                    Log.d("diacoInfo", "get failed with ", task.exception)
                }
            }
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun readCed(dbTable: String?, dbRegister: String?, mCallback: ICallback<*>) {
        db.collection(dbTable!!)
            .whereEqualTo("identification", dbRegister)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result.documents
                    if (documents.size != 0) {
                        Log.e("DiacoApp", documents.toString())
                        val data = task.result.documents[0].data
                        mCallback.onSuccess(data)
                    } else {
                        Log.e("Diaco", "1 " + task.result.metadata.toString())
                        mCallback.onFailed("Sin datos. " + task.result.toString())
                    }
                } else {
                    mCallback.onFailed("Error en consulta. " + task.result.toString())
                }
            }
    }

    override fun update(
        dbTable: String?,
        strId: String?,
        obj: Any?,
        mCallback: ICallback<*>
    ) {
        val mpEntity: Map<String, Any> = Tools.convertObjToMapReflection(obj as Any)
        db.collection(dbTable!!).document(strId!!).update(mpEntity).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mCallback.onSuccess(true)
            } else {
                mCallback.onFailed(task.exception!!.message)
            }
        }
    }

    override fun uploadFile(path: String?, uri: Uri?, mCallback: ICallback<*>) {
        val mStorageRef = FirebaseStorage.getInstance().reference
        val storageReference = mStorageRef.child(path!!)
        storageReference.putFile(uri!!).addOnSuccessListener { taskSnapshot ->
            val task = taskSnapshot.metadata!!
                .reference!!.downloadUrl
            task.addOnSuccessListener { uri -> mCallback.onSuccess(uri.toString()) }
        }.addOnFailureListener { e -> mCallback.onFailed(e.message) }
    }

    override fun drop(dbTable: String?, `object`: Any?, mCallback: ICallback<*>) {}

}