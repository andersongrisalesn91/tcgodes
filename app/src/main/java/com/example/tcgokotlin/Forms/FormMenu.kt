package com.example.tcgokotlin.Forms

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_form_menu.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tcgokotlin.R
import com.example.tcgokotlin.Sincronizar.CargarEstados
import com.example.tcgokotlin.Sincronizar.SincronizarOnline
import com.example.tcgokotlin.data.model.MenuFM
import com.example.tcgokotlin.data.model.MenuForm
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.sqliteDBHelper.LoadTaskSqlite
import com.example.tcgokotlin.utils.Common
import kotlinx.android.synthetic.main.stanby_layout.*
import kotlinx.android.synthetic.main.activity_form_menu.TVNPantalla
import kotlinx.android.synthetic.main.activity_form_menu.buttonIrMenu
import kotlinx.android.synthetic.main.activity_form_menu.buttonStanBy
import kotlinx.android.synthetic.main.activity_form_menu.recycler_items_menu
import java.util.*

class FormMenu : Fragment() {
    // Inicio Variables Globales de todas las pantallas
    private var useruid: String = ""
    private var panact: String = ""
    private var idtarea: String = ""
    private var ntarea: String = ""
    private var nclienteact: String = ""
    private var horario: String = ""
    private var idformulario: String = ""
    private var nformulario: String = ""
    private var idforma: String = ""
    private var nforma: String = ""
    private var tipoform: String = ""
    private var tituloact: String = ""
    private var estado: String = ""
    private var opn: String = ""
    // Fin variables Globales de todas las Pantallas
    private var canregsgru: String = "0"
    private var recyclerMenuForm: RecyclerView? = null
    private var lista: ArrayList<MenuFM>? = null
    private lateinit var grupo: Array<String?>
    private lateinit var ngrupo: Array<String?>
    private lateinit var evaluados: Array<String?>
    private var canitems = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        if ((this.activity as AppCompatActivity).supportActionBar != null) (activity as AppCompatActivity).supportActionBar?.hide()
        return inflater.inflate(R.layout.activity_form_menu, container, false)
    }

    internal inner class MenuFormAdapter(private var lista: ArrayList<MenuFM>) :
        RecyclerView.Adapter<MenuForm>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuForm {
            return MenuForm(
                LayoutInflater.from(parent.context).inflate(R.layout.ver_menuta, parent, false)
            )
        }

        override fun onBindViewHolder(holder: MenuForm, position: Int) {
            val opcion = lista[position]
            holder.b.text = opcion.textoBoton.toString()
            //Si está evaluado, poner chulito
            if (opcion.textoEval == "1") {
                holder.iv.setImageResource(R.drawable.check_opt)
            } else {
                holder.iv.setImageResource(R.drawable.uncheck)
            }
            holder.b.setOnClickListener { irOpcion(position) }
            holder.iv.setOnClickListener { irOpcion(position) }
        }

        override fun getItemCount(): Int {
            return lista.size
        }
    }

    override fun onStart() {
        super.onStart()
        canitems = 0
        val sinco = SincronizarOnline()
        sinco.sincronizarTablasVF(requireContext())
        // SE INICIALIZAN LAS FUNCIONES DE LOS HELPERS DE SQL
        
        val conGen = ConsultaGeneral()
        
        val fg = FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        try {
            cargarparametros()
        } finally {
            leerparametros()
        }

        //INICIALIZACION DE LISTA PARA LLENAR LOS GRUPOS
        lista = ArrayList()

        //SE CREA EL LINEAR LAYOUT QUE ALMACENARA LOS ITEMS Y SE ASIGNA A EL RECYCLER
        recyclerMenuForm = recycler_items_menu
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerMenuForm!!.layoutManager = llm

        //CARGAMOS INFORMACION DE ITEMS EJECUTADOS CONTEO DE ITEMS-GRUPOS EJECUTADOS VS LA CANTIDAD TOTAL DE ITEMS-GRUPOS
        val conteotot =
            fg.getQ1(requireContext(),
                "Select count(*) as cr from '202_PREGGRUP' where idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma'"
            )
        val conteoejec =
            fg.getQ1(requireContext(),
                "Select count(*) as cr from '202_PREGGRUP' where ev='1' and idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma'"
            )
        val conteos = "Ejecutado : $conteoejec de $conteotot"
        TV_Conteo.text = conteos


        val queryMenu = "SELECT grp, 0 as PF, ev, cdn,grpid FROM '202_PREGGRUP' where idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma' order by grpid asc"
        val objMenu =
            conGen.queryObjeto2val(
                context,
                queryMenu,
                null
            )
        if (objMenu != null) {
            grupo = arrayOfNulls(objMenu.size)
            ngrupo = arrayOfNulls(objMenu.size)
            evaluados = arrayOfNulls(objMenu.size)
            for (op in objMenu.indices) {
                val consultamostrar = objMenu[op]!![3]
                if (consultamostrar != "0") {
                    val mostrar = fg.getQ1(requireContext(),consultamostrar)
                    if (mostrar != "0" && (mostrar != "")) {
                        grupo[canitems] = objMenu[op]!![4]
                        ngrupo[canitems] = objMenu[op]!![0]
                        evaluados[canitems] = objMenu[op]!![2]
                        val model = MenuFM(ngrupo[canitems], evaluados[canitems])
                        lista?.add(model)
                        canitems++
                    }
                } else {
                    grupo[canitems] = objMenu[op]!![4]
                    ngrupo[canitems] = objMenu[op]!![0]
                    evaluados[canitems] = objMenu[op]!![2]

                    val imageView = ImageView(context)
                    if (evaluados[canitems] == "1") {
                        imageView.setImageResource(R.drawable.check_opt)
                    } else {
                        imageView.setImageResource(R.drawable.uncheck)
                    }
                    val model = MenuFM(ngrupo[canitems], evaluados[canitems])
                    lista?.add(model)
                    canitems++
                }
            }
            val ma = MenuFormAdapter(lista!!)
            recyclerMenuForm!!.adapter = ma
        }
        actualizarevotrasp()

        buttonIrMenu.setOnClickListener {
            finalizarclick()
        }
        buttonStanBy.setOnClickListener {
            standbyclick()
        }
    }

    private fun cargarparametros(){
        val fg = FuncionesGenerales()
        fg.act_param(requireContext(),"ULTIMAP", "98")
    }

    private fun leerparametros(){
        val fg = FuncionesGenerales()
        useruid = fg.parametro(requireContext(),"USERUID")
        panact = fg.parametro(requireContext(),"ULTIMAP")
        idtarea = fg.parametro(requireContext(),"TAREA_ACT")
        ntarea = fg.parametro(requireContext(),"NTAREA_ACT")
        nclienteact = fg.parametro(requireContext(),"NCLIENTE_ACT")
        horario = fg.parametro(requireContext(),"HORARIO_ACT")
        idformulario = fg.parametro(requireContext(),"FORMULARIO_ACT")
        nformulario = fg.parametro(requireContext(),"NFORMULARIO_ACT")
        idforma = fg.parametro(requireContext(),"FORMA_ACT")
        nforma = fg.parametro(requireContext(),"NFORMA_ACT")
        tipoform = fg.parametro(requireContext(),"TIPO_GRP")
        tituloact = fg.parametro(requireContext(),"TITULO_ACT")
        estado = fg.parametro(requireContext(),"ESTADO_ACT")
        opn = fg.parametro(requireContext(),"OPN_ACT")
        TVNPantalla.text = tituloact
    }

    private fun finalizarclick() {
        try {
            actualizarevotrasp()
        } finally {
            openfragment("97")
        }
    }

    private fun actualizarevotrasp() {
        val fg = FuncionesGenerales()
        val queryformartaF =
            "SELECT count(*) as cres FROM '202_PREGGRUP' where ev=0 and idtarea='$idtarea' and idform='$idforma'"
        val rtax = fg.getQ1(requireContext(),queryformartaF)
        val queryformartaT =
            "SELECT count(*) as cres FROM '202_PREGGRUP' where idtarea='$idtarea' and idform='$idforma'"
        val rtaformat = fg.getQ1(requireContext(),queryformartaT)
        val porc = (((rtaformat.toInt() - rtax.toInt()) * 100) / rtaformat.toInt())
        if (rtax == "0") {
            fg.ejecDB(requireContext(),"UPDATE '201_PREGFORM' SET ev=100 WHERE  idtarea='$idtarea' and idform='$idforma'")
        } else {
            fg.ejecDB(requireContext(),"UPDATE '201_PREGFORM' SET ev=$porc WHERE  idtarea='$idtarea' and idform='$idforma'")
        }
    }

    private fun standbyclick() {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupstandby(inflater)
    }

    private fun popupstandby(inflater: LayoutInflater) {
        val fg = FuncionesGenerales()
        val sinco = SincronizarOnline()
        sinco.sincronizarTablasVF(requireContext())
        val lts = LoadTaskSqlite()
        val popUp = inflater.inflate(R.layout.stanby_layout, null)
        val titulo = popUp.findViewById<View>(R.id.tvTitulo) as TextView
        val motivo = popUp.findViewById<View>(R.id.etObservation) as EditText
        titulo.setText("Ingrese el motivo para detener la ejecución de la tarea")
        val cancel = popUp.findViewById<View>(R.id.btnCancelarSB) as TextView
        val ok = popUp.findViewById<View>(R.id.btnIngresarST) as TextView
        val popupWindow = PopupWindow(popUp, LinearLayout.LayoutParams.MATCH_PARENT, 500, true)
        val latitud_ult = fg?.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg?.parametro(requireContext(), "ULT_LON")
        var latact = latitud_ult
        var lonact = longitud_ult
        cancel.setOnClickListener { popupWindow.dismiss() }
        ok.setOnClickListener {
            if (motivo.text.toString().length > 3) {
                try {
                    try{
                        fg.act_estadotareaandsync(requireContext(),idtarea,"Pendiente","0","1")
                    }finally {
                        lts.historynotes(requireContext(),motivo.text.toString())
                    }
                } finally {
                    try{
                        fg.ins_historial(requireContext(), motivo.text.toString(), latact, lonact)
                    }finally {
                        findNavController().navigate(R.id.navigation_tasks)
                        popupWindow.dismiss()
                    }
                }
            } else {
                Toast.makeText(requireContext(),
                    "Debe escribir una observacion para continuar",
                    Toast.LENGTH_LONG).show()
            }
        }
        popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
        popupWindow.showAtLocation(popUp, Gravity.CENTER, 0, 0)
    }

    fun irOpcion(pos: Int) {
        val fg = FuncionesGenerales()
        fg.act_param(requireContext(),"GRUPO_ACT", grupo[pos].toString())
        fg.act_param(requireContext(),"NGRUPO_ACT", ngrupo[pos].toString())
        fg.act_param(requireContext(),"TITULO_ACT", ngrupo[pos].toString())

        val querycrf = "SELECT count(*) as ctr FROM '203_PREGSGRUP' where idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "' and grpid='" + grupo[pos].toString() + "'"
        canregsgru = fg.getQ1(requireContext(),querycrf)
        if (canregsgru != "0") {
            openfragment("99")
        } else {
            fg.act_param(requireContext(),
                "PREGUNTA_ACT",
                fg.getQ1(requireContext(),"SELECT idpreg FROM '204_PREGGEN' where idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "' and grpid='" + grupo[pos].toString() + "'")

            )
            fg.act_param(requireContext(),
                "NPREGUNTA_ACT",
                fg.getQ1(requireContext(),"SELECT preg FROM '204_PREGGEN' where idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "' and grpid='" + grupo[pos].toString() + "'")

            )
            fg.act_param(requireContext(),
                "ORDEN_ACT",
                fg.getQ1(requireContext(),"SELECT orden FROM '204_PREGGEN' where idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + idforma + "' and grpid='" + grupo[pos].toString() + "'")

            )
            fg.act_param(requireContext(),"AP_CHECK","0")
            fg.act_param(requireContext(),"AP_TXT","0")
            fg.act_param(requireContext(),"AP_FOTO","0")
            fg.act_param(requireContext(),"AP_SELECT","0")
            fg.act_param(requireContext(),"TIPO_TXT","0")
            fg.act_param(requireContext(),"TIPO_FOTO","0")
            fg.act_param(requireContext(),"RTA1","0")
            fg.act_param(requireContext(),"TXT_VALOR","0")
            fg.act_param(requireContext(),"FTOM","0")
            fg.act_param(requireContext(),"RTA2","0")
            val querytipo =
                "SELECT tipo FROM '204_PREGGEN' where idtarea='$idtarea' and idformulario='$idformulario' and idform='$idforma' and grpid='${grupo[pos].toString()}'"
            openfragment(fg.getQ1(requireContext(),querytipo))
        }

    }

    private fun openfragment(strCase: String) {
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
        var instance: FormMenu? = null
    }
}