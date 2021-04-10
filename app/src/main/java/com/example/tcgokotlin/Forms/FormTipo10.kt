package com.example.tcgokotlin.Forms

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tcgokotlin.R
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.sqliteDBHelper.ProcesosTablas
import com.example.tcgokotlin.utils.Common
import kotlinx.android.synthetic.main.activity_form_tipo10.*
import kotlinx.android.synthetic.main.activity_form_tipo10.BTRegresar
import kotlinx.android.synthetic.main.activity_form_tipo10.TVNPantalla
import kotlinx.android.synthetic.main.activity_form_tipo11.*
import java.util.*


class FormTipo10 : Fragment() {
    private var titulopant: String = ""
    private var useruid: String = ""
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
    private var panact: String = ""
    private var idpreg: String = ""
    private var preg: String = ""
    private var fotoa: String = ""
    private var evanexo: String = ""
    private var lista: ArrayList<FormAnexosModel>? = null
    private var fg: FuncionesGenerales? = null
    private var at: ProcesosTablas? = null
    private var recyclerpreguntas: RecyclerView? = null
    private var tpant: String = ""
    private lateinit var OpcionesID: Array<String?>
    private lateinit var ValoresTXT: Array<String?>
    private lateinit var ValoresNUM: Array<String?>
    private lateinit var OpcionesRtaID: Array<String?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.activity_form_tipo10, container, false)
        instance = this
    }

    inner class Activ(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var CBV: ImageView
        var nombreOpcion: TextView
        var nombreNumerica: TextView
        init {
            CBV = itemView.findViewById<View>(R.id.iv_req_foto) as ImageView
            nombreOpcion = itemView.findViewById<View>(R.id.tv_req_text) as TextView
            nombreNumerica = itemView.findViewById<View>(R.id.tv_req_cant) as TextView
        }
    }

    inner class FormAnexosModel(var req_txt: String?, var req_num: String?, var req_foto: String?)
    internal inner class FormAnexosAdapter(private var lista: ArrayList<FormAnexosModel>) :
        RecyclerView.Adapter<Activ>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Activ {
            return Activ(
                LayoutInflater.from(parent.context).inflate(R.layout.ver_item_anexo, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Activ, position: Int) {
            val FormAnexos = lista[position]
            if(!FormAnexos.req_txt.toString().equals("0")){
                holder.nombreOpcion.text = FormAnexos.req_txt
            }
            if(!FormAnexos.req_num.toString().equals("0")){
                holder.nombreNumerica.text = FormAnexos.req_num
            }
            if (FormAnexos.req_foto != "0") {
                holder.CBV.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
            } else {
                holder.CBV.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            holder.CBV.setOnClickListener { editarFormAnexos(position, holder, FormAnexos) }

            holder.nombreNumerica.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> // If the event is a key-down event on the "enter" button
                if (event.action == KeyEvent.ACTION_DOWN && (keyCode > 6 && keyCode < 17 || event.keyCode == KeyEvent.KEYCODE_DEL)) {
                    if (event.keyCode == KeyEvent.KEYCODE_DEL) {
                        val NTXT = holder.nombreOpcion.text.toString()
                        val NNUM = holder.nombreNumerica.text.toString()
                        if (NNUM.length > 1) {
                            at?.actualizar301_temp10(requireContext(),idtarea, idpreg,OpcionesID[position].toString(),NTXT,NNUM)
                            holder.nombreNumerica.setText(NNUM.substring(1, NNUM.length - 1))
                            FormAnexos.req_num = holder.nombreNumerica.text.toString()
                        } else {

                            holder.nombreNumerica.setText("")
                            FormAnexos.req_num = ""
                        }
                        return@OnKeyListener true
                    } else {
                        val NTXT = holder.nombreOpcion.text.toString()
                        val NNUM = holder.nombreNumerica.text.toString() + (keyCode - 7)
                        at?.actualizar301_temp10(requireContext(),idtarea, idpreg,OpcionesID[position].toString(),NTXT,NNUM)
                        holder.nombreNumerica.setText(NNUM)
                        FormAnexos.req_num = NNUM
                        return@OnKeyListener true
                    }
                }
                false
            })

            holder.nombreOpcion.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> // If the event is a key-down event on the "enter" button
                if (event.action == KeyEvent.ACTION_DOWN) {
                        val NTXT = holder.nombreOpcion.text.toString()
                        val NNUM = holder.nombreNumerica.text.toString() + (keyCode - 7)
                        at?.actualizar301_temp10(requireContext(),idtarea!!, idpreg!!,OpcionesID[position].toString(),NTXT,NNUM)
                        holder.nombreOpcion.setText(NTXT)
                        FormAnexos.req_txt = NTXT
                        return@OnKeyListener true
                }
                false
            })

        }

        override fun getItemCount(): Int {
            return lista.size
        }
    }


    override fun onStart() {
        super.onStart()
        val conGen = ConsultaGeneral()
        at = ProcesosTablas()
        fg = FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        recyclerpreguntas = recyclerTipo10
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerpreguntas!!.layoutManager = llm
        lista = ArrayList()
        fg?.act_param(requireContext(),"OPN_ACT","0")
        //fg.ultimaPantalla("FormTipo2");
        useruid = fg?.parametro(requireContext(),"USERUID").toString()
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
        titulopant = fg?.parametro(requireContext(),"TITULO_ACT").toString()
        evanexo = fg?.getQ1(requireContext(),"select ifnull(count(*),0) as cra from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idform='$idforma'").toString()

        val querypreguntasid =
            "SELECT idpreg,preg,cnd2,fotoap ,eval FROM  '204_PREGGEN' where idtarea='" + idtarea + "' and idpreg='" + idpregact + "'"
        val actidpreg = conGen.queryObjeto2val(
            context, querypreguntasid, null
        )

        idpreg = actidpreg[0][0].toString()
        preg = actidpreg[0]!![1].toString()
        val cond2 = actidpreg[0]!![2].toString()
        fotoa = actidpreg[0]!![3].toString()
        val eval = actidpreg[0]!![4].toString()
        TVNPantalla.setText(titulopant)
        try{
            if (evanexo == "0"){
                agregaritem()
            }
        }finally {
            listarpreguntas()
        }

        imgAdicionar.setOnClickListener {
            agregaritem()
        }
        imgRestar.setOnClickListener {
            removeritem()
        }
        BTRegresar.setOnClickListener {
            desplazamientos(3)
        }
    }

    private fun agregaritem(){
        val conGen = ConsultaGeneral()
        var rtax = 1
        var trta = 0

        var cant_reg = fg?.getQ1(requireContext(),
            "SELECT ifnull(count(*),0) as cres FROM '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpreg'"
        ).toString()

        val querypreguntasrtaF =
            "SELECT rta1,rta2,opn as cres FROM '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpreg'"
        var rtapreguntasF = conGen?.queryObjeto2val(
            context, querypreguntasrtaF, null
        )

        if (rtapreguntasF != null) {
            for (c in rtapreguntasF.indices) {
                var V1 = rtapreguntasF[c][0].toString()
                var V2 = rtapreguntasF[c][1].toString()
                var V3 = fg?.getQ1(requireContext(),"select ifnull(count(*),0) as crif from '302_FOTOS_RESP_TEMP' where idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + rtapreguntasF[c]!![2].toString()).toString()
                if(((V1.equals("") || V1.equals("0")) || (V2.equals("") || V2.equals("0")) || (V3.equals("") || V3.equals("0"))) && !cant_reg.equals("0")){
                    rtax = 0
                }
            }
        }

        if (rtax.toInt() > 0) {
            var maxopn = fg?.getQ1(requireContext(),"Select ifnull(max(opn),0) as mop from '301_RESPUESTAS_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='" + idpregact + "'").toString().toInt()
            maxopn = maxopn + 1
            fg?.act_param(requireContext(),"OPN_ACT" ,maxopn.toString() )
            val latitud_ult = fg?.parametro(requireContext(),"ULT_LAT")
            val longitud_ult = fg?.parametro(requireContext(),"ULT_LON")
            var latact = latitud_ult
            var lonact = longitud_ult
            fg?.ejecDB(requireContext(),
                "insert or ignore into '301_RESPUESTAS_TEMP'(idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,opt,rta1,rta2,obs,gpslat,gpslon,useruid) VALUES " +
                        "('" + idtarea + "' , '" + idformulario + "' , '" + idforma + "' , '" + idgrupo + "' , '" + idsgrupo + "' , '" + idpregact + "' , '" + maxopn.toInt() +
                        "' , '0' , '0' , '0' , '0' , '" + latact + "' , '" + lonact + "' , '" + useruid + "');"
            )
            listarpreguntas()
        } else {
            Toast.makeText(context,"Complete el item actual antes de agregar uno nuevo",Toast.LENGTH_LONG).show()
        }
    }

    private fun removeritem(){
        val conGen = ConsultaGeneral()
        var maxopn = fg?.getQ1(requireContext(),"Select ifnull(max(opn),0) as mop from '301_RESPUESTAS_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='" + idpregact + "'").toString().toInt()
        if (maxopn!=0){
        var opnnew = maxopn - 1
        fg?.act_param(requireContext(),"OPN_ACT" ,opnnew.toString() )
        var cfotos = fg?.getQ1(requireContext(),"SELECT ifnull(count(nfoto),0) FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + maxopn.toString()).toString()
        if (!cfotos.equals("0")){
            val queryNombres = "SELECT nfoto FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + maxopn.toString()
            val nFotos = conGen.queryObjeto2val(context, queryNombres, null)
            if (nFotos != null) {
                for (i in nFotos.indices) {
                    fg?.BorrarFoto(nFotos[i][0].toString())
                }
            }
            fg?.ejecDB(requireContext(),
                "DELETE FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + maxopn.toString()
            )
        }
        fg?.ejecDB(requireContext(),
            "DELETE FROM '301_RESPUESTAS_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + maxopn.toString()
        )
        listarpreguntas()
        }
    }

    fun Act_Eval(trta: Int) {
        when (panact) {
            "97" -> {
                fg?.ejecDB(requireContext(),"UPDATE '201_PREGFORM' SET ev='" + (trta * 100).toString() + "' WHERE idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "'")
                tpant = "97"
            }
            "98" -> {
                fg?.ejecDB(requireContext(),"UPDATE '202_PREGGRUP' SET ev='" + trta.toString() + "' WHERE idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "' and grpid='" + idgrupo + "'")
                tpant = "98"
            }
            "99" -> {
                fg?.ejecDB(requireContext(),"UPDATE '203_PREGSGRUP' SET ev='" + trta.toString() + "' WHERE idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "' and grpid='" + idgrupo + "' and sgrpid='$idsgrupo'")
                tpant = "99"
            }
        }
    }

    fun listarpreguntas() {
        val conGen = ConsultaGeneral()
        val queryAct =
            "SELECT rta1,rta2,opn FROM '301_RESPUESTAS_TEMP' where idtarea='" + idtarea + "' and idpreg = '$idpreg' order by 3 ASC"
        val preguntas = conGen.queryObjeto2val(
            context, queryAct, null
        )
        lista!!.clear()
        if (preguntas != null) {
            ValoresTXT = arrayOfNulls(preguntas.size)
            ValoresNUM = arrayOfNulls(preguntas.size)
            OpcionesRtaID = arrayOfNulls(preguntas.size)
            OpcionesID = arrayOfNulls(preguntas.size)
            for (c in preguntas.indices) {
                ValoresTXT[c] = preguntas[c]!![0] as String?
                ValoresNUM[c] = preguntas[c]!![1] as String?
                OpcionesID[c] = preguntas[c]!![2] as String?
                var cfotositem = fg?.getQ1(requireContext(),"select count(*) as cri from '302_FOTOS_RESP_TEMP' where idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + OpcionesID[c].toString()).toString()
                OpcionesRtaID[c] = cfotositem as String?
                val am = FormAnexosModel(ValoresTXT[c], ValoresNUM[c], OpcionesRtaID[c])
                lista?.add(am)
            }
        }
        val aa = FormAnexosAdapter(lista!!)
        recyclerpreguntas!!.adapter = aa
    }

    fun editarFormAnexos(pos: Int, holder: Activ, preguntas: FormAnexosModel?) {
        val holder2 = holder
        val product = preguntas
        var resptxt = holder2.nombreOpcion.text.toString()
        var respnum = holder2.nombreNumerica.text.toString()
        fg?.act_param(requireContext(),"OPN_ACT",OpcionesID[pos].toString())
        at?.actualizar301_temp10(requireContext(),idtarea, idpreg,OpcionesID[pos].toString(),resptxt,respnum)
        fg?.act_param(requireContext(),"TIPO_FOTO","2")
        var foto_tomada = fg?.getQ1(requireContext(),"select ifnull(count(*),0) as crf from '302_FOTOS_RESP_TEMP' where idtarea='" + idtarea + "' and idpreg='$idpreg' and opn=" + OpcionesID[pos].toString()).toString()
        fg?.act_param(requireContext(),"FTOM",foto_tomada)
        var trta = 0
        if (!resptxt.toString().equals("") && !respnum.toString().equals("")){
            trta = 1
        } else {
            trta = 0
        }
        Act_Eval(trta)
        openFragment("100")
    }

    fun desplazamientos(Boton: Int) {
        val conGen = ConsultaGeneral()
        var rtax = 1
        var trta = 0
        var Tipox = ""

        var cant_reg = fg?.getQ1(requireContext(),
            "SELECT ifnull(count(*),0) as cres FROM '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpreg'"
        ).toString()

        val querypreguntasrtaF =
            "SELECT rta1,rta2,opn as cres FROM '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpreg'"
        var rtapreguntasF = conGen?.queryObjeto2val(
            context, querypreguntasrtaF, null
        )

        if (rtapreguntasF != null) {
            for (c in rtapreguntasF.indices) {
                var V1 = rtapreguntasF[c][0].toString()
                var V2 = rtapreguntasF[c][1].toString()
                var V3 = fg?.getQ1(requireContext(),"select ifnull(count(*),0) as crif from '302_FOTOS_RESP_TEMP' where idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + rtapreguntasF[c]!![2].toString()).toString()
                if(((V1.equals("") || V1.equals("0")) || (V2.equals("") || V2.equals("0")) || (V3.equals("") || V3.equals("0"))) && !cant_reg.equals("0")){
                    rtax = 0
                }
            }
        }
        if (rtax.toInt() > 0) {
            fg?.ejecDB(requireContext(),"UPDATE '204_PREGGEN' SET eval='1' WHERE idtarea='" + idtarea + "' and idpreg='$idpreg'")
            trta = 1
            fg?.act_param(requireContext(),"FTOM","0")
            fg?.act_param(requireContext(),"IDFOTO_ACT","0")
            if (!cant_reg.equals("0")){
                Act_Eval(trta)
            } else {
                Act_Eval(0)
            }
            openFragment(tpant)
        } else {
            trta = 0
            Act_Eval(trta)
            fg?.ejecDB(requireContext(),"UPDATE '204_PREGGEN' SET eval='0' WHERE idtarea='" + idtarea + "' and idpreg='$idpreg'")
            Toast.makeText(context,"Si inicializo algun item debe finalizarlo o eliminarlo para salir, realice el proceso e intente de nuevo",Toast.LENGTH_LONG).show()
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
        var instance: FormTipo10? = null
    }
}
