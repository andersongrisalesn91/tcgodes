package com.example.tcgokotlin.Sincronizar


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
import androidx.navigation.fragment.findNavController
import com.example.tcgokotlin.R
import kotlinx.android.synthetic.main.activity_sincronizar.*
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import dmax.dialog.SpotsDialog
import java.io.File
import java.util.*

class SincronizarOnline() {
    var tareaAct: String = ""
    var useruid: String = ""
    val storage = FirebaseStorage.getInstance()
    var file: Uri? = null
    var iv: ImageView? = null
    var regexitoso: String = "0"
    var uidexitoso: String = "0"
    val mStorageRef = storage.reference.child("img")

    fun sincronizarTablasVF(contexto: Context) {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val ce = CargarEstados()
        var conteodatosh = 0
        //Tomar los datos de cada tabla y enviarlos a tablas a Firebase
        //Traer todos los registros de cada tabla e insertarlos individualmente en Firebase
        tareaAct = fg.tarea_act(contexto)
        useruid = fg.useruid(contexto)
        var concat = ""
        var conteodatos = 0
        val vc = VerificarConex()
        val net = vc.revisarconexión(contexto)
        if (net) {
            try {
                regexitoso = "1"
                sincronizarTablasG(contexto, "400_SINCRONIZAR", "200_TAREAS", "idtarea", tareaAct)
            } finally {
                ce.ActualizarServicesp(contexto,tareaAct)
                try{
                    sincronizarRegistro101(contexto)
                }finally {
                    sincronizarHistorial102(contexto)
                }
            }
        } else {
            Toast.makeText(
                contexto,
                "No se encuentra conectado a Internet, Recuerde Sincronizar manualmente",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    fun sincronizarTablasG(
        contexto: Context,
        tablasinc: String,
        ntabla: String,
        ncampo: String,
        valor_filtro: String
    ) {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val db = FirebaseFirestore.getInstance()
        val ntablasig = fg.tcsig(contexto, tablasinc, ntabla.toString(), 1)
        val ncamposig = fg.tcsig(contexto, tablasinc, ntabla.toString(), 2)
        val idtabla = fg.idtablaact(contexto, tablasinc, ntabla.toString())

        val campos = conGen.queryObjeto(contexto, "pragma table_info('$ntabla')", null)
        val registros = conGen.queryObjeto(
            contexto,
            "SELECT * FROM '$ntabla' where $ncampo = '$valor_filtro'",
            null
        )
        val tablag: MutableMap<String, Any> = HashMap()
        var regis: MutableMap<String?, Any?>
        if (registros != null) {
            for (i in registros.indices) {
                regis = HashMap()
                for (x in campos?.indices!!) {
                    regis[campos[x][1]] = registros[i][x]
                }
                tablag[valor_filtro + "_" + i] = regis
            }
            try {
                db.collection(ntabla).document(valor_filtro).set(tablag)
                    .addOnSuccessListener {
                        if (ntabla != "200_TAREAS") {
                            fg.ejecDB(
                                contexto,
                                "UPDATE '$ntabla' SET sinc='1' WHERE $ncampo='$valor_filtro';"
                            )
                        }
                    }
            } catch (e: Exception) {
                db.collection(ntabla).document(valor_filtro).set(tablag)
                    .addOnSuccessListener {
                        if (ntabla != "200_TAREAS") {
                            fg.ejecDB(
                                contexto,
                                "UPDATE '$ntabla' SET sinc='1' WHERE $ncampo='$valor_filtro';"
                            )
                        }
                    }.addOnFailureListener {
                        regexitoso = "0"
                        fg.ejecDB(
                            contexto,
                            "UPDATE '$ntabla' SET sinc='0' WHERE $ncampo='$valor_filtro';"
                        )
                    }
            } finally {
                tablag.clear()
                if (!ntablasig.equals("0")) {
                    sincronizarTablasG(contexto, tablasinc, ntablasig, ncamposig, valor_filtro)
                } else if (tablasinc == "400_SINCRONIZAR" && regexitoso == "1") {
                    fg.ejecDB(
                        contexto,
                        "UPDATE '200_TAREAS' SET sinc='1' WHERE idtarea='$valor_filtro';"
                    )
                }
            }
        } else {
            if (!ntablasig.equals("0")) {
                sincronizarTablasG(contexto, tablasinc, ntablasig, ncamposig, valor_filtro)
            } else if (tablasinc == "400_SINCRONIZAR" && regexitoso == "1") {
                fg.ejecDB(
                    contexto,
                    "UPDATE '200_TAREAS' SET sinc='1' WHERE idtarea='$valor_filtro';"
                )
            }
        }
    }

    fun sincronizarRegistro101(
        contexto: Context
    ) {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val db = FirebaseFirestore.getInstance()
        val filtro = fg.useruid(contexto) + "_" + fg.fechaActual(1)
        val ntabla = "101_REGISTRO"
        val campos = conGen.queryObjeto(contexto, "pragma table_info('101_REGISTRO')", null)
        val registros = conGen.queryObjeto(
            contexto,
            "SELECT * FROM '101_REGISTRO' where useruid || '_' || fecha = '" + filtro + "'",
            null
        )
        var regis: MutableMap<String?, Any?>
        if (registros != null) {
            for (i in registros.indices) {
                regis = HashMap()
                for (x in campos?.indices!!) {
                    regis[campos[x][1]] = registros[i][x]
                }
                try {
                    db.collection(ntabla).document(filtro).set(regis)
                        .addOnSuccessListener {
                            Log.i("Registro cargado", "Exitoso")
                        }
                } catch (e: Exception) {
                    db.collection(ntabla).document(filtro).set(regis)
                        .addOnSuccessListener {
                            Log.i("Registro cargado", "Exitoso")
                        }.addOnFailureListener {
                            Log.i("Registro cargado", "Fallo")
                        }
                }
                regis.clear()
            }
        }
    }

    fun sincronizarHistorial102(
        contexto: Context
    ) {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val db = FirebaseFirestore.getInstance()
        val filtro = fg.useruid(contexto) + "_" + fg.fechaActual(1)
        val ntabla = "102_HISTORIAL"
        val campos = conGen.queryObjeto(contexto, "pragma table_info('102_HISTORIAL')", null)
        val registros = conGen.queryObjeto(
            contexto,
            "SELECT * FROM '102_HISTORIAL' where useruid || '_' || fecha = '" + filtro + "' order by fcr asc",
            null
        )
        var regis: MutableMap<String?, Any?>
        if (registros != null) {
            for (i in registros.indices) {
                regis = HashMap()
                for (x in campos?.indices!!) {
                    regis[campos[x][1]] = registros[i][x]
                }
                regis["ordenreg"] = i
                try {
                    db.collection(ntabla).document(filtro + "_" + i).set(regis)
                        .addOnSuccessListener {
                            Log.i("Historial cargado", "Exitoso")
                        }
                } catch (e: Exception) {
                    db.collection(ntabla).document(filtro).set(regis)
                        .addOnSuccessListener {
                            Log.i("Historial cargado", "Exitoso")
                        }.addOnFailureListener {
                            Log.i("Historial cargado", "Fallo")
                        }
                }
                regis.clear()
            }
        }
    }

    fun sincronizarTablasH(
        contexto: Context,
        tablasinc: String,
        ntabla: String,
        ncampo: String,
        valor_filtro: String
    ) {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val db = FirebaseFirestore.getInstance()
        val ntablasig = fg.tcsig(contexto, tablasinc, ntabla.toString(), 1)
        val ncamposig = fg.tcsig(contexto, tablasinc, ntabla.toString(), 2)
        val idtabla = fg.idtablaact(contexto, tablasinc, ntabla.toString())

        val campos = conGen.queryObjeto(contexto, "pragma table_info('$ntabla')", null)
        val registros = conGen.queryObjeto(
            contexto,
            "SELECT * FROM '$ntabla' where $ncampo = '$valor_filtro'",
            null
        )
        var regis: MutableMap<String?, Any?>
        if (registros != null) {
            for (i in registros.indices) {
                regis = HashMap()
                for (x in campos?.indices!!) {
                    regis[campos[x][1]] = registros[i][x]
                }
                try {
                    db.collection(ntabla).document(valor_filtro + "_" + i).set(regis)
                        .addOnSuccessListener {
                            fg.ejecDB(
                                contexto,
                                "UPDATE '$ntabla' SET sinc='1' WHERE $ncampo='$valor_filtro';"
                            )
                        }
                } catch (e: Exception) {
                    db.collection(ntabla).document(valor_filtro + "_" + i).set(regis)
                        .addOnSuccessListener {
                            fg.ejecDB(
                                contexto,
                                "UPDATE '$ntabla' SET sinc='1' WHERE $ncampo='$valor_filtro';"
                            )
                        }.addOnFailureListener {
                            fg.ejecDB(
                                contexto,
                                "UPDATE '$ntabla' SET sinc='0' WHERE $ncampo='$valor_filtro';"
                            )
                        }
                }
                regis.clear()
            }

            if (!ntablasig.equals("0")) {
                sincronizarTablasG(contexto, tablasinc, ntablasig, ncamposig, valor_filtro)
            }
        } else {
            if (!ntablasig.equals("0")) {
                sincronizarTablasG(contexto, tablasinc, ntablasig, ncamposig, valor_filtro)
            }
        }
    }

    fun cargarFotoUnica(contexto: Context, nfoto: String, sqlquery: String) {
        val fg = FuncionesGenerales()
        val vc = VerificarConex()
        val net = vc.revisarconexión(contexto)
        if (net == true) {
            val nombreFoto = nfoto
            val nfoto = Environment.getExternalStorageDirectory()
                .toString() + "/TCGO_FILES/IMG/" + nombreFoto
            val f = File(nfoto)
            if (f.exists() && f.length() > 0) {
                val file = Uri.fromFile(f)
                if (file != null) {
                    val picRef = mStorageRef.child(nombreFoto)
                    val uploadt = picRef.putFile(file)
                    uploadt.addOnSuccessListener { taskSnapshot ->
                        val url = taskSnapshot.metadata?.path
                        if (url == "" || url == null) {
                            fg.ejecDB(contexto, sqlquery)
                            Toast.makeText(
                                contexto,
                                "Foto cargada al servidor",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            contexto,
                            "Foto no cargada, recuerde realizar el cargue manual de la foto",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                contexto,
                "No hay red disponible para cargar fotos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}