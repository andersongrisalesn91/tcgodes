package com.example.tcgokotlin.Sincronizar


import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tcgokotlin.R
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_sincronizar.*
import kotlinx.android.synthetic.main.sincronizando_layout.*
import java.io.File
import java.util.*

class Sincronizar : Fragment() {
    var ssin: TextView? = null
    var cont: TextView? = null
    var tsinc: TextView? = null
    var fssin: TextView? = null
    var fcont: TextView? = null
    var vtsinc: TextView? = null
    var tareaAct: String = ""
    var histact: String = ""
    var conteosin: String = ""
    var conteosT: String = ""
    var conteosFsin: String = ""
    var conteosFT: String = ""
    var regexitoso: String = "0"
    var uidexitoso: String = "0"
    val storage = FirebaseStorage.getInstance()
    val mStorageRef = storage.reference.child("img")
    var file: Uri? = null
    var dialog: AlertDialog? = null
    var show: androidx.appcompat.app.AlertDialog? = null
    var pbregistros: ProgressBar? = null
    var pbfotos: ProgressBar? = null
    var iv: ImageView? = null
    var sincd: Button? = null
    var sincf: Button? = null
    var cfsin = 0
    var ctx = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.activity_sincronizar, container, false)
    }


    override fun onStart() {
        super.onStart()
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        sincd = buttonSincronizar2
        sincf = buttonSincronizar3

        val QueryConteosForm =
            "select count(idtarea) ct from (select idtarea from '200_TAREAS' where (estado='Finalizado' or estado='No Exitosa' or estado='STD/BY') and sinc=0 or sinc is null GROUP by idtarea) as TarNoSync"
        val ConteoForm = conGen.queryObjeto2val(requireContext(), QueryConteosForm, null)
        val QueryConteosFormT = "select count(idtarea) ct from (select idtarea from '200_TAREAS' GROUP by idtarea) as TarTotal"
        val ConteoFormT = conGen.queryObjeto2val(requireContext(), QueryConteosFormT, null)
        val QueryConteosFForm =
            "SELECT COUNT(nfoto) AS cant FROM (select nfoto from '302_FOTOS_RESP' WHERE (nfoto<>'' and nfoto<>'0') and sincf=0 OR sincf is null group by nfoto) as fsinc"
        val ConteoFForm = conGen.queryObjeto2val(requireContext(), QueryConteosFForm, null)
        val QueryConteosFFormT =
            "SELECT COUNT(nfoto) AS cant FROM (select nfoto from '302_FOTOS_RESP' where (nfoto<>'' and nfoto<>'0') group by nfoto) as fsinc"
        val ConteoFFormT = conGen.queryObjeto2val(requireContext(), QueryConteosFFormT, null)
        conteosin = ConteoForm!![0][0]
        if (conteosin == "0"){
            buttonSincronizar2.visibility = View.GONE
        }else{
            buttonSincronizar2.visibility = View.VISIBLE
        }
        conteosT = ConteoFormT!![0][0]
        conteosFsin = ConteoFForm!![0][0]
        val fotospend = fg.getQ1(requireContext(),
            "SELECT COUNT(nfoto) AS cant FROM (select nfoto from '302_FOTOS_RESP' WHERE sincf=0 OR sincf is null group by nfoto) as fsinc")
        if (fotospend == "0"){
            buttonSincronizar3.visibility = View.GONE
        }else{
            buttonSincronizar3.visibility = View.VISIBLE
        }
        conteosFT = ConteoFFormT!![0][0]
        pbfotos  = progressBarTF
        pbregistros = progressBarTT
        ssin = tVTotalSSN
        cont = tVTotalFN
        fssin = tVTotalFSSN
        fcont = tVTotalFCN
        tsinc = tVNTSINC
        vtsinc = tVPorcSinc
        ssin!!.text = conteosin
        cont!!.text = conteosT
        fssin!!.text = conteosFsin
        fcont!!.text = conteosFT
        iv = ivFotosSinc
        val fotos = conGen.queryObjeto2val(
            requireContext(),
            "SELECT COUNT(nfoto) AS cant FROM '302_FOTOS_RESP' WHERE nfoto<>'null' AND sincf=0",
            null
        )
        if (fotos != null) {
            val cant = fotos[0][0]
            if (cant != null) {
                if (cant == "0") {
                    //OK
                    iv!!.setImageResource(R.drawable.check_opt)
                } else {
                    //Falta
                    iv!!.setImageResource(R.drawable.equis_opt)
                }
            }
        }
        buttonSincronizar2.setOnClickListener {
            sincronizar()
        }
        buttonSincronizar3.setOnClickListener {
            sincronizarF()
        }
        buttonEliminarBK.setOnClickListener {
            eliminarbk()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    fun eliminarbk() {
        val bk = Backups()
        bk.Borrarbackup_viejos(requireContext())
    }

    fun sincronizar() {
        try {
            alertdiag()
        }finally {
            sincronizarTablasVF()
        }
    }

    private fun popupSincronizar() {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popUp = inflater.inflate(R.layout.sincronizando_layout, null)

        val cancel = popUp.findViewById<View>(R.id.btnCancelarSinc) as TextView
        val popupWindow = PopupWindow(popUp, LinearLayout.LayoutParams.MATCH_PARENT, 500, true)
        cancel.setOnClickListener {
            findNavController().navigate(R.id.navigation_Sinc)
            popupWindow.dismiss()
        }
        popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
        popupWindow.showAtLocation(popUp, Gravity.CENTER, 0, 0)
    }

    fun alertdiag(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("SINCRONIZACIÓN")
        builder.setMessage("Sincronización en proceso por favor espere, esta pantalla desaparecera al  terminar la sincronización")
        dialog = builder.create()
        dialog!!.show()
    }

    fun sincronizarF() {
        val fg = FuncionesGenerales()
        try{
            alertdiag()
        }finally {
            try{
                clicbt("1")
                val valc1 = fg.getQ1(requireContext(),
                    "select ifnull(vh1nfoto,0) as cf from '101_REGISTRO' where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                val valc2 = fg.getQ1(requireContext(),
                    "select ifnull(vh2nfoto,0) as cf from '101_REGISTRO' where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                val valc3 = fg.getQ1(requireContext(),
                    "select ifnull(km1nfoto,0) as cf from '101_REGISTRO' where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                val valc4 = fg.getQ1(requireContext(),
                    "select ifnull(km2nfoto,0) as cf from '101_REGISTRO' where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                val valsc1 = fg.getQ1(requireContext(),
                    "select ifnull(vh1sinc,0) as cf from '101_REGISTRO' where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                val valsc2 = fg.getQ1(requireContext(),
                    "select ifnull(vh2sinc,0) as cf from '101_REGISTRO' where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                val valsc3 = fg.getQ1(requireContext(),
                    "select ifnull(km1sinc,0) as cf from '101_REGISTRO' where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                val valsc4 = fg.getQ1(requireContext(),
                    "select ifnull(km2sinc,0) as cf from '101_REGISTRO' where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                if (valsc1 == "0" && valc1 != "0" && valc1 != "" && valc1 != "null") cargarFotoUnica(
                    requireContext(),
                    valc1,
                    "update '101_REGISTRO' set vh1sinc=1 where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'") else fg.ejecDB(
                    requireContext(),
                    "update '101_REGISTRO' set vh1sinc=1 where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                if (valsc2 == "0" && valc2 != "0" && valc2 != "" && valc2 != "null") cargarFotoUnica(
                    requireContext(),
                    valc2,
                    "update '101_REGISTRO' set vh2sinc=1 where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'") else fg.ejecDB(
                    requireContext(),
                    "update '101_REGISTRO' set vh2sinc=1 where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                if (valsc3 == "0" && valc3 != "0" && valc3 != "" && valc3 != "null") cargarFotoUnica(
                    requireContext(),
                    valc3,
                    "update '101_REGISTRO' set km1sinc=1 where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'") else fg.ejecDB(
                    requireContext(),
                    "update '101_REGISTRO' set km1sinc=1 where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                if (valsc4 == "0" && valc4 != "0" && valc4 != "" && valc4 != "null") cargarFotoUnica(
                    requireContext(),
                    valc4,
                    "update '101_REGISTRO' set km2sinc=1 where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'") else fg.ejecDB(
                    requireContext(),
                    "update '101_REGISTRO' set km2sinc=1 where useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
            }finally {
                guardarFotosR()
            }
        }
    }

    fun guardarFotosR() {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        var mod = 20
        val registros = conGen.queryObjeto(
            requireContext(),
            "SELECT nfoto FROM '302_FOTOS_RESP' where (nfoto<>'' and nfoto<>'0') and sincf is null or sincf=0",
            null
        )
        if (registros != null) {
            var cantRegistros = registros.size
            if (cantRegistros > 79) {
                mod = if (cantRegistros >= 1000) {
                    200
                } else if (cantRegistros < 240) {
                    60
                } else if (cantRegistros < 600) {
                    100
                } else {
                    150
                }
            }

            for (r in registros.indices) {
                val vc = VerificarConex()
                val net = vc.revisarconexión(requireContext())
                if (net == true) {
                    if (r % mod == 0) {
                        Toast.makeText(
                            requireContext(),
                            "Sincronizando las fotos, por favor espere",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val nombreFoto = registros[r][0].toString()
                    val nfoto = Environment.getExternalStorageDirectory()
                        .toString() + "/TCGO_FILES/IMG/" + nombreFoto
                    val f = File(nfoto)
                    if (f.exists() && f.length() > 0) {
                        val file = Uri.fromFile(f)
                        if (file != null) {
                            if(nombreFoto != null && nombreFoto != "" && nombreFoto != "0"){
                                val picRef = mStorageRef.child(nombreFoto)
                                val uploadt = picRef.putFile(file)
                                uploadt.addOnSuccessListener { taskSnapshot ->
                                    val url = taskSnapshot.metadata?.path
                                    if (url == "" || url == null) {
                                        fg.ejecDB(requireContext(),"UPDATE '302_FOTOS_RESP' SET sincf='0' WHERE nfoto='$nombreFoto';")
                                    } else {
                                        cfsin++
                                        val cfs = "" + (conteosFsin.toInt() - cfsin)
                                        fssin?.text = cfs
                                        fg.ejecDB(requireContext(),"UPDATE '302_FOTOS_RESP' SET sincf='1' WHERE nfoto='$nombreFoto';")
                                        if (cfs == "0") {
                                            clicbt("2")
                                            iv?.setImageResource(R.drawable.check_opt)
                                            Toast.makeText(
                                                requireContext(),
                                                "Sincronizacion de Fotos Finalizada",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            dialog!!.dismiss()
                                            findNavController().navigate(R.id.navigation_Sinc)
                                        }
                                    }
                                }.addOnFailureListener {
                                    fg.ejecDB(requireContext(),
                                        "UPDATE '302_FOTOS_RESP' SET sincf='0' WHERE nfoto='$nombreFoto';")
                                }.addOnProgressListener { taskSnapshot -> updateProgress(taskSnapshot)
                                }
                            }else {
                                fg.ejecDB(requireContext(),
                                    "UPDATE '302_FOTOS_RESP' SET sincf='2' WHERE nfoto='$nombreFoto';")
                            }
                        } else {
                            fg.ejecDB(requireContext(),
                                "UPDATE '302_FOTOS_RESP' SET sincf='2' WHERE nfoto='$nombreFoto';")
                        }
                    } else {
                        fg.ejecDB(requireContext(),
                            "UPDATE '302_FOTOS_RESP' SET sincf='2' WHERE nfoto='$nombreFoto';")
                    }
                } else {
                    clicbt("2")
                    Toast.makeText(
                        requireContext(),
                        "No hay red disponible para cargar fotos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog!!.dismiss()
            findNavController().navigate(R.id.navigation_Sinc)
        } else {
            clicbt("2")
            iv?.setImageResource(R.drawable.check)
            Toast.makeText(requireContext(), "No hay fotos por Sincronizar", Toast.LENGTH_SHORT).show()
            dialog!!.dismiss()
            findNavController().navigate(R.id.navigation_Sinc)
        }
    }

    fun updateProgress(taskSnapshot: UploadTask.TaskSnapshot) {
        val fileSize = taskSnapshot.totalByteCount
        val uploadBytes = taskSnapshot.bytesTransferred
        val progress: Int
        progress = Math.round((100 * uploadBytes / fileSize).toFloat())
        pbfotos?.progress = progress
    }

    fun sincronizarTablasVF() {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        //Tomar los datos de cada tabla y enviarlos a tablas a Firebase
        //Traer todos los registros de cada tabla e insertarlos individualmente en Firebase
        var conteodatos = 0

        val vc = VerificarConex()
        val net = vc.revisarconexión(context)
        if (net) {
            clicbt("1")
            try{
                val queryAct =
                    "SELECT idtarea FROM '200_TAREAS' where (estado='Finalizado' or estado='No Exitosa' or estado='STD/BY') group by 1 order by 1 asc"
                val registrosinc = conGen.queryObjeto2val(
                    requireContext(), queryAct, null
                )  as Array<ArrayList<String>?>?
                if (registrosinc != null) {
                    for (c in registrosinc.indices) {
                        fg.act_param(requireContext(), "TAREA_ACT", tareaAct)
                        fg.act_estadotarea(requireContext())
                        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
                        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
                        val latact = latitud_ult
                        val lonact = longitud_ult
                        fg.ins_historial(requireContext(),
                            "Sincronizar Manualmente",
                            latact,
                            lonact)
                        val ce = CargarEstados()
                        ce.ActualizarServices(requireContext())
                        regexitoso = "1"
                        tareaAct = registrosinc[c]!![0].toString()
                        sincronizarTablasG("400_SINCRONIZAR", "200_TAREAS", "idtarea", tareaAct)
                        conteodatos++
                    }
                }
            } catch (e: Exception){
                clicbt("2")
                Log.i("Fallo-Sinc Informacion:", e.toString())
                findNavController().navigate(R.id.navigation_Sinc)
                dialog!!.dismiss()
            } finally {
                try{
                    sincronizarRegistro101(requireContext())
                }catch (ex: Exception){
                    clicbt("2")
                    Log.i("Fallo-Sinc Informacion:", ex.toString())
                    findNavController().navigate(R.id.navigation_Sinc)
                    dialog!!.dismiss()
                } finally {
                    try{
                        sincronizarHistorial102(requireContext())
                    }catch (exe: Exception){
                        clicbt("2")
                        Log.i("Fallo-Sinc Informacion:", exe.toString())
                        findNavController().navigate(R.id.navigation_Sinc)
                        dialog!!.dismiss()
                    } finally {
                        clicbt("2")
                        Toast.makeText(requireContext(), "Sincronización Finalizada", Toast.LENGTH_LONG).show();
                        findNavController().navigate(R.id.navigation_Sinc)
                        dialog!!.dismiss()
                    }
                }
            }
        } else {
            clicbt("2")
            Toast.makeText(requireContext(), "No se encuentra conectado a Internet", Toast.LENGTH_SHORT)
                .show()
            dialog!!.dismiss()
            findNavController().navigate(R.id.navigation_Sinc)
        }
    }

    fun clicbt(tipo:String){
        if(tipo=="1"){
            buttonSincronizar2.isClickable = false
            buttonSincronizar3.isClickable = false
        }else{
            buttonSincronizar2.isClickable = true
            buttonSincronizar3.isClickable = true
        }
    }

    fun sincronizarTablasG(tablasinc: String, ntabla: String, ncampo: String, valor_filtro: String) {
        var db = FirebaseFirestore.getInstance()
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        cambiarntabla(ntabla)
        vtsinc?.text = "0"
        val ntablasig = fg.tcsig(requireContext(), tablasinc, ntabla.toString(), 1)
        val ncamposig = fg.tcsig(requireContext(), tablasinc, ntabla.toString(), 2)

        val campos = conGen.queryObjeto(requireContext(), "pragma table_info('$ntabla')", null)
        val registros = conGen.queryObjeto(
            requireContext(),
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
                        ctx++
                        cambiarcontador()
                        if (ntabla != "200_TAREAS"){
                            fg.ejecDB(requireContext(),
                                "UPDATE '$ntabla' SET sinc='1' WHERE $ncampo='$valor_filtro';")
                        }

                    }
            } catch (e: Exception) {
                db.collection(ntabla).document(valor_filtro).set(tablag)
                    .addOnSuccessListener {
                        ctx++
                        cambiarcontador()
                        if (ntabla != "200_TAREAS"){
                            fg.ejecDB(requireContext(),
                                "UPDATE '$ntabla' SET sinc='1' WHERE $ncampo='$valor_filtro';")
                        }
                    }.addOnFailureListener {
                        regexitoso = "0"
                        fg.ejecDB(requireContext(),
                            "UPDATE '$ntabla' SET sinc='0' WHERE $ncampo='$valor_filtro';")
                        Toast.makeText(
                            requireContext(),
                            "La sincronización ha fallado en $ntabla-registro=$valor_filtro",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } finally {
                tablag.clear()
                if (!ntablasig.equals("0")) {
                    sincronizarTablasG(tablasinc, ntablasig, ncamposig, valor_filtro)
                }else if (tablasinc == "400_SINCRONIZAR" && regexitoso == "1"){
                    fg.ejecDB(requireContext(),
                        "UPDATE '200_TAREAS' SET sinc='1' WHERE idtarea='$valor_filtro';")
                }
            }
        } else {
            if (!ntablasig.equals("0")) {
                sincronizarTablasG(tablasinc, ntablasig, ncamposig, valor_filtro)
            }else if (tablasinc == "400_SINCRONIZAR" && regexitoso == "1"){
                fg.ejecDB(requireContext(),
                    "UPDATE '200_TAREAS' SET sinc='1' WHERE idtarea='$valor_filtro';")
            }
        }
    }

    fun sincronizarRegistro101(
        contexto: Context,
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
        contexto: Context,
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

    fun sincronizarTablasH(tablasinc: String, ntabla: String, ncampo: String, valor_filtro: String) {
        var db = FirebaseFirestore.getInstance()
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        var cont = requireContext()
        cambiarntabla(ntabla)
        vtsinc?.text = "0"
        val ntablasig = fg.tcsig(requireContext(), tablasinc, ntabla.toString(), 1)
        val ncamposig = fg.tcsig(requireContext(), tablasinc, ntabla.toString(), 2)

        val campos = conGen.queryObjeto(requireContext(), "pragma table_info('$ntabla')", null)
        val registros = conGen.queryObjeto(
            requireContext(),
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
                            ctx++
                            cambiarcontador()
                            fg.ejecDB(cont,
                                "UPDATE '$ntabla' SET sinc='1' WHERE $ncampo='$valor_filtro';")
                        }
                } catch (e: Exception) {
                    db.collection(ntabla).document(valor_filtro + "_" + i).set(regis)
                        .addOnSuccessListener {
                            ctx++
                            cambiarcontador()
                            fg.ejecDB(requireContext(),
                                "UPDATE '$ntabla' SET sinc='1' WHERE $ncampo='$valor_filtro';")
                        }.addOnFailureListener {
                            uidexitoso = "0"
                            fg.ejecDB(requireContext(),
                                "UPDATE '$ntabla' SET sinc='0' WHERE $ncampo='$valor_filtro';")
                            Toast.makeText(
                                requireContext(),
                                "La sincronización ha fallado en $ntabla-registro=$valor_filtro",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                regis.clear()
            }

            if (!ntablasig.equals("0")) {
                sincronizarTablasG(tablasinc, ntablasig, ncamposig, valor_filtro)
            }else if (uidexitoso == "1"){
                fg.ejecDB(requireContext(),
                    "UPDATE '102_HISTORIAL' SET sinc='1' WHERE $ncampo='$valor_filtro';")
            }
        } else {
            if (!ntablasig.equals("0")) {
                sincronizarTablasG(tablasinc, ntablasig, ncamposig, valor_filtro)
            }else if (uidexitoso == "1"){
                fg.ejecDB(requireContext(),
                    "UPDATE '102_HISTORIAL' SET sinc='1' WHERE $ncampo='$valor_filtro';")
            }
        }
    }

    fun cargarFotoUnica(contexto: Context, nomfoto: String, sqlquery: String) {
        val fg = FuncionesGenerales()
        val vc = VerificarConex()
        val net = vc.revisarconexión(contexto)
        if (net == true) {
            val nombreFoto = nomfoto
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
                        cargarFotoUnica(contexto, nfoto, sqlquery)
                    }.addOnProgressListener { taskSnapshot -> updateProgress(taskSnapshot)
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

    fun updateProgresst(valsinc: Int) {
        val fileSize = conteosin.toInt()
        val uploadBytes = valsinc
        val progress: Int
        progress = Math.round((100 * uploadBytes / fileSize).toFloat())
        pbregistros!!.progress = progress
    }

    fun cambiarntabla(ntabla: String?){
        tsinc?.text = ntabla
    }
    fun cambiarcontador(){
        vtsinc?.text = ctx.toString()
    }

    companion object {
        var instance: Sincronizar? = null
    }
}