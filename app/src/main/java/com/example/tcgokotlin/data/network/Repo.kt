package com.example.tcgokotlin.data.network

import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.utils.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import java.text.ParseException
import java.util.*

@ExperimentalCoroutinesApi
class Repo: IRepo {

    override suspend fun getArrayServicesFin(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        // 2.- Creamos una referencia al documento en nuestra DB
        val eventDocument =  FirebaseFirestore
            .getInstance()
            .collection("services")
            .whereEqualTo("ejecucion.fechafin", Common.formatDate.format(getCurrentDate()))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("estado", "Finalizado")

        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot != null) {
                val arrayListFin = snapshot.let { Common.serviceListener(it) }
                offer(Resource.Success(arrayListFin))
            }

        }

        awaitClose { subscription.remove() }
    }.conflate()

    override suspend fun getRegServicesPen(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        val eventDocument =  FirebaseFirestore
                .getInstance()
                .collection("services")
                .whereLessThanOrEqualTo("ejecucion.fechain", Common.formatDate.format(getCurrentDate()))
                .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
                .whereEqualTo("ejecucion.abierta", "1")
                .whereEqualTo("estado", "Pendiente")

        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot != null){
                val arrayListServicesPen = snapshot.let { Common.serviceListener(it) }
                offer(Resource.Success(arrayListServicesPen))
            }

        }

        awaitClose { subscription.remove() }
    }.conflate()

    override suspend fun getRegServicesRec(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        val eventDocument =  FirebaseFirestore
            .getInstance()
            .collection("services")
            .whereLessThanOrEqualTo("ejecucion.fechain", Common.formatDate.format(getCurrentDate()))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "1")
            .whereEqualTo("estado", "En Recorrido")

        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot != null){
                val arrayListServicesRec = snapshot.let { Common.serviceListener(it) }
                offer(Resource.Success(arrayListServicesRec))
            }
        }

        awaitClose { subscription.remove() }
    }.conflate()

    override suspend fun getRegServicesPro(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        val eventDocument =  FirebaseFirestore
                .getInstance()
                .collection("services")
                .whereLessThanOrEqualTo("ejecucion.fechain", Common.formatDate.format(getCurrentDate()))
                .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
                .whereEqualTo("ejecucion.abierta", "1")
                .whereEqualTo("estado", "En Proceso")

        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot != null){
                val arrayListServicesPro = snapshot.let { Common.serviceListener(it) }
                offer(Resource.Success(arrayListServicesPro))
            }
        }

        awaitClose { subscription.remove() }
    }.conflate()

    override suspend fun getRegServicesPen1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        val eventDocument =  Common.db
                ?.collection("Services")
                ?.whereEqualTo("ejecucion.fechain", Common.formatDate.format(getCurrentDate()))
                ?.whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
                ?.whereEqualTo("ejecucion.abierta", "0")
                ?.whereEqualTo("estado", "Pendiente")
        val subscription = eventDocument?.addSnapshotListener { snapshot, _ ->
            if(snapshot != null) {
                if (snapshot.documentChanges.isNotEmpty()) {
                    val arrayListServicesPen1 = snapshot.let { Common.serviceListener(it) }
                    offer(Resource.Success(arrayListServicesPen1))
                }
            }
        }

        awaitClose { subscription?.remove() }
    }.conflate()

    override suspend fun getRegServicesRec1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        val eventDocument =  Common.db
            ?.collection("Services")
            ?.whereEqualTo("ejecucion.fechain", Common.formatDate.format(getCurrentDate()))
            ?.whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            ?.whereEqualTo("ejecucion.abierta", "0")
            ?.whereEqualTo("estado", "En Recorrido")
        val subscription = eventDocument?.addSnapshotListener { snapshot, _ ->
            if(snapshot != null) {
                if (snapshot.documentChanges.isNotEmpty()) {
                    val arrayListServicesRec1 = snapshot.let { Common.serviceListener(it) }
                    offer(Resource.Success(arrayListServicesRec1))
                }
            }
        }

        awaitClose { subscription?.remove() }
    }.conflate()

    override suspend fun getRegServicesPro1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        val eventDocument =  FirebaseFirestore
                .getInstance()
                .collection("services")
                .whereEqualTo("ejecucion.fechain", Common.formatDate.format(getCurrentDate()))
                .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
                .whereEqualTo("ejecucion.abierta", "0")
                .whereEqualTo("estado", "En Proceso")

        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot != null){
                val arrayListServicesPro = snapshot.let { Common.serviceListener(it) }
                offer(Resource.Success(arrayListServicesPro))
            }
        }

        awaitClose { subscription.remove() }
    }.conflate()

    override suspend fun getRegServicesClose(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        val eventDocument =  FirebaseFirestore
                .getInstance()
                .collection("services")
                .whereLessThan("ejecucion.fechain", Common.formatDate.format(getCurrentDate()))
                .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
                .whereEqualTo("ejecucion.abierta", "0")
                .whereEqualTo("estado", "Pendiente")

        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot != null){
                val arrayListServicesClose = snapshot.let { Common.serviceListener(it) }
                offer(Resource.Success(arrayListServicesClose))
            }
        }

        awaitClose { subscription.remove() }
    }.conflate()

    override suspend fun getRegServicesClose1(): Flow<Resource<ArrayList<MutableMap<String, Any>?>?>> = callbackFlow {
        val eventDocument =  FirebaseFirestore
                .getInstance()
                .collection("services")
                .whereLessThan("ejecucion.fechain", Common.formatDate.format(getCurrentDate()))
                .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
                .whereEqualTo("ejecucion.abierta", "0")
                .whereEqualTo("estado", "En proceso")

        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot != null){
                val arrayListServicesClose1 = snapshot.let { Common.serviceListener(it) }
                offer(Resource.Success(arrayListServicesClose1))
            }
        }

        awaitClose { subscription.remove() }
    }.conflate()

    private fun getCurrentDate(): Date  {
        return try {
            Common.formatDate.parse(Common.formatDate.format(Date()))
        } catch (e: ParseException) {
            Date()
        }
    }

}