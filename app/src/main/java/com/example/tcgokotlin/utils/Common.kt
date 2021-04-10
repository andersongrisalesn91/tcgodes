package com.example.tcgokotlin.utils

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.tcgokotlin.R
import com.example.tcgokotlin.data.model.User
import com.firebase.geofire.GeoFire
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.paperdb.Paper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

object Common {
    var txtFirma: String = ""
    var auth: FirebaseAuth? = null
    var UID = ""
    var EMAIL = ""
    const val REQUEST_IMAGE_CAPTURE = 1

    //FireStore  Database;
    var dbSettings: FirebaseFirestoreSettings? = null
    var ifRute = false
    var listenerRegDriversInformation: ListenerRegistration? = null
    var listenerRegServicesPen: ListenerRegistration? = null
    var listenerRegServicesRec: ListenerRegistration? = null
    var listenerRegServicesPro: ListenerRegistration? = null
    var listenerRegServicesPen1: ListenerRegistration? = null
    var listenerRegServicesRec1: ListenerRegistration? = null
    var listenerRegServicesPro1: ListenerRegistration? = null
    var listenerRegServicesFin: ListenerRegistration? = null
    var listenerRegServicesClose: ListenerRegistration? = null
    var listenerRegServicesClose1: ListenerRegistration? = null
    var listenerRegistro: ListenerRegistration? = null
    var listenerDriver: ListenerRegistration? = null
    var listenerRegistroUs: ListenerRegistration? = null
    var db: FirebaseFirestore? = null
    var dbVersion: CollectionReference? = null
    var dbRegistro: CollectionReference? = null
    var dbRegistroUs: CollectionReference? = null
    var dbDriver: CollectionReference? = null
    var dbDriversInformation: CollectionReference? = null
    var dbServices: CollectionReference? = null
    var versionCode = 0
    var documentUser: MutableMap<String, Any?>? = null

    var mapRegistroUs: MutableMap<String, Any>? = null
    var arrayServicePen: ArrayList<MutableMap<String, Any>?>? = null
    var arrayServiceRec: ArrayList<MutableMap<String, Any>?>? = null
    var arrayServicePro: ArrayList<MutableMap<String, Any>?>? = null
    var arrayServicePen1: ArrayList<MutableMap<String, Any>?>? = null
    var arrayServiceRec1: ArrayList<MutableMap<String, Any>?>? = null
    var arrayServicePro1: ArrayList<MutableMap<String, Any>?>? = null
    var arrayServiceFin: ArrayList<MutableMap<String, Any>?>? = null
    var arrayServiceClose: ArrayList<MutableMap<String, Any>?>? = null
    var arrayServiceClose1: ArrayList<MutableMap<String, Any>?>? = null
    var mDialog: ProgressDialog? = null
    var mDialog2: ProgressDialog? = null
    var mDialogUsuario: ProgressDialog? = null
    var currentPhotoPath: String? = null
    var selectedService: MutableMap<String, Any>? = null
    var currentPhotoName: String? = null
    var mapTask: MutableMap<String, Any?>? = null
    var ifSave: String? = null
    var arrayItems: ArrayList<MutableMap<String, Any?>>? = null
    var finalY: MutableMap<String, Any?>? = null
    var finalX: MutableMap<String, Any?>? = null
    var arrayContenido: ArrayList<Any>? = null
    var cantGrup = 0
    var strReq: String? = null
    var mapImgHome: MutableMap<String, Any>? = null
    var mapImgNormal: MutableMap<String?, Any>? = null
    var txtImgHome: String? = null
    var intTypeImg = 0
    var boolTypePhoto = false
    var ifRegistro = true
    var ifSaveImage = false
    private var alertDialog: AlertDialog.Builder? = null
    private var show: AlertDialog? = null
    var geoFire: GeoFire? = null
    var urlApi = "https://backofficedev.tcgoapp.net/api?request=getData"
    //var urlApi = "https://backofficedev.tcgoapp.net/webserviceapi/appmovil/v1/gatData/"

