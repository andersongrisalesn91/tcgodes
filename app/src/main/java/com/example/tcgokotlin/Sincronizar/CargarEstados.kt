package com.example.tcgokotlin.Sincronizar


import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.tcgokotlin.R
import kotlinx.android.synthetic.main.activity_sincronizar.*
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.utils.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.content_driver_home.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CargarEstados() {
    
    private var canitems = 0

    fun ActualizarServices(contexto: Context) {
        val fg = FuncionesGenerales()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val val_key = fg.key_act(contexto)
        val estado_act = fg.estado_act(contexto)
        val standbytxt = fg.standby_act(contexto)
        val fecha_act = fg.fechaActual(1)
        val standby_act = if (standbytxt != "0") {
            "1"
        } else {
            "0"
        }
        val estado = db.collection("Services").document(val_key)
        
        estado
            .update("estado", estado_act)
            .addOnSuccessListener { Log.i("Estado Actualizado a:", estado_act) }
            .addOnFailureListener { e -> Log.i("Estado,sin Act err:", e.toString()) }

        val standby = db.collection("Services").document(val_key)
        
        standby
            .update("standBy", standby_act)
            .addOnSuccessListener { Log.i("standby Actualizado a:", standby_act) }
            .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }

        if (estado_act == "Finalizado") {
            val fechafin = db.collection("Services").document(val_key)
            
            fechafin
                .update("ejecucion.fechafin", fg.fechaActual(1))
                .addOnSuccessListener { Log.i("standby Actualizado a:", standby_act) }
                .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }

            val fechafinreg = db.collection("Services").document(val_key)

            fechafinreg
                .update("historial.$fecha_act.TareaHoraFin", fg.fechaActual(1))
                .addOnSuccessListener { Log.i("standby Actualizado a:", standby_act) }
                .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }

            val abierta = db.collection("Services").document(val_key)
            
            abierta
                .update("ejecucion.abierta", "0")
                .addOnSuccessListener { Log.i("standby Actualizado a:", standby_act) }
                .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }
        }
    }

    fun ActualizarServicesp(contexto: Context,idtarea:String) {
        val fg = FuncionesGenerales()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val val_key = fg.getQ1(contexto,"select valkey from '200_TAREAS' where idtarea='$idtarea'")
        val estado_act = fg.getQ1(contexto,"select estado from '200_TAREAS' where idtarea='$idtarea'")
        val standbytxt = fg.getQ1(contexto,"select standby from '200_TAREAS' where idtarea='$idtarea'")
        val standby_act = if (standbytxt != "0") {
            "1"
        } else {
            "0"
        }
        val estado = db.collection("Services").document(val_key)

        estado
            .update("estado", estado_act)
            .addOnSuccessListener { Log.i("Estado Actualizado a:", estado_act) }
            .addOnFailureListener { e -> Log.i("Estado,sin Act err:", e.toString()) }

        val standby = db.collection("Services").document(val_key)

        standby
            .update("standBy", standby_act)
            .addOnSuccessListener { Log.i("standby Actualizado a:", standby_act) }
            .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }

        if (estado_act == "Finalizado") {
            val fechafin = db.collection("Services").document(val_key)

            fechafin
                .update("ejecucion.fechafin", fg.fechaActual(1))
                .addOnSuccessListener { Log.i("standby Actualizado a:", standby_act) }
                .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }

            val abierta = db.collection("Services").document(val_key)

            abierta
                .update("ejecucion.abierta", "0")
                .addOnSuccessListener { Log.i("standby Actualizado a:", standby_act) }
                .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }
        }
    }


    fun ActualizarEstado(contexto: Context) {
        val fg = FuncionesGenerales()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val val_key = fg.key_act(contexto)
        val estado_act = fg.estado_act(contexto)
        val estado = db.collection("Services").document(val_key.toString())
        
        estado
            .update("estado", estado_act.toString())
            .addOnSuccessListener { Log.i("Estado Actualizado a:", estado_act) }
            .addOnFailureListener { e -> Log.i("Estado,sin Act err:", e.toString()) }
    }

    fun ActualizarhistoryNotes(contexto: Context) {
        val fg = FuncionesGenerales()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val val_key = fg.key_act(contexto)
        val fecha_act = fg.fechaActual(1)
        val estado_act = fg.estado_act(contexto)
        val conGen = ConsultaGeneral()
        var listHistoryNote: java.util.ArrayList<Any>?

        val histnotes = db.collection("Services").document(val_key.toString())
        val querryNotas = "Select idnote,iduser,nota,tipouser from '206_HISTORY_NOTES'" +
                " where idtarea='" + fg.tarea_act(contexto) + "' and fecha='" + fecha_act + "' order by 1"
        val registros = conGen.queryObjeto2val(contexto, querryNotas, null)

        if (registros.isNotEmpty()){
            listHistoryNote = ArrayList()
            var itemnota: MutableMap<String, Any>
            for (op in registros.indices) {
                itemnota = java.util.HashMap()
                itemnota["iduser"] = registros[op][1]
                itemnota["nota"] = registros[op][2]
                itemnota["tipouser"] = registros[op][3]
                listHistoryNote.add(itemnota)
                canitems++
            }
            histnotes
                .update("historial.$fecha_act.historyNote", listHistoryNote)
                .addOnSuccessListener { Log.i("Notas Actualizadas: ", "ok") }
                .addOnFailureListener { e -> Log.i("Notas no Actualizadas", e.toString()) }
        }
    }

    fun act_Exitosa_NoExitosa(contexto: Context, estado: String, tipoNovedad: String) {
        val fg = FuncionesGenerales()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val val_key = fg.key_act(contexto)
        val fecha_act = fg.fechaActual(1)
        val key = fg.estado_act(contexto)

        val prsnoved = if (estado == "2") "1" else "0"
        val varabierta = if (estado == "2") "0" else "1"

        val exitosa = db.collection("Services").document(val_key)
        exitosa
            .update("historial.$fecha_act.exitosa", estado)
            .addOnSuccessListener { Log.i("Exitosa Actualizado a:", estado) }
            .addOnFailureListener { e -> Log.i("Exitosa,sin Act err:", e.toString()) }

        val novedad = db.collection("Services").document(val_key)
        novedad
            .update("prsNoved", prsnoved)
            .addOnSuccessListener { Log.i("prsNoved Actualizado a:", prsnoved) }
            .addOnFailureListener { e -> Log.i("prsNoved,sin Act err:", e.toString()) }

        val tiponov = db.collection("Services").document(val_key)
        tiponov
            .update("historial.$fecha_act.tipNov", "4")
            .addOnSuccessListener { Log.i("tipNov Actualizado a:", tipoNovedad) }
            .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }

        val motivonoexit = db.collection("Services").document(val_key)
        motivonoexit
            .update("historial.$fecha_act.motivoNoEx", tipoNovedad)
            .addOnSuccessListener { Log.i("tipNov Actualizado a:", tipoNovedad) }
            .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }

        val abierta = db.collection("Services").document(val_key)
        abierta
            .update("ejecucion.abierta", varabierta)
            .addOnSuccessListener { Log.i("standby Actualizado a:", varabierta) }
            .addOnFailureListener { e -> Log.i("standby,sin Act err:", e.toString()) }
    }

    fun act_variabledoc(contexto: Context, variable: String, coleccion: String , documento:String ,campo:String, valor:String) {
        val fg = FuncionesGenerales()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val variable = db.collection(coleccion).document(documento)
        
        variable
            .update(campo, valor)
            .addOnSuccessListener { Log.i("Actualizo:", campo) }
            .addOnFailureListener { e -> Log.i("F. Actualizar" + campo, e.toString()) }
    }

    fun reload_infogen(contexto: Context) {
        val fg = FuncionesGenerales()
        val sincO = SincronizarOnline()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        var regis: MutableMap<String, *>
        val docid = fg.useruid(contexto) + "_" + fg.fechaActual(1)
        val registrouserall = db.collection("101_REGISTRO").document(docid)
        registrouserall.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val iniciojornada = document["iniciojornada"]
                    val finjornada = document["finjornada"]
                    val iniciobreak = document["iniciobreak"]
                    val finbreak = document["finbreak"]
                    val inicioalmuerzo = document["inicioalmuerzo"]
                    val finalmuerzo = document["finalmuerzo"]
                    val iniciopermiso = document["iniciopermiso"]
                    val finpermiso = document["finpermiso"]
                    val vh1nfoto = document["vh1nfoto"]
                    val vh1hora = document["vh1hora"]
                    val vh1lat = document["vh1lat"]
                    val vh1lon = document["vh1lon"]
                    val vh2nfoto = document["vh2nfoto"]
                    val vh2hora = document["vh2hora"]
                    val vh2lat = document["vh2lat"]
                    val vh2lon = document["vh2lon"]
                    val km1nfoto = document["km1nfoto"]
                    val km1hora = document["km1hora"]
                    val km1lat = document["km1lat"]
                    val km1lon = document["km1lon"]
                    val km2nfoto = document["km2nfoto"]
                    val km2hora = document["km2hora"]
                    val km2lat = document["km2lat"]
                    val km2lon = document["km2lon"]
                    val gpslat = document["gpslat"]
                    val gpslon = document["gpslon"]
                    val sinc = document["sinc"]
                    val vh1sinc = document["vh1sinc"]
                    val vh2sinc = document["vh2sinc"]
                    val km1sinc = document["km1sinc"]
                    val km2sinc = document["km2sinc"]
                    val sqlact = "update '101_REGISTRO' set " +
                            "iniciojornada = '" + iniciojornada + "' , " +
                            "finjornada = '" + finjornada + "' , " +
                            "iniciobreak = '" + iniciobreak + "' , " +
                            "finbreak = '" + finbreak + "' , " +
                            "inicioalmuerzo = '" + inicioalmuerzo + "' , " +
                            "finalmuerzo = '" + finalmuerzo + "' , " +
                            "iniciopermiso = '" + iniciopermiso + "' , " +
                            "finpermiso = '" + finpermiso + "' , " +
                            "vh1nfoto = '" + vh1nfoto + "' , " +
                            "vh1hora = '" + vh1hora + "' , " +
                            "vh1lat = '" + vh1lat + "' , " +
                            "vh1lon = '" + vh1lon + "' , " +
                            "vh2nfoto = '" + vh2nfoto + "' , " +
                            "vh2hora = '" + vh2hora + "' , " +
                            "vh2lat = '" + vh2lat + "' , " +
                            "vh2lon = '" + vh2lon + "' , " +
                            "km1nfoto = '" + km1nfoto + "' , " +
                            "km1hora = '" + km1hora + "' , " +
                            "km1lat = '" + km1lat + "' , " +
                            "km1lon = '" + km1lon + "' , " +
                            "km2nfoto = '" + km2nfoto + "' , " +
                            "km2hora = '" + km2hora + "' , " +
                            "km2lat = '" + km2lat + "' , " +
                            "km2lon = '" + km2lon + "' , " +
                            "gpslat = '" + gpslat + "' , " +
                            "gpslon = '" + gpslon + "' , " +
                            "sinc = '" + sinc + "' , " +
                            "vh1sinc = '" + vh1sinc + "' , " +
                            "vh2sinc = '" + vh2sinc + "' , " +
                            "km1sinc = '" + km1sinc + "' , " +
                            "km2sinc = '" + km2sinc + "' " +
                            "where useruid='" +  fg.useruid(contexto) + "' and fecha='" + fg.fechaActual(1) + "';"
                    fg.ejecDB(contexto,sqlact)
                } else {
                    sincO.sincronizarRegistro101(contexto)
                    Log.i("Documento no existe", "Fallo el cargue")
                }
            }
            .addOnFailureListener { exception ->
                sincO.sincronizarRegistro101(contexto)
                Log.d(TAG, "get failed with ", exception)
            }
    }


}