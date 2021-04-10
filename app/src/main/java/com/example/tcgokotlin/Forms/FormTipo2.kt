package com.example.tcgokotlin.Forms

import android.os.Bundle
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
import com.example.tcgokotlin.sqliteDBHelper.ProcesosTablas
import kotlinx.android.synthetic.main.activity_form_tipo2.*
import java.util.*


class FormTipo2 : Fragment() {
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
    private var ordact = ""
    private var panact: String = ""
    private var lista: ArrayList<ActivacionModel>? = null
    private var idpreg: String = ""
    private var preg: String = ""
    private var fotoa: String = ""
    private var respselector: String = ""
    private var recyclerpreguntas: RecyclerView? = null
    private lateinit var opcionesid: Array<String?>
    private lateinit var opcionestid: Array<String?>
    private lateinit var opcionesrtaid: Array<String?>
    private var orden: String = ""
    private var grupo: String = ""
    private var tpant: String = ""
    var iv: ImageView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.activity_form_tipo2, container, false)
    }

    inner class Activ(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cbv: ImageView = itemView.findViewById<View>(R.id.imgRBV) as ImageView
        var nombreopcion: TextView = itemView.findViewById<View>(R.id.tvNombreOpcionRD) as TextView
    }

    inner class ActivacionModel(var nombreopcion: String, var rtack: String)
    internal inner class ActivacionAdapter(private var lista: ArrayList<ActivacionModel>) :
        RecyclerView.Adapter<Activ>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Activ {
            return Activ(
                LayoutInflater.from(parent.context).inflate(R.layout.ver_choice, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Activ, position: Int) {
            val selector = lista[position]
            holder.nombreopcion.text = selector.nombreopcion
            if (selector.rtack != "0") {
                holder.cbv.setImageResource(R.drawable.rdb)
            } else {
                holder.cbv.setImageResource(R.drawable.rdnoselb)
            }
            holder.cbv.setOnClickListener { editartipo2(position) }
            holder.nombreopcion.setOnClickListener {
                editartipo2(position)
            }
        }

        override fun getItemCount(): Int {
            return lista.size
        }
    }


    override fun onStart() {
        super.onStart()
        
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        recyclerpreguntas = recyclerTipo2
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerpreguntas!!.layoutManager = llm
        lista = ArrayList()

        idtarea = fg.parametro(requireContext(),"TAREA_ACT")
        ntarea = fg.parametro(requireContext(),"NTAREA_ACT")
        idformulario = fg.parametro(requireContext(),"FORMULARIO_ACT")
        nformulario = fg.parametro(requireContext(),"NFORMULARIO_ACT")
        idforma = fg.parametro(requireContext(),"FORMA_ACT")
        nforma = fg.parametro(requireContext(),"NFORMA_ACT")
        idgrupo = fg.parametro(requireContext(),"GRUPO_ACT")
        ngrupo = fg.parametro(requireContext(),"NGRUPO_ACT")
        idsgrupo = fg.parametro(requireContext(),"SUBGRUPO_ACT")
        nsgrupo = fg.parametro(requireContext(),"NSUBGRUPO_ACT")
        tipoform = fg.parametro(requireContext(),"TIPO_GRP")
        panact = fg.parametro(requireContext(),"ULTIMAP")
        idpregact = fg.parametro(requireContext(),"PREGUNTA_ACT")
        titulopant = fg.parametro(requireContext(),"TITULO_ACT")
        grupo = idforma
        ordact = fg.getQ1(requireContext(),"select orden from '002_PREGUNTAS'")
        ordact = if (panact == "97"){
            fg.getQ1(requireContext(),
                "select orden from '204_PREGGEN' where idtarea='$idtarea' and idpreg='$idpregact'"
            )
        }else {
            fg.getQ1(requireContext(),"select orden from '002_PREGUNTASB'")
        }
        orden = ordact


        val querypreguntasid =
            "SELECT idpreg,preg,cnd2,fotoap FROM '002_PREGUNTASB'"
        val actidpreg = conGen.queryObjeto2val(
            context, querypreguntasid, null
        )
        idpreg = actidpreg[0][0].toString()
        preg = actidpreg[0][1] .toString()
        val cond2 = actidpreg[0][2].toString()
        fotoa = actidpreg[0][3].toString()
        TVNPantalla.text = titulopant
        TV_Info_preg.text = preg


        val valcn = if (cond2 == "0") {
            "1"
        } else {
            val condicional = fg.getQ1(requireContext(),cond2)
            condicional
        }

        if (valcn.toInt() > 0) {
            listarpreguntas()
        } else {
            val queryActextx = when (panact) {
                "97" -> {
                    "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '003_PREGUNTSF' where orden>$orden"
                }
                "98" -> {
                    "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '002_PREGUNTASC' where orden>$orden"
                }
                else -> {
                    "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '002_PREGUNTAS' where orden>$orden"
                }
            }
            
            val actidexx = conGen.queryObjeto2val(
                context, queryActextx, null
            )
            val querypreguntasrtaF =
                "SELECT count(rta1) as cres FROM '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpreg' and rta1<>'0'"
            val rtax = fg.getQ1(requireContext(),querypreguntasrtaF)

            val trta = if (rtax.toInt() > 0) {
                1
            } else {
                0
            }
            if (rtax.toInt() > 0) {
                fg.ejecDB(requireContext(),
                    "UPDATE '204_PREGGEN' SET eval='1' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
                )
            } else {
                fg.ejecDB(requireContext(),
                    "UPDATE '204_PREGGEN' SET eval='0' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
                )
            }
            acteval(trta)

            if (actidexx[0][0].toInt() > 0) {
                val querymino = when (panact) {
                    "97" -> {
                        "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '003_PREGUNTSF' where idform='" + actidexx[0][1].toString() + "'"
                    }
                    "98" -> {
                        "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '002_PREGUNTASC' where orden=" + actidexx[0][1].toString()
                    }
                    else -> {
                        "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '002_PREGUNTAS' where orden=" + actidexx[0][1].toString()
                    }
                }
                val mino = conGen.queryObjeto2val(
                    context, querymino, null
                )
                fg.act_param(requireContext(),"FORMA_ACT", mino[0][1].toString())
                fg.act_param(requireContext(),"GRUPO_ACT", mino[0][2].toString())
                fg.act_param(requireContext(),"SUBGRUPO_ACT", mino[0][3].toString())
                fg.act_param(requireContext(),"ORDEN_ACT", mino[0][4].toString())
                fg.act_param(requireContext(),"PREGUNTA_ACT", mino[0][5].toString())
                fg.act_param(requireContext(),"NPREGUNTA_ACT", mino[0][6].toString())
                val tipox = mino!![0]!![0].toString()
                openFragment(tipox)
            } else {
                try {
                    acteval(trta)
                } finally {
                    openFragment(tpant)
                }
            }
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
    }

    private fun acteval(trta: Int) {
        val fg = FuncionesGenerales()
        when (panact) {
            "97" -> {
                fg.ejecDB(requireContext(),"UPDATE '201_PREGFORM' SET ev='" + (trta * 100).toString() + "' WHERE idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "'")
                if(respselector == "2"){
                    fg.ejecDB(requireContext(),
                        "UPDATE '201_PREGFORM' SET ev='100' WHERE idtarea='$idtarea' and idformulario='$idformulario' and (idform='2' or idform='3' or idform='4')"
                    )
                }
                if(respselector == "1"){
                    val foto1 = fg.getQ1(requireContext(),
                        "select count(*) from '302_FOTOS_RESP_TEMP' where  idtarea='$idtarea' and idpreg='GEN_6'"
                    )
                    val foto2 = fg.getQ1(requireContext(),
                        "select count(*) from '302_FOTOS_RESP_TEMP' where  idtarea='$idtarea' and idpreg='GEN_7'"
                    )
                    val foto3 = fg.getQ1(requireContext(),
                        "select count(*) from '302_FOTOS_RESP_TEMP' where  idtarea='$idtarea' and idpreg='GEN_8'"
                    )
                    if (foto1 == "0"){
                        fg.ejecDB(requireContext(),
                            "UPDATE '201_PREGFORM' SET ev='0' WHERE idtarea='$idtarea' and idformulario='$idformulario' and idform='2'"
                        )
                    }
                    if (foto2 == "0"){
                        fg.ejecDB(requireContext(),
                            "UPDATE '201_PREGFORM' SET ev='0' WHERE idtarea='$idtarea' and idformulario='$idformulario' and idform='3'"
                        )
                    }
                    if (foto3 == "0"){
                        fg.ejecDB(requireContext(),
                            "UPDATE '201_PREGFORM' SET ev='0' WHERE idtarea='$idtarea' and idformulario='$idformulario' and idform='4'"
                        )
                    }
                }
                tpant = "97"
            }
            "98" -> {
                fg.ejecDB(requireContext(),
                    "UPDATE '202_PREGGRUP' SET ev='$trta' WHERE idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma' and grpid='$idgrupo'"
                )
                tpant = "98"
            }
            "99" -> {
                fg.ejecDB(requireContext(),
                    "UPDATE '203_PREGSGRUP' SET ev='$trta' WHERE idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma' and grpid='$idgrupo' and sgrpid='$idsgrupo'"
                )
                tpant = "99"
            }
        }
    }

    private fun listarpreguntas() {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        respselector = fg.getQ1(requireContext(),
            "Select rta2 from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg = '$idpreg'"
        )
        val queryAct =
            "SELECT '205_PREGGEND'.opn,'205_PREGGEND'.opt,rta1 FROM '301_RESPUESTAS_TEMP' JOIN '205_PREGGEND' on '301_RESPUESTAS_TEMP'.idtarea = '205_PREGGEND'.idtarea and '301_RESPUESTAS_TEMP'.idpreg = '205_PREGGEND'.idpreg where '301_RESPUESTAS_TEMP'.idtarea='$idtarea' and '301_RESPUESTAS_TEMP'.idpreg = '$idpreg' order by 1 asc"
        val preguntas = conGen.queryObjeto2val(
            context, queryAct, null
        )
        lista!!.clear()
        if (preguntas != null) {
            opcionesid = arrayOfNulls(preguntas.size)
            opcionestid = arrayOfNulls(preguntas.size)
            opcionesrtaid = arrayOfNulls(preguntas.size)
            for (c in preguntas.indices) {
                opcionesid[c] = preguntas[c][0]
                opcionestid[c] = preguntas[c][1]
                if (respselector == opcionesid[c]){
                    opcionesrtaid[c] = respselector
                }else{
                    opcionesrtaid[c] = preguntas[c][2]
                }
                val am = ActivacionModel(opcionestid[c]!!, opcionesrtaid[c]!!)
                lista?.add(am)
            }
        }
        val aa = ActivacionAdapter(lista!!)
        recyclerpreguntas!!.adapter = aa
    }

    fun editartipo2(pos: Int) {
        val at = ProcesosTablas()
        val fg = FuncionesGenerales()

        at.actualizar301_temp(requireContext(), idtarea, idpreg, opcionesid[pos].toString())
        respselector = opcionesid[pos].toString()
        val trta = if (respselector.toInt() > 0) {
            1
        } else {
            0
        }
        if (respselector.toInt() > 0) {
            fg.ejecDB(requireContext(),
                "UPDATE '204_PREGGEN' SET eval='1' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
            )
        } else {
            fg.ejecDB(requireContext(),
                "UPDATE '204_PREGGEN' SET eval='0' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
            )
        }
        acteval(trta)
        listarpreguntas()
    }

    private fun desplazamientos(Boton: Int) {
        val fg = FuncionesGenerales()
        val conGen = ConsultaGeneral()
        val querypreguntasrtaF =
            "SELECT rta2 as cres FROM '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpreg'"
        respselector = fg.getQ1(requireContext(),querypreguntasrtaF)
        val trta = if (respselector != "0") 1 else 0
        if (respselector != "0") {
            fg.ejecDB(requireContext(),
                "UPDATE '204_PREGGEN' SET eval='1' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
            )
        } else {
            fg.ejecDB(requireContext(),
                "UPDATE '204_PREGGEN' SET eval='0' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
            )
        }
        if (Boton != 3) {
            val querysigp = if (Boton == 2) {
                when (panact) {
                    "97" -> {
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='$idtarea' and orden>$orden"
                    }
                    "98" -> {
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='$idtarea' and orden>$orden"
                    }
                    else -> {
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='$idtarea' and orden>$orden"
                    }
                }
            } else {
                when (panact) {
                    "97" -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='$idtarea' and orden<$orden"
                    }
                    "98" -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='$idtarea' and orden<$orden"
                    }
                    else -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '204_PREGGEN' where idtarea='$idtarea' and orden<$orden"
                    }
                }
            }
            val ordensig = conGen.queryObjeto2val(
                context, querysigp as String?, null
            )
            if (ordensig[0][0].toInt() > 0) {
                val querymino = if (panact == "97") {
                    "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg,cnd1 FROM '204_PREGGEN' where idtarea='" + idtarea + "' and idform='" + ordensig[0]!![1].toString() + "'"
                } else {
                    "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg,cnd1 FROM '204_PREGGEN' where idtarea='" + idtarea + "' and orden=" + ordensig[0]!![1].toString()
                }
                val mino = conGen.queryObjeto2val(
                    context, querymino, null
                )
                val consultamostrar = mino[0][7].toString()
                val mostrar = fg.getQ1(requireContext(),consultamostrar)
                if (mostrar != "0" && (mostrar != "")) {

                fg.act_param(requireContext(),"FORMA_ACT", mino[0][1].toString())
                fg.act_param(requireContext(),
                    "NFORMA_ACT",
                    fg.getQ1(requireContext(),"Select form from '201_PREGFORM' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "'")
                )
                fg.act_param(requireContext(),"GRUPO_ACT", mino[0][2].toString())
                fg.act_param(requireContext(),
                    "NGRUPO_ACT",
                    fg.getQ1(requireContext(),"Select grp from '202_PREGGRUP' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "' and grpid='" + mino[0][2].toString() + "'")
                )
                fg.act_param(requireContext(),"SUBGRUPO_ACT", mino[0][3].toString())
                fg.act_param(requireContext(),
                    "NSUBGRUPO_ACT",
                    fg.getQ1(requireContext(),"Select sgrp from '203_PREGSGRUP' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "' and grpid='" + mino[0][2].toString() + "' and sgrpid='" + mino[0][3].toString() + "'")
                )

                fg.act_param(requireContext(),"ORDEN_ACT", mino[0][4].toString())
                fg.act_param(requireContext(),"PREGUNTA_ACT", mino[0][5].toString())
                fg.act_param(requireContext(),"NPREGUNTA_ACT", mino[0][6].toString())
                fg.act_param(requireContext(),"TITULO_ACT", mino[0][6].toString())
                    acteval(trta)
                    val tipox = mino[0][0].toString()
                    openFragment(tipox)
                }else{
                    acteval(trta)
                    openFragment(tpant)
                }
            } else {
                try {
                    acteval(trta)
                } finally {
                    openFragment(tpant)
                }
            }
        } else {
            try {
                acteval(trta)
            } finally {
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
        var instance: FormTipo2? = null
    }
}