    //"https://www.tcgoapp.net/login/api/";
    //"https://www.tcgoapp.com/tcgoDev/api/";
    //"https://www.tcgoapp.com/login/api/";
    //"http://192.168.0.21/tcgodev/api/";
    val formatDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatHora = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatHora = SimpleDateFormat("HH:mm a", Locale.getDefault())
    val formatDateHora = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault())
    val formatDateHoraSeg = SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.getDefault())
    val formatDateHoraSeg2 = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault())
    var TCGOdbname = ""
    const val baseURL = "https://maps.googleapis.com"
    const val fcmURL = "https://fcm.googleapis.com"
    var driver_tb1 = "Drivers"
    var user_driver_tb1 = "DriversInformation"
    var user_register_tb1 = "Registro"
    var user_error_tb1 = "Error"
    var token_tbl = "Tokens"
    var rate_detail_tbl = "RateDetails"
    var service_tbl = "Service"
    var novedad_tbl = "Novedad"
    var chat_tbl = "Chat"
    var urlListAdapterImgPhoto: String? = null
    var typePhoto: String? = null
    var txtPhoto: String? = null
    var ListAdapterImgPhoto = false
    var boolPhotos = true
    var nameFirma = ""
    var recIdTarea = ""
    var keyReq = "ea5f5af802977407f5a0d5ac43b532d1"
    var hashCode: String? = null
    var firma = false
    var mTypoTarea = false
    var boolSMS = true
    var boolPlay = true
    var listServicios = false
    var boolRecKm = false
    var base_fare = 2500.0
    var ifJornada = 0
    var ifBreack = 0
    var ifAlmuerzo = 0
    var ifPermiso = 0
    var cantPhotos = 40
    var mLastLocation: Location? = null
    var imageUri: Uri? = null
    var listTipoServ: Map<String, String>? = null
    var listNoEx: Map<Int, String>? = null
    var currentUser: User? = null
    var listSelector: Map<Int, String>? = null
    private val mapService: Map<String, Any>? = null
    var listSelectorElement: Map<String, ArrayList<String>>? = null
    var mContext: Context? = null
    var mLayoutInflater: LayoutInflater? = null
    private var mediaPlayer: MediaPlayer? = null
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference.child("image")
    var key: String? = null
    private const val contDialog = 0
    var waitingDialog: android.app.AlertDialog? = null
    var waitingDialogTrack: android.app.AlertDialog? = null
    fun saveError(e: Exception, dirClass: String?, mapNota: MutableMap<String?, Any?>) {
        mapNota["idUser"] = FirebaseAuth.getInstance().currentUser!!.uid
        mapNota["date"] = formatDateHora.format(Date())
        mapNota["errorMessage"] = e.message
        mapNota["errorType"] = e.javaClass.name
        mapNota["errorLine"] = e.stackTrace[0].toString()
        FirebaseDatabase.getInstance().getReference(hashCode + "/" + user_error_tb1).push()
            .setValue(mapNota)
    }

    fun setDbSettings() {
        db = FirebaseFirestore.getInstance()
        dbSettings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .setPersistenceEnabled(true)
            .build()
        db?.firestoreSettings = dbSettings as FirebaseFirestoreSettings
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun versionListener() {
        dbVersion = db?.collection("version")
        try { show?.dismiss() } catch (f: Exception) { }
        dbVersion?.addSnapshotListener(
                MetadataChanges.INCLUDE,
                object : EventListener<QuerySnapshot> {
                    override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                        if (e != null) {
                            Log.e("Consulta", "Listen error", e)
                            return
                        }
                        value?.documentChanges?.forEach { change ->
                            if (change.type == DocumentChange.Type.ADDED) {
                                Log.e("Consulta", "ADDED:" + change.document.data)
                            }
                            if (change.type == DocumentChange.Type.MODIFIED) {
                                Log.e("Consulta", "MODIFIED:" + change.document.data)
                            }
                            if (change.type == DocumentChange.Type.REMOVED) {
                                Log.e("Consulta", "MODIFIED:" + change.document.data)
                            }
                            if (versionCode != change.document.data["code"].toString().toInt()) {
                                alertDialog = mContext?.let { AlertDialog.Builder(it) }
                                alertDialog?.setIcon(R.drawable.ic_android)
                                alertDialog?.setTitle(mContext?.getString(R.string.new_update_available))
                                alertDialog?.setMessage(mContext?.getString(R.string.please_update_version))
                                val layout_bnt: View? = mLayoutInflater?.inflate(R.layout.layout_btn, null)
                                val btn1 = layout_bnt?.findViewById<Button>(R.id.btn1)
                                val btn2 = layout_bnt?.findViewById<Button>(R.id.btn2)
                                btn1?.visibility = View.GONE
                                btn2?.text = mContext?.getString(R.string.accept)
                                alertDialog?.setView(layout_bnt)
                                alertDialog?.setOnDismissListener {
                                    show?.dismiss()
                                    Toast.makeText(mContext, "finish()", Toast.LENGTH_SHORT).show()
                                }
                                show = alertDialog?.show()
                                btn2?.setOnClickListener {
                                    mContext?.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.app.worktic.tcgo"))
                                    )
                                }
                            } else {
                                try { show?.dismiss() } catch (f: Exception) { }
                            }
                        }
                    }
                })
    }

    fun setDriversInformation(field: String?, value: String?) {
        dbDriversInformation = db!!.collection("DriversInformation")
        try {
            listenerRegDriversInformation!!.remove()
        } catch (e: Exception) {
        }
        listenerRegDriversInformation = dbDriversInformation!!.whereEqualTo(field!!, value)
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.w("Consulta", "Listen error", e)
                        return
                    }
                    value?.documentChanges?.forEach { change ->
                        documentUser = change.document.data
                        documentUser!!["key"] = value.documents[0].id
                        Log.e("Consulta Common", documentUser.toString())
                        try {
                            mDialogUsuario!!.dismiss()
                        } catch (i: Exception) {
                        }
                    }
                }
            })
    }

    fun setServices() {
        dbServices = db!!.collection("Services")
        val mDate: Date?
        mDate = try {
            formatDate.parse(formatDate.format(Date()))
        } catch (e: ParseException) {
            Date()
        }

        // Tareas Finalizadas -------------------------------------------------------------------------------------------------------------------------------------------------------
        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        try {
            listenerRegServicesFin!!.remove()
        } catch (e: Exception) {
        }
        listenerRegServicesFin = dbServices!!
            .whereEqualTo("ejecucion.fechafin", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("estado", "Finalizado")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServiceFin = value?.let { serviceListener(it) }
                    try {
                        waitingDialogTrack!!.dismiss()
                    } catch (i: Exception) {
                    }
                }
            })
        // Fin Tareas Finalizadas -------------------------------------------------------------------------------------------------------------------------------------------------------
        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // Tareas Para Mostrar -------------------------------------------------------------------------------------------------------------------------------------------------------
        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        try {
            listenerRegServicesRec!!.remove()
        } catch (e: Exception) {
        }
        listenerRegServicesRec = dbServices!!
            .whereLessThanOrEqualTo("ejecucion.fechain", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "1")
            .whereEqualTo("estado", "En Recorrido")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServiceRec = value?.let { serviceListener(it) }
                    if (arrayServiceRec!!.size > 0) {
                        mediaPlayer = MediaPlayer.create(mContext, R.raw.tarea) /*mediaPlayer.start();*/
                    }
                    try {
                        waitingDialogTrack!!.
                        dismiss()
                    } catch (i: Exception) {
                    }
                }
            })
        try {
            listenerRegServicesRec1!!.remove()
        } catch (e: Exception) {
        }

        listenerRegServicesRec1 = dbServices!!
            .whereEqualTo("ejecucion.fechain", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "0")
            .whereEqualTo("estado", "En Recorrido")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServiceRec1 = value?.let { serviceListener(it) }
                    if (arrayServiceRec1!!.size > 0) {
                        mediaPlayer = MediaPlayer.create(mContext, R.raw.tarea) /*mediaPlayer.start();*/
                    }
                    try {
                        waitingDialogTrack!!.
                        dismiss()
                    } catch (i: Exception) {
                    }
                }
            })

        try {
            listenerRegServicesPen!!.remove()
        } catch (e: Exception) {
        }
        listenerRegServicesPen = dbServices!!
                .whereLessThanOrEqualTo("ejecucion.fechain", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "1")
            .whereEqualTo("estado", "Pendiente")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServicePen = value?.let { serviceListener(it) }
                    if (arrayServicePen!!.size > 0) {
                        mediaPlayer = MediaPlayer.create(mContext, R.raw.tarea) /*mediaPlayer.start();*/
                    }
                    try {
                        waitingDialogTrack!!.
                        dismiss()
                    } catch (i: Exception) {
                    }
                }
            })

        try {
            listenerRegServicesPen1!!.remove()
        } catch (e: Exception) {
        }
        listenerRegServicesPen1 = dbServices!!
            .whereEqualTo("ejecucion.fechain", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "0")
            .whereEqualTo("estado", "Pendiente")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServicePen1 = value?.let { serviceListener(it) }
                    if (arrayServicePen1!!.size > 0) {
                        mediaPlayer = MediaPlayer.create(mContext, R.raw.tarea) /*mediaPlayer.start();*/
                    }
                    try {
                        waitingDialogTrack!!.dismiss()
                    } catch (i: Exception) {
                    }
                }
            })

        try {
            listenerRegServicesPro!!.remove()
        } catch (e: Exception) {
        }
        listenerRegServicesPro = dbServices!!
            .whereLessThanOrEqualTo("ejecucion.fechain", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "1")
            .whereEqualTo("estado", "En Proceso")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServicePro = value?.let { serviceListener(it) }
                    if (arrayServicePro!!.size > 0) {
                        mediaPlayer = MediaPlayer.create(mContext, R.raw.tarea) /*mediaPlayer.start();*/
                    }
                    try {
                        waitingDialogTrack!!.dismiss()
                    } catch (i: Exception) {
                    }
                }
            })

        try {
            listenerRegServicesPro1!!.remove()
        } catch (e: Exception) {
        }
        listenerRegServicesPro1 = dbServices!!
            .whereEqualTo("ejecucion.fechain", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "0")
            .whereEqualTo("estado", "En Proceso")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServicePro1 = value?.let { serviceListener(it) }
                    if (arrayServicePro1!!.size > 0) {
                        mediaPlayer =
                                MediaPlayer.create(mContext, R.raw.tarea) /*mediaPlayer.start();*/
                    } else {
                    }
                    try {
                        waitingDialogTrack!!.dismiss()
                    } catch (i: Exception) {
                    }
                }
            })
        // Fin Tareas Para Mostrar -------------------------------------------------------------------------------------------------------------------------------------------------------
        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // Tareas Para Cerrar -------------------------------------------------------------------------------------------------------------------------------------------------------
        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        try {
            listenerRegServicesClose!!.remove()
        } catch (e: Exception) {
        }
        listenerRegServicesClose = dbServices!!
            .whereLessThan("ejecucion.fechain", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "0")
            .whereEqualTo("estado", "Pendiente")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServiceClose = serviceListener(value!!)
                    try {
                        waitingDialogTrack!!.dismiss()
                    } catch (i: Exception) {
                    }
                }
            })
        try {
            listenerRegServicesClose1!!.remove()
        } catch (e: Exception) {
        }
        listenerRegServicesClose1 = dbServices!!
            .whereLessThan("ejecucion.fechain", formatDate.format(mDate))
            .whereEqualTo("infoUsers.0.idDriver", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("ejecucion.abierta", "0")
            .whereEqualTo("estado", "En Proceso")
            .addSnapshotListener(MetadataChanges.INCLUDE, object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        Log.e("ConsultaDate", "Listen error", e)
                        try {
                            waitingDialogTrack!!.dismiss()
                        } catch (i: Exception) {
                        }
                        return
                    }
                    arrayServiceClose1 = value?.let { serviceListener(it) }
                    try {
                        waitingDialogTrack!!.dismiss()
                    } catch (i: Exception) {
                    }
                }
            })
        // Fin Tareas Para Cerrar -------------------------------------------------------------------------------------------------------------------------------------------------------
        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    }

    fun serviceListener(querySnapshot: QuerySnapshot): ArrayList<MutableMap<String, Any>?>? {
        val source = if (querySnapshot.metadata.isFromCache) "local" else "server"
        val mArrayService = ArrayList<MutableMap<String, Any>?>()
        var mapService: MutableMap<String, Any>
        for (snapshot in querySnapshot.documents) {
            mapService = snapshot.data!!
            val mapHistory = mapService["historial"] as Map<String, Any>?
            try {
                (mapHistory!![formatDate.format(Date())] as Map<String?, Any>?)!!["TareaRangoIn"].toString()
            } catch (e: Exception) {
                val mapHistoryUpdate: MutableMap<String, Any> = HashMap()
                val mapHistorynow: MutableMap<String, Any> = HashMap()
                for ((key1, value) in mapHistory!!) {
                    mapHistorynow[key1] = value
                    mapHistorynow[formatDate.format(Date())] = value
                }
                mapHistoryUpdate["historial"] = mapHistorynow
                dbServices!!.document(snapshot.id).update(mapHistoryUpdate)
            }
            mapService["key"] = snapshot.id
            mArrayService.add(mapService)
        }
        return mArrayService
    }

    fun setRegistroUs(StrId: String?) {
        mDialog2?.show()
        try {
            dbRegistroUs = db!!.collection("Registro").document(StrId!!).collection("Usuarios")
        } catch (o: Exception) {
            o.printStackTrace()
            Log.e("tcgoReg", o.cause.toString())
        }
        val StrIdUser = documentUser!!["key"].toString()
        Log.e("tcgoReg", "pre 5 $StrIdUser")
        dbRegistroUs!!.whereEqualTo("IdUsuario", StrIdUser).get()
            .addOnSuccessListener { querySnapshotRegUs ->
                Log.e("tcgoReg", "5" + querySnapshotRegUs.documents.toString())
                Log.e("tcgoReg", "6")
                val doc = querySnapshotRegUs.documents
                if (mapRegistroUs == null) {
                    mapRegistroUs = HashMap()
                }
                if (doc.size != 0) {
                    mapRegistroUs = doc[0].data
                    mapRegistroUs!!["key"] = doc[0].id
                    if ((mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["Estado"].toString() != "0"
                    ) {
                        recIdTarea =
                            (mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["IdTarea"].toString()
                        if ((mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["Estado"].toString() != "5" &&
                            (mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["Estado"].toString() != "2" &&
                            (mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["Estado"].toString() != "3"
                        ) {
                            boolRecKm = true
                        } else {
                            boolRecKm = false
                        }
                    } else {
                        boolRecKm = false
                        recIdTarea = ""
                    }
                    intReg = 3
                    mDialog2!!.dismiss()
                    Log.e("tcgoReg", mapRegistroUs.toString())
                } else {
                    Log.e("tcgoRegistro", "")
                    val mapUsRec: MutableMap<String, Any> =
                        HashMap()
                    val mapUsTiem: MutableMap<String, Any?> =
                        HashMap()
                    mapUsRec["Estado"] = "0"
                    mapUsRec["IdTarea"] = "0"
                    mapUsRec["RecHoraFin"] = "0"
                    mapUsRec["RecHoraIn"] = "0"
                    mapUsRec["RecHoraInRet"] = "0"
                    mapUsRec["RecKmEst"] = "0"
                    mapUsRec["RecKmReal"] = "0"
                    mapUsRec["RecTiempoEst"] = "0"
                    mapUsRec["RecTiempoReal"] = "0"
                    mapUsTiem["FinAlmuerzo"] = null
                    mapUsTiem["FinBreack"] = null
                    mapUsTiem["FinJornada"] = null
                    mapUsTiem["FinPermiso"] = null
                    mapUsTiem["InicioAlmuerzo"] = null
                    mapUsTiem["InicioBreack"] = null
                    mapUsTiem["InicioJornada"] = null
                    mapUsTiem["InicioPermiso"] = null
                    mapRegistroUs!!["IdUsuario"] = documentUser!!["key"].toString()
                    mapRegistroUs!!["UserUID"] = documentUser!!["UserUID"].toString()
                    mapRegistroUs!!["idClient"] =
                        documentUser!!["idClientw"].toString()
                    mapRegistroUs!!["idClientW"] =
                        documentUser!!["idClientw"].toString()
                    mapRegistroUs!!["Recorrido"] = mapUsRec
                    mapRegistroUs!!["Tiempos"] = mapUsTiem
                    mapRegistroUs!!["dateLocation"] =
                        formatDateHora.format(Date())
                    dbRegistro!!.document(StrId!!).collection("Usuarios")
                        .add(mapRegistroUs!!)
                    intReg = 2
                    mDialog2!!.dismiss()
                }
            }
    }

    /*
    * 1=FAILED
    * 2=SUCCESS
    * */
    var intReg = 0
    var idDayReg: String? = null
    fun setRegFailed() {
        mDialog2!!.show()
        val mapDate: MutableMap<String, Any> = HashMap()
        Log.e("tcgoReg", "3")
        Log.e("tcgoReg", formatDate.format(Date()))
        mapDate["fecha"] = formatDate.format(Date())
        Log.e("tcgoReg", mapDate.toString())
        dbRegistro!!.add(mapDate)
        intReg = 0
        mDialog2!!.dismiss()
    }

    fun setRegistro() {
        mDialog2!!.show()
        Log.e("tcgoReg", "1")
        if (db == null) {
            setDbSettings()
        } else {
        }
        dbRegistro = db!!.collection("Registro")
        dbRegistro!!.whereEqualTo("fecha", formatDate.format(Date())).get()
            .addOnFailureListener {
                intReg = 1
                mDialog2!!.dismiss()
            }
            .addOnSuccessListener { querySnapshotReg ->
                if (querySnapshotReg.size() != 0) {
                    Log.e("tcgoReg", "4")
                    val snapshotReg = querySnapshotReg.documents[0]
                    idDayReg = snapshotReg.id
                    intReg = 2
                    mDialog2!!.dismiss()
                } else {
                    intReg = 1
                    mDialog2!!.dismiss()
                }
            }
    }

    const val REQUEST_TAKE_PHOTO = 1
    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp = formatDateHoraSeg2.format(Date())
        val imageFileName = "TCGO_" + timeStamp + "_"
        val storageDir = mContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        currentPhotoName = image.name
        Log.e("tcgoPhoto", storageDir.toString())
        return image
    }

    fun dispatchTakePictureIntent(fragment: Fragment) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            fragment.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }



    fun resizeBitmap(getBitmap: Bitmap?, maxSize: Int): Bitmap? {
        return try {
            val width = getBitmap!!.width
            var height = getBitmap.height
            val porcentaje: Double
            porcentaje = (height * 100 / width).toDouble()
            height = (maxSize * porcentaje).toInt() / 100
            Bitmap.createScaledBitmap(getBitmap, maxSize, height, false)
        } catch (e: Exception) {
            null
        }
    }

    fun saveOfflineImg() {
        mDialog2?.show()
        intReg = 4
        if (!isConnected) {
            val imgFile = File(currentPhotoPath)
            var listImage: ArrayList<String?>?
            try {
                listImage = Paper.book().read("listImage")
                if (listImage == null) listImage = ArrayList() else {
                }
            } catch (e: Exception) {
                listImage = ArrayList()
            }
            listImage!!.add(currentPhotoPath)
            Paper.book().write("listImage", listImage)
            var myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            myBitmap = resizeBitmap(myBitmap, 700)
            mDialog2!!.dismiss()
        } else {
            val file = File(currentPhotoPath)
            var bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            bitmap = resizeBitmap(bitmap, 700)
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val mountainsRef = storageRef.child(file.name)
            val uploadTask = mountainsRef.putBytes(data)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                Log.e("tcgoUpLoad", "UpLoadImage" + taskSnapshot.uploadSessionUri)
                mDialog2?.dismiss()
            }.addOnFailureListener {
                Toast.makeText(mContext, mContext?.getString(R.string.problems_upload_images), Toast.LENGTH_SHORT).show()
                mDialog2?.dismiss()
            }
        }
    }

    val isConnected: Boolean get() {
            return true
        }

    fun saveTask() {
        Log.e("tcgoImage", key?:"")
        mapTask?.let { dbServices?.document(key?:"")?.update(it) }

    }

    var mapArrayImg: ArrayList<Map<String, Any>>? = null
    var mapItemReq: MutableMap<String, Any?>? = null
    var listReq: ArrayList<Map<String, Any?>?>? = null
    var mapReq: MutableMap<String, Any?>? = null
    var mapFormStruct: MutableMap<String, Any?>? = null
    var position = 0
    var mStringX: Map<String, Any>? = null
    fun saveTaskReqPhoto() {
        val mapImg: MutableMap<String, Any> = HashMap()
        val mapLatLng: MutableMap<String, Any> = HashMap()
        mapLatLng["latitude"] = mLastLocation!!.latitude
        mapLatLng["longitude"] = mLastLocation!!.longitude
        mapImg["hora"] = formatHora.format(Date())
        mapImg["latLng"] = mapLatLng
        mapImg["url"] =
            "https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F" + currentPhotoName + "?alt=media"
        mapArrayImg!!.add(mapImg)
        mapItemReq!!["img"] = mapArrayImg
        listReq!![position] = mapItemReq
        mapReq!!["items"] = listReq
        mapFormStruct!![strReq!!] = mapReq
        mapTask = HashMap()
        mapTask?.set("formStruct", mapFormStruct)
        dbServices!!.document(mStringX!!["key"].toString()).update(mapTask as HashMap<String, Any?>)
    }

    fun saveTaskItemPhoto() {
        val mapImg: MutableMap<String, Any> = HashMap()
        val mapLatLng: MutableMap<String, Any> = HashMap()
        mapLatLng["latitude"] = mLastLocation!!.latitude
        mapLatLng["longitude"] = mLastLocation!!.longitude
        mapImg["hora"] = formatHora.format(Date())
        mapImg["latLng"] = mapLatLng
        mapImg["url"] =
            "https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F" + currentPhotoName + "?alt=media"
        mapArrayImg!!.add(mapImg)
        finalY!!["img"] = mapArrayImg
        if (cantGrup == 1) {
            arrayItems!![position - cantGrup] = finalY!!
            finalX!!["items"] = arrayItems
            arrayContenido!![(mStringX!!["itemClick"] as ArrayList<Int?>?)!![0]!!] =
                finalX!!
            mapFormStruct!!["contenido"] = arrayContenido
            mapTask!!["formStruct"] = mapFormStruct
        } else if (cantGrup == 2) {
            arrayItems!![position - cantGrup] = finalY!!
            finalX!!["items"] = arrayItems
            val mapSelGroup =
                arrayContenido!![(mStringX!!["itemClick"] as ArrayList<Int?>?)!![0]!!] as MutableMap<String, Any?>?
            val arrayGroup = mapSelGroup!!["grupItems"] as ArrayList<Map<String, Any?>?>?
            arrayGroup!![(mStringX!!["itemClick"] as ArrayList<Int?>?)!![1]!!] =
                finalX
            mapSelGroup["grupItems"] = arrayGroup
            mapFormStruct!!["contenido"] = mapSelGroup
            mapTask!!["formStruct"] = mapFormStruct
        }
        key = mStringX!!["key"].toString()
        saveTask()
    }

    fun saveImgHome() {
        mDialog2?.show()
        var mapUpdate: MutableMap<String?, Any?>? = HashMap()
        val mapImg: MutableMap<String, Any> = HashMap()
        val mapLatLng: MutableMap<String, Any> = HashMap()
        if (mapImgHome == null) { mapImgHome = HashMap() }
        mapLatLng["latitude"] = mLastLocation?.latitude ?: 0.0
        mapLatLng["longitude"] = mLastLocation?.longitude ?: 0.0
        mapImg["hora"] = formatHora.format(Date())
        mapImg["latLng"] = mapLatLng
        mapImg["url"] = "https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F" + currentPhotoName + "?alt=media"
        if (boolTypePhoto) {
            boolTypePhoto = false
            when (intTypeImg) {
                0 -> {
                    txtImgHome?.let { mapImgHome!!.put(it, mapImg) }
                    mapUpdate?.set("img", mapImgHome)
                    mapUpdate?.let { dbRegistroUs!!.document(mapRegistroUs!!["key"].toString()).update(it) }
                }
                1 -> {
                    documentUser!!["fotoConductor"] = mapImg
                    dbDriversInformation!!.document(Objects.requireNonNull(documentUser?.get("key")).toString()).update(documentUser!!)
                }
                2 -> {
                    mapUpdate = documentUser!!["images"] as MutableMap<String?, Any?>?
                    mapUpdate!![txtImgHome] = mapImg
                    documentUser!!["images"] = mapUpdate
                    dbDriversInformation!!.document(documentUser!!["key"].toString()).update(documentUser!!)
                }
            }
        } else {
            txtImgHome?.let { mapImgHome!!.put(it, mapImg) }
            mapUpdate!!["img"] = mapImgHome
            ifSaveImage = true
            dbRegistroUs!!.document(mapRegistroUs!!["key"].toString()).update(
                    mapUpdate
            )
        }
        intReg = 5
        mDialog2?.dismiss()
    }

    fun saveTaskEsphoto() {
        val mapImg: MutableMap<String, Any> = HashMap()
        val mapLatLng: MutableMap<String, Any> = HashMap()
        mapLatLng["latitude"] = mLastLocation!!.latitude
        mapLatLng["longitude"] = mLastLocation!!.longitude
        mapImg["hora"] = formatHora.format(Date())
        mapImg["latLng"] = mapLatLng
        mapImg["url"] = "https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F" + currentPhotoName + "?alt=media"
        Log.e("tcgoappImage", "https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F" + currentPhotoName + "?alt=media")
        mapImgNormal!![txtImgHome] = mapImg
        val mapHistoryDates = selectedService!!["historial"] as MutableMap<String, Any?>?
        val mapHistory = mapHistoryDates!![formatDate.format(Date())] as MutableMap<String, Any?>?
        mapHistory!!["img"] = mapImgNormal
        mapHistoryDates[formatDate.format(Date())] = mapHistory
        selectedService!!["historial"] = mapHistoryDates
        mapTask = HashMap()
        mapTask?.set("historial", mapHistoryDates)
        key = selectedService!!["key"].toString()
        saveTask()
    }
}