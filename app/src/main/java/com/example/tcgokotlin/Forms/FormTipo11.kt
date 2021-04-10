package com.example.tcgokotlin.Forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavGraph
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tcgokotlin.R
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.sqliteDBHelper.ProcesosTablas
import kotlinx.android.synthetic.main.activity_form_tipo11.*
import java.util.*


class FormTipo11 : Fragment() {
    private var useruid: String = ""
    private var panact: String = ""
    private var idtarea: String = ""
    private var ntarea: String = ""
    private var ncliente_act: String = ""
    private var horario: String = ""
    private var idformulario: String = ""
    private var nformulario: String = ""
    private var idforma: String = ""
    private var nforma: String = ""
    private var tituloform: String = ""
    private var tituloact: String = ""
    private var idgrupo: String = ""
    private var nogrupo: String = ""
    private var idsgrupo: String = ""
    private var nsgrupo: String = ""
    private var idpregunta: String = ""
    private var npregunta: String = ""
    private var tipopreg: String = ""
    private var tipocarga: String = ""
    private var ordenac: String = ""
    private var estado: String = ""
    private var opn: String = ""
    private var obs_gen: String = ""
    private var autoriza: String = ""
    private var tipofirma: String = ""
    private var firmacli: String = ""
    private var firmauser: String = ""
    private var nom_cli: String = ""
    private var ident_cli: String = ""
    private var ap_check: String = ""
    private var ap_txt: String = ""
    private var ap_foto: String = ""
    private var ap_select: String = ""
    private var rta1: String = ""
    private var rta2: String = ""
    private var obs: String = ""
    private var fototom: String = ""
    private var tipotxt: String = ""
    private var tipofoto: String = ""
    private var cantfotos: String = ""
    private var maximoFotoscli: String = ""
    private var tpant: String = ""
    private var lista: ArrayList<ActivacionModel>? = null
    private var fg: FuncionesGenerales? = null
    private var at: ProcesosTablas? = null

