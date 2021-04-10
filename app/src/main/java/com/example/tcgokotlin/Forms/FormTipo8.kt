package com.example.tcgokotlin.Forms

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tcgokotlin.R
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.utils.Common
import kotlinx.android.synthetic.main.activity_form_tipo8.*
import kotlinx.android.synthetic.main.activity_form_tipo8.BTAtras
import kotlinx.android.synthetic.main.activity_form_tipo8.BTRegresar
import kotlinx.android.synthetic.main.activity_form_tipo8.BTSiguiente
import kotlinx.android.synthetic.main.activity_form_tipo8.TVNPantalla
import java.io.File
import java.util.*


class FormTipo8 : Fragment() {
    private var titulopant: String = ""
    private var idformulario: String = ""
    private var nformulario: String = ""
    private var idtarea: String = ""
    private var ntarea: String = ""
    private var idforma: String = ""
    private var nforma: String = ""
    private var idgrupo: String = ""
    private var ngrupo: String = ""
    private var idsgrupo: String = ""
    private var nsgrupo: String = ""
    private var tipoform: String = ""
    private var idpregact: String = ""
    private var fototom: String = ""
    private var opnact: String = ""
    private var useruid: String = ""
    private var orden: String = ""
    private var tpant: String = ""
    private var panact: String = ""
    private var path: String = ""
    private var iv: ImageView? = null
    private var nombreFoto: String = ""
    private var fg: FuncionesGenerales? = null
    private var recycler: RecyclerView? = null
    private var lista: ArrayList<RutaModel>? = null
    private lateinit var fotoid: Array<String?>
    private lateinit var fotonom: Array<String?>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.activity_form_tipo8, container, false)
    }

    internal inner class Ruta(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvruta: TextView = itemView.findViewById<View>(R.id.tvRutaFoto) as TextView
        var iveliminar: ImageView = itemView.findViewById<View>(R.id.ivEliminarFoto) as ImageView
    }

    inner class RutaModel(var ruta: String)
    internal inner class RutaAdpater(private var lista: ArrayList<RutaModel>) :
        RecyclerView.Adapter<Ruta>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Ruta {
            return Ruta(
                LayoutInflater.from(parent.context).inflate(R.layout.ver_ruta_foto, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Ruta, position: Int) {
            val rm = lista[position]
            holder.tvruta.text = rm.ruta
            holder.tvruta.setOnClickListener { MostrarFoto(position) }
            holder.iveliminar.setOnClickListener { EliminarFoto(position) }
        }
        override fun getItemCount(): Int {
            return lista.size
        }
    }

    override fun onStart() {
        super.onStart()
        fg =FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        iv = imageViewFoto
        idtarea = fg?.parametro(requireContext(),"TAREA_ACT").toString()
        ntarea = fg?.parametro(requireContext(),"NTAREA_ACT").toString()
        idformulario = fg?.parametro(requireContext(),"FORMULARIO_ACT").toString()
        nformulario = fg?.parametro(requireContext(),"NFORMULARIO_ACT").toString()
        idforma = fg?.parametro(requireContext(),"FORMA_ACT").toString()
        nforma = fg?.parametro(requireContext(),"NFORMA_ACT").toString()
        idgrupo = fg?.parametro(requireContext(),"GRUPO_ACT").toString()
        ngrupo = fg?.parametro(requireContext(),"NGRUPO_ACT").toString()
        idsgrupo = fg?.parametro(requireContext(),"SUBGRUPO_ACT").toString()
        nsgrupo = fg?.parametro(requireContext(),"NSUBGRUPO_ACT").toString()
        tipoform = fg?.parametro(requireContext(),"TIPO_GRP").toString()
        panact = fg?.parametro(requireContext(),"ULTIMAP").toString()
        idpregact = fg?.parametro(requireContext(),"PREGUNTA_ACT").toString()
        opnact = fg?.parametro(requireContext(),"OPN_ACT").toString()
        useruid = fg?.parametro(requireContext(),"USERUID").toString()
        titulopant = fg?.parametro(requireContext(),"TITULO_ACT").toString()
        val foto_tomada = fg?.getQ1(requireContext(),"select case ifnull(count(*),0) WHEN 0 THEN 0 ELSE 1 END AS crf from '302_FOTOS_RESP_TEMP' where idtarea='" + idtarea + "' and idpreg='" + idpregact + "'").toString()
        fg?.act_param(requireContext(),"FTOM",foto_tomada.toString())
        fototom = fg?.parametro(requireContext(),"FTOM").toString()
        val fotoact = fg?.parametro(requireContext(),"IDFOTO_ACT").toString()
        if(!fotoact.equals("0")){
            val uriImage =
                Uri.parse(Environment.getExternalStorageDirectory().toString() + "/TCGO_FILES/IMG/" + fotoact.toString())
            iv?.setImageURI(uriImage)
        }

        orden = fg?.getQ1(requireContext(),"select orden from '002_PREGUNTASB'").toString()
        recycler = recyclerViewFoto
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recycler?.layoutManager = llm
        lista = ArrayList()

        TVNPantalla.setText(titulopant)
        //Llenar el recycler con lo que contenga path
        if (fototom.equals("1")){
            listarfotos()
        }else{
            tomarFoto()
        }

        BTAtras.setOnClickListener {
            desplazamientos(1)
        }
        BTSiguiente.setOnClickListener {
            desplazamientos(2)
        }
        BTRegresar.setOnClickListener {
            desplazamientos(3)
        }
        ivTomarFoto.setOnClickListener {
            tomarFoto()
        }
    }

    fun Act_Eval(trta: Int){
        if(panact.equals("97")) {
            fg?.ejecDB(requireContext(),"UPDATE '201_PREGFORM' SET ev='" + (trta * 100).toString() + "' WHERE idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "'")
            tpant = "97"
        }else if(panact.equals("98")) {
            fg?.ejecDB(requireContext(),"UPDATE '202_PREGGRUP' SET ev='" + trta.toString() + "' WHERE idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "' and grpid='" + idgrupo + "'")
            tpant = "98"
        }else if(panact.equals("99")){
            fg?.ejecDB(requireContext(),"UPDATE '203_PREGSGRUP' SET ev='" + trta.toString() + "' WHERE idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "' and grpid='" + idgrupo + "' and sgrpid='$idsgrupo'")
            tpant = "99"
        }
    }

    @Throws(Exception::class)

    fun tomarFoto() {
        val two: Thread = object : Thread() {
            override fun run() {
                val carpetaIMG = File(
                    Environment.getExternalStorageDirectory().toString() + "/TCGO_FILES/",
                    "IMG"
                )

                val builder = VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())
                val camara = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (!carpetaIMG.exists()) {
                    carpetaIMG.mkdirs()
                }

                nombreFoto =
                    fg?.parametro(requireContext(),"TAREA_ACT")
                        .toString() + "_" + fg?.fechaActual(2) + "_" + fg?.parametro(requireContext(),"PREGUNTA_ACT")
                        .toString() + "_" + fg?.parametro(requireContext(),"OPN_ACT").toString() + ".jpg"

                path = Environment.getExternalStorageDirectory()
                    .toString() + "/TCGO_FILES/IMG/" + nombreFoto
                val imagen = File(carpetaIMG, nombreFoto)
                val uri = Uri.fromFile(imagen)
                camara.putExtra(MediaStore.EXTRA_OUTPUT.toString(), uri)
                startActivityForResult(camara, TAKE_PICTURE)
            }
        }
        two.start()
        two.join()
    }

    private fun EliminarFoto(pos: Int) {
        
        fg?.ejecDB(requireContext(),
            "DELETE FROM '302_FOTOS_RESP_TEMP' WHERE idfoto='" + fotoid[pos]?.toString() + "'"
        )
        fg?.BorrarFoto(fotoid[pos].toString())
        val querypreguntasrtaF =
            "SELECT count(*) as cres FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='$idpregact'"
        var rtapreguntasF = fg?.getQ1(requireContext(),querypreguntasrtaF)
        val rtax = rtapreguntasF.toString()
        val trta = if (rtax.toInt() > 0) {
            1
        } else {
            0
        }
        fg?.act_param(requireContext(),"FTOM", trta.toString())
        fg?.ejecDB(requireContext(),"UPDATE '204_PREGGEN' SET eval='" + trta + "' , evalfoto='" + trta + "' WHERE idtarea='" + idtarea + "' and idpreg='$idpregact'")
        Act_Eval(trta)
        listarfotos()
    }

    private fun MostrarFoto(pos: Int) {
        val uriImage =
            Uri.parse(Environment.getExternalStorageDirectory().toString() + "/TCGO_FILES/IMG/" + fotonom[pos].toString())
        iv?.setImageURI(uriImage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PICTURE) {
                lista?.add(RutaModel(nombreFoto))
                fg?.ejecDB(requireContext(),
                    "UPDATE '204_PREGGEN' SET eval='1' , evalfoto='1' WHERE idtarea='$idtarea' and idpreg='$idpregact'"
                )
                Act_Eval(1)
                val latitud_ult = fg?.parametro(requireContext(),"ULT_LAT")
                val longitud_ult = fg?.parametro(requireContext(),"ULT_LON")
                var latact = latitud_ult
                var lonact = longitud_ult
                val queryFoto =
                    "INSERT INTO '302_FOTOS_RESP_TEMP' (idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid) VALUES('" +
                            idtarea + "' , '" + idformulario + "' , '" + idforma + "' , '" + idgrupo + "' , '" + idsgrupo + "' , '" + idpregact + "' , '" +
                            opnact + "' , '" + nombreFoto.substringBefore(".") + "' , '" + nombreFoto + "' , '" + latact + "' , '" + lonact+ "' , '" + useruid + "');"
                fg?.ejecDB(requireContext(),queryFoto)

                fg?.act_param(requireContext(),"IDFOTO_ACT", nombreFoto.toString())
                fg?.act_param(requireContext(),"FTOM", "1")
                openFragment("8")
            }
        }
    }

    fun listarfotos() {
        val conGen = ConsultaGeneral()
        lista!!.clear()
        val queryNombres =
            "SELECT nfoto,idfoto FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='$idtarea' and idpreg='$idpregact' order by idfoto"
        val nFotos = conGen.queryObjeto2val(context, queryNombres, null)
        if (nFotos != null) {
            fotonom = arrayOfNulls(nFotos.size)
            fotoid = arrayOfNulls(nFotos.size)
            for (i in nFotos.indices) {
                fotonom[i] = nFotos[i][0].toString()
                fotoid[i] = nFotos[i][1].toString()
                lista?.add(RutaModel(fotoid[i].toString()))
            }
            val ra = RutaAdpater(lista!!)
            recycler?.adapter = ra
        }else{
            val ra = RutaAdpater(lista!!)
            recycler?.adapter = ra
        }
    }

    fun desplazamientos(Boton: Int) {
        val conGen = ConsultaGeneral()
        val querypreguntasrtaF =
            "SELECT count(*) as cres FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='$idpregact'"
        var rtapreguntasF = fg?.getQ1(requireContext(),querypreguntasrtaF)
        val rtax = rtapreguntasF.toString()
        val trta = if (rtax.toInt() > 0) {1} else {0}
        fg?.ejecDB(requireContext(),"UPDATE '204_PREGGEN' SET eval='" + trta + "' , evalfoto='" + trta + "' WHERE idtarea='" + idtarea + "' and idpreg='$idpregact'")
        if (Boton != 3) {
            var querysigp = if (Boton == 2) {
                when (panact) {
                    "97" -> {
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden>$orden"
                    }
                    "98" -> {
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden>$orden"
                    }
                    else -> {
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden>$orden"
                    }
                }
            } else {
                when (panact) {
                    "97" -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden<$orden"
                    }
                    "98" -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden<$orden"
                    }
                    else -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden<$orden"
                    }
                }
            }
            val ordensig = conGen.queryObjeto2val(
                context, querysigp as String?, null
            )
            if (ordensig[0][0].toInt() > 0) {
                val querymino = if (panact.equals("97")) {
                    "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '204_PREGGEN' where idtarea='" + idtarea + "' and idform='" + ordensig[0]!![1].toString() + "'"
                }else if (panact.equals("98")) {
                    "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden=" + ordensig[0]!![1].toString()
                } else {
                    "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden=" + ordensig[0]!![1].toString()
                }
                val mino = conGen.queryObjeto2val(
                    context, querymino, null
                )
                fg?.act_param(requireContext(),"FORMA_ACT",mino[0][1].toString())
                fg?.act_param(requireContext(),"NFORMA_ACT",fg?.getQ1(requireContext(),"Select form from '201_PREGFORM' where idtarea='" + idtarea + "' and idform='" + mino!![0]!![1].toString() + "'").toString())
                fg?.act_param(requireContext(),"GRUPO_ACT",mino[0][2].toString())
                fg?.act_param(requireContext(),"NGRUPO_ACT",fg?.getQ1(requireContext(),"Select grp from '202_PREGGRUP' where idtarea='" + idtarea + "' and idform='" + mino!![0]!![1].toString() + "' and grpid='" + mino[0]!![2].toString() + "'").toString())
                fg?.act_param(requireContext(),"SUBGRUPO_ACT",mino[0][3].toString())
                fg?.act_param(requireContext(),"NSUBGRUPO_ACT",fg?.getQ1(requireContext(),"Select sgrp from '203_PREGSGRUP' where idtarea='" + idtarea + "' and idform='" + mino!![0]!![1].toString() + "' and grpid='" + mino[0]!![2].toString()  + "' and sgrpid='" + mino[0]!![3].toString() + "'").toString())
                fg?.act_param(requireContext(),"ORDEN_ACT", mino[0][4].toString())
                fg?.act_param(requireContext(),"PREGUNTA_ACT", mino[0][5].toString())
                fg?.act_param(requireContext(),"NPREGUNTA_ACT", mino[0][6].toString())
                fg?.act_param(requireContext(),"TITULO_ACT", mino[0][6].toString())
                fg?.act_param(requireContext(),"IDFOTO_ACT", "0")
                Act_Eval(trta)
                val tipox = mino[0][0].toString()
                openFragment(tipox)
            } else {
                try {
                    Act_Eval(trta)
                }finally {
                    openFragment(tpant)
                }
            }
        } else {
            try {
                Act_Eval(trta)
            }finally {
                openFragment(tpant)
            }
        }
    }

    private fun openFragment(strCase: String) {
        when (strCase) {
            "97" -> {
                findNavController().navigate(R.id.formaFragment)
            }
            "98" -> {
                findNavController().navigate(R.id.MenuFragment)
            }
            "99" -> {
                findNavController().navigate(R.id.SubMenuFragment)
            }
            "100" -> {
                findNavController().navigate(R.id.FotosFragment)
            }
            "101" -> {
                findNavController().navigate(R.id.navigation_tasks)
            }
            "2" -> {
                findNavController().navigate(R.id.Tipo2Fragment)
            }
            "3" -> {
                findNavController().navigate(R.id.Tipo3Fragment)
            }
            "8" -> {
                findNavController().navigate(R.id.Tipo8Fragment)
            }
            "9" -> {
                findNavController().navigate(R.id.Tipo9Fragment)
            }
            "10" -> {
                findNavController().navigate(R.id.Tipo10Fragment)
            }
            "11" -> {
                findNavController().navigate(R.id.Tipo11Fragment)
            }
        }
    }

    companion object {
        private const val TAKE_PICTURE = 1
        var instance: FormTipo8? = null
    }
}