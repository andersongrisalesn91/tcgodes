package com.example.tcgokotlin.Forms


import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import android.widget.EditText
import android.os.Bundle
import com.example.tcgokotlin.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tcgokotlin.sqliteDBHelper.Backups
import kotlinx.android.synthetic.main.activity_form_tipo3.*
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral

class FormTipo3 : Fragment() {
     private var rango1: String = ""
     private var rango2: String = ""
     private var tpant: String = ""
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
     private var tipoforma: String = ""
     private var idpregact: String = ""
     private var panact: String = ""
     private var idpreg: String = ""
     private var pregu: String = ""
     private var fotoa: String = ""
     private var useruid: String = ""
     private lateinit var valcampo: EditText
     private var orden: String = ""
     private var grupo: String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.activity_form_tipo3, container, false)
    }

    override fun onStart() {
        super.onStart()
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
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
        tipoforma = fg.parametro(requireContext(),"TIPO_GRP")
        panact = fg.parametro(requireContext(),"ULTIMAP")
        idpregact = fg.parametro(requireContext(),"PREGUNTA_ACT")
        titulopant = fg.parametro(requireContext(),"TITULO_ACT")
        grupo = idforma
        orden = fg.getQ1(requireContext(),"select orden from '002_PREGUNTAS'")
        useruid = fg.parametro(requireContext(),"USERUID")
        val querypreguntasid = "SELECT idpreg,preg,cnd2,fotoap ,eval,r1,r2 FROM '002_PREGUNTAS'"
        val actidpreg = conGen.queryObjeto2val(
            context, querypreguntasid, null
        )
        idpreg = actidpreg[0][0].toString()
        pregu = actidpreg[0][1].toString()
        val cond2 = actidpreg[0][2].toString()
        fotoa = actidpreg[0][3].toString()

        TVNPantalla.text = titulopant
        TV_Info_preg.text = pregu
        rango1 = actidpreg[0][5].toString()
        rango2 = actidpreg[0][6].toString()
        var valcn = "0"
        if (cond2 == "0") {
            valcn = "1"
        }
        if (cond2 != "0") {
            val condicional = fg.getQ1(requireContext(),cond2)
            valcn = condicional
        }
        if (valcn.toInt() > 0) {
            val querypreguntasrta =
                "SELECT ifnull(obs,'0') as obser FROM '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='$idpreg'"
            val rtapreguntas = fg.getQ1(requireContext(),querypreguntasrta)

            if (rtapreguntas != "0") {
                tv_req_text.setText(rtapreguntas)
            }
            val queryFoto =
                "INSERT OR IGNORE INTO '301_RESPUESTAS_TEMP' (idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,opt,obs,gpslat,gpslon,useruid) VALUES('$idtarea' , '$idformulario' , '$idforma' , '$idgrupo' , '$idsgrupo' , '$idpregact' , '0' , '0' , '0' , '0' , '0' , '$useruid');"
            fg.ejecDB(requireContext(),queryFoto)

            var lonobs = fg.getQ1(requireContext(),
                "select length(obs) obsaplica from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma' and grpid='$idgrupo' and sgrpid='$idsgrupo'"
            )

            if (rango1.equals(null) || rango1 == "") {
                rango1 = "0"
            }
            if (lonobs.equals(null) || lonobs == "") {
                lonobs = "0"
            }
            val rtax = if (lonobs.toInt()>=rango1.toInt()) 1 else 0

            val trta = if (rtax > 0) 1 else 0

            if (rtax > 0) {
                fg.ejecDB(requireContext(),
                    "UPDATE '204_PREGGEN' SET eval='1' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
                )
            } else {
                fg.ejecDB(requireContext(),
                    "UPDATE '204_PREGGEN' SET eval='0' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
                )
            }
            acteval(trta)
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
            val rtax = fg.getQ1(requireContext(),
                "select CASE obs WHEN length(obs) >= $rango1 THEN 1 ELSE 0 END obsaplica from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma' and grpid='$idgrupo' and sgrpid='$idsgrupo'"
            )
            val trta = if (rtax.toInt() > 0) 1 else 0
            if (rtax.toInt() > 0) {
                fg.ejecDB(requireContext(),
                    "UPDATE '204_PREGGEN' SET eval='1' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
                )
            } else {
                fg.ejecDB(requireContext(),
                    "UPDATE '204_PREGGEN' SET eval='0' WHERE idtarea='$idtarea' and idpreg='$idpreg'"
                )
            }
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
                fg.act_param(requireContext(),"FORMA_ACT",mino[0][1].toString())
                fg.act_param(requireContext(),"NFORMA_ACT",fg.getQ1(requireContext(),"Select form from '201_PREGFORM' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "'"))
                fg.act_param(requireContext(),"GRUPO_ACT",mino[0][2].toString())
                fg.act_param(requireContext(),"NGRUPO_ACT",fg.getQ1(requireContext(),"Select grp from '202_PREGGRUP' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "' and grpid='" + mino[0][2].toString() + "'"))
                fg.act_param(requireContext(),"SUBGRUPO_ACT",mino[0][3].toString())
                fg.act_param(requireContext(),"NSUBGRUPO_ACT",fg.getQ1(requireContext(),"Select sgrp from '203_PREGSGRUP' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "' and grpid='" + mino[0][2].toString()  + "' and sgrpid='" + mino[0][3].toString() + "'"))
                fg.act_param(requireContext(),"ORDEN_ACT", mino[0][4].toString())
                fg.act_param(requireContext(),"PREGUNTA_ACT", mino[0][5].toString())
                fg.act_param(requireContext(),"NPREGUNTA_ACT", mino[0][6].toString())
                val tipox = mino[0][0].toString()
                acteval(trta)
                openFragment(tipox)
            } else {
                try{
                    acteval(trta)
                }finally {
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

    private fun acteval(trta: Int){
        val fg = FuncionesGenerales()
        when (panact) {
            "97" -> {
                fg.ejecDB(requireContext(),
                    """UPDATE '201_PREGFORM' SET ev='${trta * 100}' WHERE idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma'"""
                )
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

    private fun desplazamientos(Boton: Int) {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val rtatxt = valcampo.text.toString()

        fg.ejecDB(requireContext(),
            "UPDATE '301_RESPUESTAS_TEMP' SET obs='$rtatxt' WHERE idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma' and grpid='$idgrupo' and sgrpid='$idsgrupo' and idpreg='$idpreg'"
        )
        val lonobs = fg.getQ1(requireContext(),
            "select length(obs) obsaplica from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma' and grpid='$idgrupo' and sgrpid='$idsgrupo'"
        )
        val rtax = if (lonobs.toInt()>=rango1.toInt()) 1 else 0
        val trta = if (rtax > 0) 1 else 0

        if (rtax > 0) {
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
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '003_PREGUNTSF' where orden>$orden"
                    }
                    "98" -> {
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '002_PREGUNTASC' where orden>$orden"
                    }
                    else -> {
                        "SELECT count(orden) as co,ifnull(min(orden) ,0) as mo FROM '002_PREGUNTAS' where orden>$orden"
                    }
                }
            } else {
                when (panact) {
                    "97" -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '003_PREGUNTSF' where orden<$orden"
                    }
                    "98" -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '002_PREGUNTASC' where orden<$orden"
                    }
                    else -> {
                        "SELECT count(orden) as co,ifnull(max(orden) ,0) as mo FROM '002_PREGUNTAS' where orden<$orden"
                    }
                }
            }
            val ordensig = conGen.queryObjeto2val(
                context, querysigp as String?, null
            )
            if (ordensig[0][0].toInt() > 0) {
                val querymino = when (panact) {
                    "97" -> {
                        "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '003_PREGUNTSF' where idform='" + ordensig[0][1].toString() + "'"
                    }
                    "98" -> {
                        "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '002_PREGUNTASC' where orden=" + ordensig[0][1].toString()
                    }
                    else -> {
                        "SELECT tipo,idform,grpid,sgrpid,orden,idpreg,preg FROM '002_PREGUNTAS' where orden=" + ordensig[0][1].toString()
                    }
                }
                val mino = conGen.queryObjeto2val(
                    context, querymino, null
                )
                fg.act_param(requireContext(),"FORMA_ACT",mino[0][1].toString())
                fg.act_param(requireContext(),"NFORMA_ACT",fg.getQ1(requireContext(),"Select form from '201_PREGFORM' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "'"))
                fg.act_param(requireContext(),"GRUPO_ACT",mino[0][2].toString())
                fg.act_param(requireContext(),"NGRUPO_ACT",fg.getQ1(requireContext(),"Select grp from '202_PREGGRUP' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "' and grpid='" + mino[0][2].toString() + "'"))
                fg.act_param(requireContext(),"SUBGRUPO_ACT",mino[0][3].toString())
                fg.act_param(requireContext(),"NSUBGRUPO_ACT",fg.getQ1(requireContext(),"Select sgrp from '203_PREGSGRUP' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "' and grpid='" + mino[0][2].toString()  + "' and sgrpid='" + mino[0][3].toString() + "'"))
                fg.act_param(requireContext(),"ORDEN_ACT", mino[0][4].toString())
                fg.act_param(requireContext(),"PREGUNTA_ACT", mino[0][5].toString())
                fg.act_param(requireContext(),"NPREGUNTA_ACT", mino[0][6].toString())
                fg.act_param(requireContext(),"TITULO_ACT", mino[0][6].toString())
                val tipox = mino[0][0].toString()
                acteval(trta)
                openFragment(tipox)
            } else {
                try{
                    acteval(trta)
                }finally {
                    openFragment(tpant)
                }
            }
        } else {
            try{
                acteval(trta)
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
        var instance: FormTipo3? = null
    }
}