    private var recyclerpreguntas: RecyclerView? = null
    private lateinit var OpcionesID: Array<String?>
    private lateinit var OpcionesTID: Array<String?>
    private lateinit var OpcionesRtaID: Array<String?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.activity_form_tipo11, container, false)
    }

    inner class Activ(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var CBV: ImageView
        var nombreOpcion: TextView

        init {
            CBV = itemView.findViewById<View>(R.id.imgRBV) as ImageView
            nombreOpcion = itemView.findViewById<View>(R.id.tvNombreOpcionRD) as TextView
        }
    }

    inner class ActivacionModel(var nombreOpcion: String?, var rtaCK: String?)
    internal inner class ActivacionAdapter(var lista: ArrayList<ActivacionModel>) :
        RecyclerView.Adapter<Activ>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Activ {
            return Activ(
                LayoutInflater.from(parent.context).inflate(R.layout.ver_choice, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Activ, position: Int) {
            val Activacion = lista[position]
            holder.nombreOpcion.text = Activacion.nombreOpcion
            if (Activacion.rtaCK != "0") {
                holder.CBV.setImageResource(R.drawable.rdb)
            } else {
                holder.CBV.setImageResource(R.drawable.rdnoselb)
            }
            holder.CBV.setOnClickListener { actselector(position, holder, Activacion) }
            holder.nombreOpcion.setOnClickListener {
                actselector(
                    position,
                    holder,
                    Activacion
                )
            }
        }

        override fun getItemCount(): Int {
            return lista.size
        }
    }


    override fun onStart() {
        super.onStart()
        // SE INICIALIZAN LAS FUNCIONES DE LOS HELPERS DE SQL
        at = ProcesosTablas()
        fg = FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        //SE CREA EL LINEAR LAYOUT QUE ALMACENARA LOS ITEMS Y SE ASIGNA A EL RECYCLER
        recyclerpreguntas = RecyclerSelector
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerpreguntas!!.layoutManager = llm

        //INICIALIZACION DE LISTA PARA LLENAR LOS GRUPOS
        lista = ArrayList()

        //SE CARGARN LOS PARAMETROS DE ESTE FORMULARIO SEGUN LA PANTALLA DE LA QUE VENGAN
        panact = fg?.parametro(requireContext(),"ULTIMAP").toString()
        tituloform = fg?.parametro(requireContext(),"TIPO_GRP").toString()
        maximoFotoscli = fg?.parametro(requireContext(),"FOTOSCLI").toString()
        
            try {
                cargarparametros()
            } finally {
                leerParametros()
            }

        TV_Check_SI.setOnClickListener {
            actualizacheck("1")
        }
        TV_Check_NO.setOnClickListener {
            actualizacheck("2")
        }
        TV_Check_NA.setOnClickListener {
            actualizacheck("3")
        }
        ivTomarFoto.setOnClickListener {
            cantfotos = fg?.getQ1(requireContext(),"Select count(*) from '302_FOTOS_RESP_TEMP' where idtarea='$idtarea' and idpreg<>'FIRMA_CLI' and idpreg<>'FIRMA_USER'").toString()
            if (cantfotos.toInt()>=maximoFotoscli.toInt()){
                Toast.makeText(requireContext(),"Alcanzo el maximo de fotos permitidas, para tomar mas elimine una anterior o solicite que su compañia contacte con el administrador para solicitar capacidad adicional",Toast.LENGTH_LONG).show()
            }else{
                val resp_txt = tv_Observacion.text.toString()
                if(resp_txt != "0" && resp_txt != ""){
                    fg?.ejecDB(requireContext(),
                        "UPDATE '301_RESPUESTAS_TEMP' set obs='$resp_txt' where idtarea='$idtarea' and idpreg='$idpregunta'"
                    )
                }
                fg?.act_param(requireContext(),"TIPO_FOTO",fg?.getQ1(requireContext(),"select fototipo from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'").toString())
                openFragment("100")
            }

        }
        //SE CARGAN LOS LISTENERS DE LOS BOTONES Y VARIABLES
        RB_Obligatorio.setOnClickListener {
            Toast.makeText(context,"TEXTO REQUERIDO",Toast.LENGTH_SHORT).show()
        }
        BTRegresar.setOnClickListener {
            desplazamientos(3)
        }
        BTRegresar2.setOnClickListener {
            desplazamientos(3)
        }
    }

    private fun actualizacheck(resp: String) {
        val resp_txt = tv_Observacion.text.toString()
        if(!resp_txt.equals("0") && !resp_txt.equals("")){
            fg?.ejecDB(requireContext(),
                "UPDATE '301_RESPUESTAS_TEMP' set obs='$resp_txt' where idtarea='$idtarea' and idpreg='$idpregunta'"
            )
        }
        fg?.ejecDB(requireContext(),
            "UPDATE '301_RESPUESTAS_TEMP' SET rta1='$resp' where idtarea='$idtarea' and idpreg='$idpregunta'"
        )
         fg?.act_param(requireContext(),"RTA1", resp)
        rta1 = resp
        TV_Check_SI.setText(fg?.getQ1(requireContext(),
            "select campo1 from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString())
        TV_Check_NO.setText(fg?.getQ1(requireContext(),
            "select campo2 from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString())
        TV_Check_NA.setText(fg?.getQ1(requireContext(),
            "select campo3 from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString())
        if (rta1 == "1") {
            TV_Check_SI.setBackgroundResource(R.drawable.btmenu_primarc)
            TV_Check_NO.setBackgroundResource(R.drawable.btmenu_secundlight)
            TV_Check_NA.setBackgroundResource(R.drawable.btmenu_secundlight)
        } else if (rta1 == "2") {
            TV_Check_SI.setBackgroundResource(R.drawable.btmenu_secundlight)
            TV_Check_NO.setBackgroundResource(R.drawable.btmenu_primarc)
            TV_Check_NA.setBackgroundResource(R.drawable.btmenu_secundlight)
        }else if (rta1 == "3") {
            TV_Check_SI.setBackgroundResource(R.drawable.btmenu_secundlight)
            TV_Check_NO.setBackgroundResource(R.drawable.btmenu_secundlight)
            TV_Check_NA.setBackgroundResource(R.drawable.btmenu_primarc)
        }
        if (tipofoto == "2"){
            if (fototom == "1"){
                ivTomarFoto.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
            } else if(resp=="0" || resp=="2"){
                ivTomarFoto.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            } else {
                ivTomarFoto.setImageResource(android.R.drawable.ic_menu_camera)
            }
        } else if (tipofoto.toInt() > 2){
                if (fototom == "1"){
                    ivTomarFoto.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
                }else {
                    ivTomarFoto.setImageResource(R.drawable.ic_camera_alt_black_24dp)
                }
        }
        verificacl()
    }

    // SE CARGAN LA INFORMACION DE LA PANTALLA EN LOS PARAMETROS DE LA BASE DE DATOS
    private fun cargarparametros() {
        idtarea = fg?.parametro(requireContext(),"TAREA_ACT").toString()
        idpregunta = fg?.parametro(requireContext(),"PREGUNTA_ACT").toString()
        val aplica_check = fg?.getQ1(requireContext(),
            "select apcheck from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()
        val aplica_txt =  fg?.getQ1(requireContext(),
            "select aptxt from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()
        val aplica_foto =  fg?.getQ1(requireContext(),
            "select apfoto from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()
        val aplica_selector =  fg?.getQ1(requireContext(),
            "select apsel from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()

        val tipofoto = fg?.getQ1(requireContext(),
            "select fototipo from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()
        val tipo_texto = fg?.getQ1(requireContext(),
            "select tipotxt from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()

        val rta_check = fg?.getQ1(requireContext(),
            "select rta1 from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()
        val rta_texto= fg?.getQ1(requireContext(),
            "select obs from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()
        val fototomada = fg?.getQ1(requireContext(),
            "select case ifnull(count(*),0) WHEN 0 THEN 0 ELSE 1 END AS crf from '302_FOTOS_RESP_TEMP' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()
        val rta_selector = fg?.getQ1(requireContext(),
            "select rta2 from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpregunta'"
        ).toString()

        fg?.act_param(requireContext(),"AP_CHECK",aplica_check)
        fg?.act_param(requireContext(),"AP_TXT",aplica_txt)
        fg?.act_param(requireContext(),"AP_FOTO",aplica_foto)
        fg?.act_param(requireContext(),"AP_SELECT",aplica_selector)

        fg?.act_param(requireContext(),"TIPO_TXT",tipo_texto)
        fg?.act_param(requireContext(),"TIPO_FOTO",tipofoto)

        fg?.act_param(requireContext(),"RTA1",rta_check)
        fg?.act_param(requireContext(),"TXT_VALOR",rta_texto)
        fg?.act_param(requireContext(),"FTOM",fototomada)
        fg?.act_param(requireContext(),"RTA2",rta_selector.toString(                                                                                                                                                                  ))
    }

    private fun verificacl() {
        if (ap_check == "0"){
            CL_Check_SI.visibility = View.GONE
            CL_Check_NO.visibility = View.GONE
            CL_Check_NA.visibility = View.GONE
        }else{
            CL_Check_SI.visibility = View.VISIBLE
            CL_Check_NO.visibility = View.VISIBLE
            CL_Check_NA.visibility = View.VISIBLE
        }
        if (ap_txt == "0"){
            CL_Observaciones.visibility = View.GONE
        }else {
            CL_Observaciones.visibility = View.VISIBLE
            if (tipotxt == "3"){
                RB_Obligatorio.visibility = View.VISIBLE
            }else{
                RB_Obligatorio.visibility = View.GONE
            }
        }
        if (ap_foto == "0"){
            CL_TomarFoto.visibility = View.GONE
        }else{
            CL_TomarFoto.visibility = View.VISIBLE
        }
        if (ap_select.equals("0")){
            CL_RecyclerSelector.visibility = View.GONE
        }else{
            val nselect = fg?.getQ1(requireContext(),"select selector from '106_SELECTOR' where idselector in (select idselector from '204_PREGGEN' where idtarea='$idtarea' and idpreg = '$idpregunta')").toString()
            TV_Titulo_Selector.text = nselect
            CL_RecyclerSelector.visibility = View.VISIBLE
        }
    }

    private fun rellenarvariables() {
        verificacl()
        if (obs != "" && obs != "0"){
            tv_Observacion.setText(obs)
        }else {
            tv_Observacion.hint = fg?.getQ1(requireContext(),
                "select labeltext from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregunta'"
            ).toString()
        }
       actualizacheck(rta1)
        if(ap_select == "1"){
            cargarselector()
        }
    }

    private fun acteval(trta: Int) {
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

    private fun cargarselector() {
        val conGen = ConsultaGeneral()
        rta2 = fg?.getQ1(requireContext(),
            "Select rta2 from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg = '$idpregunta'"
        ).toString()
        val queryAct =
            "SELECT idselelemento,selelemento,0 FROM '107_SEL_ELEMENTO' where idselector in (select idselector from '204_PREGGEN' where idtarea='$idtarea' and idpreg = '$idpregunta')"
        val preguntas = conGen.queryObjeto2val(
            context, queryAct, null
        )
        lista!!.clear()
        if (preguntas != null) {
            OpcionesID = arrayOfNulls(preguntas.size)
            OpcionesTID = arrayOfNulls(preguntas.size)
            OpcionesRtaID = arrayOfNulls(preguntas.size)
            for (c in preguntas.indices) {
                OpcionesID[c] = preguntas[c][0].toString()
                OpcionesTID[c] = preguntas[c][1].toString()
                if (rta2 == OpcionesID[c].toString()){
                    OpcionesRtaID[c] = rta2
                }else{
                    OpcionesRtaID[c] = preguntas[c][2].toString()
                }
                val am = ActivacionModel(OpcionesTID[c], OpcionesRtaID[c])
                lista?.add(am)
            }
        }else{
            OpcionesID = arrayOfNulls(1)
            OpcionesTID = arrayOfNulls(1)
            OpcionesRtaID = arrayOfNulls(1)
            OpcionesID[0] = "999"
            OpcionesTID[0] = "Sin Listas Para Cargar"
            OpcionesRtaID[0] = "999"
            at?.actualizar301_temp(requireContext(),idtarea, idpregunta , OpcionesID[0]!!)
            val am = ActivacionModel(OpcionesTID[0], OpcionesID[0])
            lista?.add(am)
        }
        val aa = ActivacionAdapter(lista!!)
        recyclerpreguntas!!.adapter = aa
    }

    private fun actselector(pos: Int, holder: Activ, preguntas: ActivacionModel?) {
        val resp_txt = tv_Observacion.text.toString()
        if(resp_txt != "0" && resp_txt != ""){
            fg?.ejecDB(requireContext(),
                "UPDATE '301_RESPUESTAS_TEMP' set obs='$resp_txt' where idtarea='$idtarea' and idpreg='$idpregunta'"
            )
        }
        at?.actualizar301_temp(requireContext(),idtarea, idpregunta , OpcionesID[pos]!!)
        fg?.act_param(requireContext(),"RTA2", OpcionesID[pos]!!)
        rta2 = OpcionesID[pos].toString()
        cargarselector()
    }

    private fun desplazamientos(Boton: Int) {
        var txttoast =  "Para finalizar este item, usted debe completar lo siguiente: "
        val resp_txt = tv_Observacion.text.toString()
        if(resp_txt != "0" && resp_txt != ""){
            fg?.ejecDB(requireContext(),
                "UPDATE '301_RESPUESTAS_TEMP' set obs='$resp_txt' where idtarea='$idtarea' and idpreg='$idpregunta'"
            )
        }
        var rtax = ""
        var trta = 0
        var Tipox = ""
        val querypreguntasrtaF =
            "SELECT count(*) as cres FROM '302_FOTOS_RESP_TEMP' where idtarea='$idtarea' and idpreg='$idpregunta'"
        val rtapreguntasF = fg?.getQ1(requireContext(),querypreguntasrtaF)
        rtax = rtapreguntasF.toString()
        if (((ap_foto == "0") || (ap_foto == "1" && tipofoto != "2"  && rtax.toInt() >0) || (ap_foto == "1" && tipofoto == "2" && rta1 == "2" && rtax.toInt() >0) || (ap_foto == "1" && tipofoto == "2" && rta1 != "2" )) && ((resp_txt != "0" && resp_txt != "") || ap_txt == "0" || tipotxt == "2") &&
            (ap_check == "0" || rta1 != "0") && (ap_select == "0" || rta2 != "0")) {
            fg?.ejecDB(requireContext(),
                "UPDATE '204_PREGGEN' SET eval='1' WHERE idtarea='$idtarea' and idpreg='$idpregunta'"
            )
            trta = 1
        } else {
            trta = 0
            fg?.ejecDB(requireContext(),
                "UPDATE '204_PREGGEN' SET eval='0' WHERE idtarea='$idtarea' and idpreg='$idpregunta'"
            )
        }
        if (!((ap_foto == "0") || (ap_foto == "1" && tipofoto != "2"  && rtax.toInt() >0) || (ap_foto == "1" && tipofoto == "2" && rta1 == "2" && rtax.toInt() >0) || (ap_foto == "1" && tipofoto == "2" && rta1 != "2" ))){
            txttoast = txttoast + " - Tome al menos una foto"
        }
        if (!((resp_txt != "0" && resp_txt != "") || ap_txt == "0" || tipotxt == "2")){
            txttoast = txttoast + " - Rellene el texto"
        }
        if (!(ap_check == "0" || rta1 != "0")){
            txttoast = txttoast + " - Seleccione alguna opcion del check"
        }
        if (!(ap_select == "0" || rta2 != "0")){
            txttoast = txttoast + " - Seleccione algun opcion del selector"
        }

        if (trta == 0){
        Toast.makeText(requireContext(),txttoast,
            Toast.LENGTH_LONG).show()
        }

        try {
                acteval(trta)
            } finally {
                openFragment(tpant)
            }
    }

    fun leerParametros() {
        useruid = fg?.parametro(requireContext(),"USERUID").toString()
        idtarea = fg?.parametro(requireContext(),"TAREA_ACT").toString()
        ntarea = fg?.parametro(requireContext(),"NTAREA_ACT").toString()
        ncliente_act = fg?.parametro(requireContext(),"NCLIENTE_ACT").toString()
        horario = fg?.parametro(requireContext(),"HORARIO_ACT").toString()
        idformulario = fg?.parametro(requireContext(),"FORMULARIO_ACT").toString()
        nformulario = fg?.parametro(requireContext(),"NFORMULARIO_ACT").toString()
        idforma = fg?.parametro(requireContext(),"FORMA_ACT").toString()
        nforma = fg?.parametro(requireContext(),"NFORMA_ACT").toString().toString()
        tituloact = fg?.parametro(requireContext(),"TITULO_ACT").toString()
        idgrupo = fg?.parametro(requireContext(),"GRUPO_ACT").toString()
        nogrupo = fg?.parametro(requireContext(),"NGRUPO_ACT").toString()
        idsgrupo = fg?.parametro(requireContext(),"SUBGRUPO_ACT").toString()
        nsgrupo = fg?.parametro(requireContext(),"NSUBGRUPO_ACT").toString()
        idpregunta = fg?.parametro(requireContext(),"PREGUNTA_ACT").toString()
        npregunta = fg?.parametro(requireContext(),"NPREGUNTA_ACT").toString()
        tipopreg = fg?.parametro(requireContext(),"TIPO_PREGUNTA").toString()
        tipocarga = fg?.parametro(requireContext(),"TIPOCARGA").toString()
        ordenac = fg?.parametro(requireContext(),"ORDEN_ACT").toString()
        estado = fg?.parametro(requireContext(),"ESTADO_ACT").toString()
        opn = fg?.parametro(requireContext(),"OPN_ACT").toString()
        obs_gen = fg?.parametro(requireContext(),"OBS_GEN").toString()
        autoriza = fg?.parametro(requireContext(),"AUTORIZA").toString()
        tipofirma = fg?.parametro(requireContext(),"TIPO_FIRMA").toString()
        firmacli = fg?.parametro(requireContext(),"FIRMA_CLI").toString()
        firmauser = fg?.parametro(requireContext(),"FIRMA_USER").toString()
        nom_cli = fg?.parametro(requireContext(),"NOM_CLI").toString()
        ident_cli = fg?.parametro(requireContext(),"IDENT_CLI").toString()
        ap_check = fg?.parametro(requireContext(),"AP_CHECK").toString()
        ap_txt = fg?.parametro(requireContext(),"AP_TXT").toString()
        ap_foto = fg?.parametro(requireContext(),"AP_FOTO").toString()
        ap_select = fg?.parametro(requireContext(),"AP_SELECT").toString()
        rta1 = fg?.parametro(requireContext(),"RTA1").toString()
        rta2 = fg?.parametro(requireContext(),"RTA2").toString()
        obs = fg?.parametro(requireContext(),"TXT_VALOR").toString()
        fototom = fg?.parametro(requireContext(),"FTOM").toString()
        tipotxt = fg?.parametro(requireContext(),"TIPO_TXT").toString()
        tipofoto = fg?.parametro(requireContext(),"TIPO_FOTO").toString()
        TVNPantalla.text = tituloact
        rellenarvariables()
    }

    private fun openFragment(strCase: String) {
        when (strCase) {
            "97" -> {
                //val action =
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
        var instance: FormTipo11? = null
    }
}