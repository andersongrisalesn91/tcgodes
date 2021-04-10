package com.example.tcgokotlin.Forms


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tcgokotlin.OnSignedCaptureListener
import com.example.tcgokotlin.R
import com.example.tcgokotlin.SignatureDialogFragment
import com.example.tcgokotlin.Sincronizar.CargarEstados
import com.example.tcgokotlin.Sincronizar.SincronizarOnline
import com.example.tcgokotlin.data.model.MenuFM
import com.example.tcgokotlin.data.model.MenuPorc
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.sqliteDBHelper.LoadTaskSqlite
import kotlinx.android.synthetic.main.activity_forma.*
import kotlinx.android.synthetic.main.stanby_layout.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList


class Forma : Fragment(), OnSignedCaptureListener {

    // Inicio Variables Globales de todas las pantallas
    private var useruid: String = ""
    private var panact: String = ""
    private var idtarea: String = ""
    private var ntarea: String = ""
    private var nclienteact: String = ""
    private var horario: String = ""
    private var idformulario: String = ""
    private var nformulario: String = ""
    private var tipoform: String = ""
    private var tituloact: String = ""
    private var tipocarga: String = ""
    private var ordenact: String = ""
    private var estado: String = ""
    private var obsgen: String = ""
    private var autoriza: String = ""
    private var tipofirma: String = ""
    private var firmacli: String = ""
    private var firmauser: String = ""
    private var nomcli: String = ""
    private var identcli: String = ""
    private var anexo1ap: String = ""
    private var anexo2ap: String = ""
    private var evanexomostrar: String = "0"
    private var evanexo1: String = ""
    private var evanexo2: String = ""

    // Fin private variables Globales de todas las Pantallas
    private var canreggru: String = ""
    private var firmatxt: String = ""
    private var recyclerMenuPorc: RecyclerView? = null
    private var lista: ArrayList<MenuFM>? = null
    private var canitems = 0
    private lateinit var grupo: Array<String?>
    private lateinit var ngrupo: Array<String?>
    private lateinit var evaluados: Array<String?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreate(savedInstanceState)
        if ((activity as AppCompatActivity).supportActionBar != null) {
            (activity as AppCompatActivity).supportActionBar?.hide()
        }
        return inflater.inflate(R.layout.activity_forma, container, false)
    }

    private fun showDialog() {
        val dialogFragment = SignatureDialogFragment(this)
        dialogFragment.show((activity as AppCompatActivity).supportFragmentManager, "signature")
    }

    override fun onSignatureCaptured(bitmap: Bitmap, fileUri: String) {
        // SE INICIAN ALGUNOS HELPERS SQL YA QUE PIERDEN SU VALOR AL SALIR DE LA PANTALLA
        val fg = FuncionesGenerales()

        //OBTENCION Y VERIFICACION DE RUTA DEL ARCHIVO - NOMBRE / CREACION DEL ARCHIVO
        val carpetaIMG = File(
            getExternalStorageDirectory().toString() + "/TCGO_FILES/",
            "IMG"
        )
        if (!carpetaIMG.exists()) {
            carpetaIMG.mkdirs()
        }

        val tareaact = fg.parametro(requireContext(), "TAREA_ACT")
        val fechact = fg.fechaActual(2)
        val rp = "_"
        val firma = "FIRMA"
        val nombrefoto = "$tareaact$rp$fechact$rp$firma$rp$tipofirma.jpg"

        val rotatedBitmap = bitmap.rotate(90f, bitmap)
        val mutableBitmap: Bitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true)
        if (tipofirma == "1") {
            val canvas = Canvas(mutableBitmap)
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 40f
            val mText =
                tv_NombreCliente.text.toString() + " - " + tv_Identificacion.text.toString()
            try {
                var i = 29
                for (line in mText.split("\n").toTypedArray()) {
                    i = i + 100
                }
                for (line in mText.split("\n").toTypedArray()) {
                    i = i - 100
                    canvas.drawText(line,
                        0,
                        line.toCharArray().size,
                        25f,
                        (mutableBitmap.height - i).toFloat(),
                        paint)
                }
            } catch (e: Exception) {
            }
        }
        val path = getExternalStorageDirectory()
            .toString() + "/TCGO_FILES/IMG/"

        val file = File(path, nombrefoto)
        val out = FileOutputStream(file)
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        val uriImage =
            Uri.parse(
                getExternalStorageDirectory()
                    .toString() + "/TCGO_FILES/IMG/$nombrefoto"
            )

        //CARGUE Y ACTUALIZACION DE PARAMETROS DE FIRMA

        if (tipofirma == "1") {
            firmatxt = "FIRMA_CLI"
            IVFirmaCliente.setImageURI(uriImage)
            firmacli = nombrefoto
        } else {
            firmatxt = "FIRMA_USER"
            IVFirmaUsuario.setImageURI(uriImage)
            firmauser = nombrefoto
        }
        fg.act_param(requireContext(), firmatxt, nombrefoto)

        // ACTUALIZACION DE REGISTRO EN BASE DE DATOS DE LA IMAGEN DE LA FIRMA
        val idfoton = nombrefoto.substringBefore(".")
        val queryFoto =
            "UPDATE '302_FOTOS_RESP_TEMP' SET idfoto='$idfoton' , nfoto='$nombrefoto' WHERE idtarea='$idtarea' and idpreg='$firmatxt';"
        fg.ejecDB(requireContext(), queryFoto)
    }

    fun Bitmap.rotate(degrees: Float, bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }


    internal inner class MenuPorcAdapter(private var lista: ArrayList<MenuFM>) :
        RecyclerView.Adapter<MenuPorc>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuPorc {
            return MenuPorc(
                LayoutInflater.from(parent.context).inflate(R.layout.ver_menu, parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MenuPorc, position: Int) {
            val opcion = lista[position]
            holder.b.text = opcion.textoBoton.toString()
            holder.iv.text = opcion.textoEval.toString() + " %"
            when (grupo[position].toString()) {
                "9991" -> {
                    holder.ti.text = "MENU -  ANEXO 1"
                    holder.ti.visibility = View.VISIBLE
                }
                "9992" -> {
                    holder.ti.text = "MENU -  ANEXO 1"
                    holder.ti.visibility = View.VISIBLE
                }
                else -> {
                    holder.ti.visibility = View.GONE
                }
            }

            val porcentaje = opcion.textoEval.toString()
            //Si está evaluado, poner chulito

            if (porcentaje.toInt() <= 50) {
                holder.iv.setBackgroundResource(R.drawable.btmenu_secundaryrd)
            } else if (porcentaje.toInt() <= 75) {
                holder.iv.setBackgroundResource(R.drawable.btmenu_secundaryamarillo)
            } else if (porcentaje.toInt() <= 100) {
                holder.iv.setBackgroundResource(R.drawable.btmenu_secundaryverde)
            }
            holder.b.setOnClickListener { irOpcion(position) }
        }

        override fun getItemCount(): Int {
            return lista.size
        }
    }

    override fun onStart() {
        super.onStart()

        canitems = 0

        // SE INICIALIZAN LAS FUNCIONES DE LOS HELPERS DE SQL

        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        //INICIALIZACION DE LISTA PARA LLENAR LOS GRUPOS
        lista = ArrayList()

        //SE CREA EL LINEAR LAYOUT QUE ALMACENARA LOS ITEMS Y SE ASIGNA A EL RECYCLER
        recyclerMenuPorc = recycler_items_menu
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerMenuPorc?.layoutManager = llm

        //SE CARGARN LOS PARAMETROS DE ESTE FORMULARIO SEGUN LA PANTALLA DE LA QUE VENGAN
        panact = fg.parametro(requireContext(), "ULTIMAP")
        tipoform = fg.parametro(requireContext(), "TIPO_GRP")
        fg.act_param(requireContext(),
            "TITULO_ACT",
            fg.getQ1(requireContext(),
                "Select formulario from '108_FORMULARIOS' where idtarea='$idtarea'"
            )
        )
        try {
            cargarparametros()
        } finally {
            leerparametros()
        }


        //SE CARGA LA INFORMACION DEL ENCABEZADO
        val querydatos =
            "SELECT idclient, nomclient, tel1, tel2, prioridad, horario,estimado FROM '200_TAREAS' where idtarea='$idtarea'"
        val objDatos =
            conGen.queryObjeto2val(requireContext(), querydatos, null) as Array<ArrayList<String>>
        val descripcion = fg.getQ1(requireContext(),
            "select descripcion from '108_FORMULARIOS' where idtarea='$idtarea'")
        val consecutivo = fg.getQ1(requireContext(),
            "select conse from '108_FORMULARIOS' where idtarea='$idtarea'")
        val totalfotos = fg.getQ1(requireContext(),
            "Select count(*) from '302_FOTOS_RESP_TEMP' where idtarea='$idtarea' and idpreg<>'FIRMA_CLI' and idpreg<>'FIRMA_USER' and idform<>'9991' and idform<>'9992'") + " / " + fg.parametro(
            requireContext(),
            "FOTOSCLI")
        val tiposervicio = fg.typeservice(requireContext())

        val txttitulo = nformulario + " - " + "CONSEC: " + consecutivo
        if (objDatos.isNotEmpty()) {
            val idcliente = "Identificación: " + objDatos[0][0].toString()
            val nomcliente = objDatos[0][1].toString().toUpperCase(Locale.ROOT)
            val telefonos =
                "Teléfonos: " + objDatos[0][2].toString() + " - " + objDatos[0][3].toString()
            val Descripcion = "Descripción: $descripcion"
            val prioridad = objDatos[0][4].toString()
            val horariotarea = "Horario: " + objDatos[0][5].toString()
            val conteofotos = "N. Fotos: " + totalfotos
            val estimado = "Estimado: " + objDatos[0][6].toString()
            TVNPantalla1.text = Descripcion
            TVNPantalla2.text = idcliente
            TVNPantalla3.text = horariotarea
            TVNPantalla4.text = telefonos
            TVNPantalla5.text = nomcliente
            TVNPantalla6.text = tiposervicio
            TVNPantalla7.text = conteofotos
            TVNPantalla8.text = estimado
            if (prioridad == "VIP") {
                TVNPantalla9.visibility = View.VISIBLE
            } else {
                TVNPantalla9.visibility = View.GONE
            }

            TVNPantalla.text = txttitulo
        }

        //SE CARGA LA LISTA DE FORMAS CREADAS DEFINIDAS EN EL APLICATIVO LEYENDO LA INFORMACION ALMACENADA EN LA TABLA DE FORMAS
        // OMITIENDO EL FORM 9999 QUE SON LAS VARIABLES DE  ENCABEZADO
        cargarFormas()

        //SE CARGAN LOS LISTENERS DE LOS BOTONES Y VARIABLES
        bt_showhideanexos.setOnClickListener {
            mostraranexo()
        }
        bt_Anexo1.setOnClickListener {
            iranexo("1")
        }
        bt_Anexo2.setOnClickListener {
            iranexo("2")
        }
        tvAutorizaSI.setOnClickListener {
            actualizaauth("1")
        }
        tvAutorizaNO.setOnClickListener {
            actualizaauth("2")
        }
        IVFirmaCliente.setOnClickListener {
            tipofirma = "1"
            fg.act_param(requireContext(), "TIPO_FIRMA", "1")
            showDialog()
        }
        IVFirmaUsuario.setOnClickListener {
            tipofirma = "2"
            fg.act_param(requireContext(), "TIPO_FIRMA", "2")
            showDialog()
        }
        buttonIrMenu.setOnClickListener {
            finalizarclick()
        }
        buttonStanBy.setOnClickListener {
            standbyclick()
        }
    }

    private fun mostraranexo() {
        if (evanexomostrar == "0") {
            bt_showhideanexos.setText("OCULTAR")
            evanexomostrar = "1"
            CL_ItemsAnexos.visibility = View.VISIBLE
            if (anexo1ap != "0") {
                tv_titulo_Anexo1.visibility = View.VISIBLE
                marginstart.visibility = View.VISIBLE
                bt_Anexo1.visibility = View.VISIBLE
                if (evanexo1 != "0") {
                    bt_Anexo1.setBackgroundResource(R.drawable.anexo_blue)
                } else {
                    bt_Anexo1.setBackgroundResource(R.drawable.anexo_black)
                }
            } else {
                tv_titulo_Anexo1.visibility = View.GONE
                marginstart.visibility = View.GONE
                bt_Anexo1.visibility = View.GONE
            }
            if (anexo2ap != "0") {
                tv_titulo_Anexo2.visibility = View.VISIBLE
                marginend.visibility = View.VISIBLE
                bt_Anexo2.visibility = View.VISIBLE
                if (evanexo2 != "0") {
                    bt_Anexo2.setBackgroundResource(R.drawable.anexo_blue)
                } else {
                    bt_Anexo2.setBackgroundResource(R.drawable.anexo_black)
                }
            } else {
                tv_titulo_Anexo2.visibility = View.GONE
                marginend.visibility = View.GONE
                bt_Anexo2.visibility = View.GONE
            }
        } else {
            evanexomostrar = "0"
            bt_showhideanexos.setText("MOSTRAR")
            CL_ItemsAnexos.visibility = View.GONE
        }
    }

    private fun iranexo(resp: String) {

        val fg = FuncionesGenerales()
        guardarpregenc()

        val anexsel = if (resp == "1") {
            "9991"
        } else {
            "9992"
        }
        val tituloanex = fg.getQ1(requireContext(),
            "select form from '201_PREGFORM' where idtarea='$idtarea' and idform='$anexsel'")

        fg.act_param(requireContext(), "FORMA_ACT", anexsel)
        fg.act_param(requireContext(), "NFORMA_ACT", tituloanex)
        fg.act_param(requireContext(), "TITULO_ACT", tituloanex)

        fg.act_param(requireContext(),
            "PREGUNTA_ACT",
            fg.getQ1(requireContext(),
                "SELECT idpreg FROM '204_PREGGEN' where idtarea='$idtarea' and idformulario='$idformulario' and idform='$anexsel'"
            )
        )
        val querytipo =
            "SELECT tipo FROM '204_PREGGEN' where idtarea='$idtarea' and idformulario='$idformulario' and idform='$anexsel'"
        val tpreg = fg.getQ1(requireContext(), querytipo)
        openFragment(tpreg)

    }

    private fun cargarFormas() {
        val conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val queryMenu =
            "SELECT form, 0 as PF, ev, cdn,idform FROM '201_PREGFORM' where idtarea='$idtarea' and idformulario='$idformulario' and (idform<>'9999' and idform<>'9991' and idform<>'9992') order by idform asc"
        val objMenu =
            conGen.queryObjeto2val(requireContext(), queryMenu, null)
        if (objMenu.isNotEmpty()) {
            ngrupo = arrayOfNulls(objMenu.size)
            grupo = arrayOfNulls(objMenu.size)
            evaluados = arrayOfNulls(objMenu.size)
            for (op in objMenu.indices) {
                val consultamostrar = objMenu[op][3]
                if (consultamostrar != "0") {
                    val mostrar = fg.getQ1(requireContext(), consultamostrar)
                    if (mostrar != "0" && (mostrar != "")) {
                        grupo[canitems] = objMenu[op][4]
                        ngrupo[canitems] = objMenu[op][0]
                        evaluados[canitems] = objMenu[op][2]
                        val model = MenuFM(ngrupo[canitems], evaluados[canitems])
                        lista?.add(model)
                        canitems++
                    }
                } else {
                    grupo[canitems] = objMenu[op][4]
                    ngrupo[canitems] = objMenu[op][0]
                    evaluados[canitems] = objMenu[op][2]
                    val model = MenuFM(ngrupo[canitems], evaluados[canitems])
                    lista?.add(model)
                    canitems++
                }
            }

            val ma = MenuPorcAdapter(lista!!)
            recyclerMenuPorc?.adapter = ma
        }
    }

    private fun actualizaauth(resp: String) {
        val fg = FuncionesGenerales()
        fg.ejecDB(requireContext(),
            "UPDATE '301_RESPUESTAS_TEMP' SET rta1='$resp' where idtarea='$idtarea' and idpreg='GEN_2'"
        )
        fg.act_param(requireContext(), "AUTORIZA", resp)
        if (resp == "1") {
            tvAutorizaNO.setBackgroundResource(R.drawable.btmenu_secundaryrd)
            tvAutorizaSI.setBackgroundResource(R.drawable.btmenu_primaryri)
        }
        if (resp == "2") {
            tvAutorizaSI.setBackgroundResource(R.drawable.btmenu_secundaryri)
            tvAutorizaNO.setBackgroundResource(R.drawable.btmenu_primaryrd)
        }
    }

    // SE CARGAN LA INFORMACION DE LA PANTALLA EN LOS PARAMETROS DE LA BASE DE DATOS
    private fun cargarparametros() {
        val fg = FuncionesGenerales()
        idtarea = fg.parametro(requireContext(), "TAREA_ACT")
        anexo1ap = fg.getQ1(requireContext(),
            "select ifnull(count(*),0) as apanex1 from '201_PREGFORM' where idtarea='$idtarea' and idform='9991'")
        anexo2ap = fg.getQ1(requireContext(),
            "select ifnull(count(*),0) as apanex2 from '201_PREGFORM' where idtarea='$idtarea' and idform='9992'")

        fg.act_param(requireContext(), "AP_ANEXO1", anexo1ap)
        fg.act_param(requireContext(), "AP_ANEXO2", anexo2ap)

        fg.act_param(requireContext(),
            "FORMULARIO_ACT",
            fg.getQ1(requireContext(),
                "Select idformulario from '108_FORMULARIOS' where idtarea='$idtarea'"
            )
        )
        fg.act_param(requireContext(),
            "NFORMULARIO_ACT",
            fg.getQ1(requireContext(),
                "Select formulario from '108_FORMULARIOS' where idtarea='$idtarea'"
            )
        )
        fg.act_param(requireContext(),
            "OBS_GEN",
            fg.getQ1(requireContext(),
                "Select obs from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='GEN_1'"
            )
        )
        fg.act_param(requireContext(),
            "AUTORIZA",
            fg.getQ1(requireContext(),
                "Select rta1 from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='GEN_2'"
            )
        )
        fg.act_param(requireContext(),
            "NOM_CLI",
            fg.getQ1(requireContext(),
                "Select obs from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='GEN_3'"
            )
        )
        fg.act_param(requireContext(),
            "IDENT_CLI",
            fg.getQ1(requireContext(),
                "Select obs from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idpreg='GEN_4'"
            )
        )

        fg.act_param(requireContext(),
            "FIRMA_CLI",
            fg.getQ1(requireContext(),
                "Select nfoto from '302_FOTOS_RESP_TEMP' where idtarea='$idtarea' and idpreg='FIRMA_CLI'"
            )
        )

        fg.act_param(requireContext(),
            "FIRMA_USER",
            fg.getQ1(requireContext(),
                "Select nfoto from '302_FOTOS_RESP_TEMP' where idtarea='$idtarea' and idpreg='FIRMA_USER'"
            )
        )
    }

    // SE lEE LA BASE DE PARAMETROS Y SE INGRESA LA INFORMACION EN LAS VARIABLES GLOBALES DE LA VISTA
    private fun leerparametros() {
        val fg = FuncionesGenerales()
        fg.act_param(requireContext(), "ULTIMAP", "97")
        useruid = fg.parametro(requireContext(), "USERUID")
        idtarea = fg.parametro(requireContext(), "TAREA_ACT")
        ntarea = fg.parametro(requireContext(), "NTAREA_ACT")
        nclienteact = fg.parametro(requireContext(), "NCLIENTE_ACT")
        horario = fg.parametro(requireContext(), "HORARIO_ACT")
        idformulario = fg.parametro(requireContext(), "FORMULARIO_ACT")
        nformulario = fg.parametro(requireContext(), "NFORMULARIO_ACT")
        tituloact = fg.parametro(requireContext(), "TITULO_ACT")
        tipocarga = fg.parametro(requireContext(), "TIPOCARGA")
        ordenact = fg.parametro(requireContext(), "ORDEN_ACT")
        estado = fg.parametro(requireContext(), "ESTADO_ACT")
        obsgen = fg.parametro(requireContext(), "OBS_GEN")
        autoriza = fg.parametro(requireContext(), "AUTORIZA")
        tipofirma = fg.parametro(requireContext(), "TIPO_FIRMA")
        firmacli = fg.parametro(requireContext(), "FIRMA_CLI")
        firmauser = fg.parametro(requireContext(), "FIRMA_USER")
        nomcli = fg.parametro(requireContext(), "NOM_CLI")
        identcli = fg.parametro(requireContext(), "IDENT_CLI")
        anexo1ap = fg.parametro(requireContext(), "AP_ANEXO1")
        anexo2ap = fg.parametro(requireContext(), "AP_ANEXO2")

        rellenarGenerales()
    }

    // SE CARGA LA VISUALIZACION DE LAS VARIABLES GENERALES CON LA INFORMACION DE LOS PARAMETROS EN CASO DE QUE YA SE HAYAN LLENADO PREVIAMENTE
    private fun rellenarGenerales() {
        val fg = FuncionesGenerales()
        if (anexo1ap != "0" || anexo2ap != "0") {
            CL_Anexos.visibility = View.VISIBLE
            evanexo1 = fg.getQ1(requireContext(),
                "select ifnull(count(*),0) as cra1 from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idform='9991'")
            fg.act_param(requireContext(), "EV_ANEXO1", evanexo1)
            evanexo2 = fg.getQ1(requireContext(),
                "select ifnull(count(*),0) as cra2 from '301_RESPUESTAS_TEMP' where idtarea='$idtarea' and idform='9992'")
            fg.act_param(requireContext(), "EV_ANEXO2", evanexo2)

            if (anexo1ap != "0") {
                val tituloanex1 = fg.getQ1(requireContext(),
                    "select form from '201_PREGFORM' where idtarea='$idtarea' and idform='9991'")
                val titanex1 = tituloanex1.toString() + " : " + evanexo1
                tv_titulo_Anexo1.text = titanex1
            }
            if (anexo2ap != "0") {
                val tituloanex2 = fg.getQ1(requireContext(),
                    "select form from '201_PREGFORM' where idtarea='$idtarea' and idform='9992'")
                val titanex2 = tituloanex2.toString() + " : " + evanexo2
                tv_titulo_Anexo2.text = titanex2
            }
            CL_ShowAnexos.visibility = View.VISIBLE
            if (evanexo1 != "0" || evanexo2 != "0") {
                evanexomostrar = "1"
                bt_showhideanexos.setText("OCULTAR")
                CL_ItemsAnexos.visibility = View.VISIBLE
                tv_titulo_Anexo1.visibility = View.VISIBLE
                marginstart.visibility = View.VISIBLE
                bt_Anexo1.visibility = View.VISIBLE
                tv_titulo_Anexo2.visibility = View.VISIBLE
                marginend.visibility = View.VISIBLE
                bt_Anexo2.visibility = View.VISIBLE
                if (evanexo1 != "0") {
                    bt_Anexo1.setBackgroundResource(R.drawable.anexo_blue)
                } else {
                    bt_Anexo1.setBackgroundResource(R.drawable.anexo_black)
                }
                if (evanexo2 != "0") {
                    bt_Anexo2.setBackgroundResource(R.drawable.anexo_blue)
                } else {
                    bt_Anexo2.setBackgroundResource(R.drawable.anexo_black)
                }
            } else {
                evanexomostrar = "0"
                CL_ItemsAnexos.visibility = View.GONE
                tv_titulo_Anexo1.visibility = View.GONE
                marginstart.visibility = View.GONE
                bt_Anexo1.visibility = View.GONE
                tv_titulo_Anexo2.visibility = View.GONE
                marginend.visibility = View.GONE
                bt_Anexo2.visibility = View.GONE
            }
        } else {
            CL_Anexos.visibility = View.GONE
        }

        if (obsgen != "0") {
            tv_Observacion.setText(obsgen)
        }
        if (nomcli != "0") {
            tv_NombreCliente.setText(obsgen)
        }
        if (identcli != "0") {
            tv_Identificacion.setText(obsgen)
        }
        actualizaauth(autoriza)

        if (firmacli != "0" && !firmacli.equals(null) && firmacli != "") {
            val uriImage =
                Uri.parse(
                    getExternalStorageDirectory()
                        .toString() + "/TCGO_FILES/IMG/" + firmacli
                )
            IVFirmaCliente?.setImageURI(uriImage)
        }
        if (firmauser != "0" && !firmauser.equals(null) && firmauser != "") {
            val uriImage =
                Uri.parse(
                    getExternalStorageDirectory()
                        .toString() + "/TCGO_FILES/IMG/" + firmauser
                )
            IVFirmaUsuario?.setImageURI(uriImage)
        }
    }

    // SE EVALUA QUE SE HAYA COMPLETADO EN SU TOTALIDAD LAS FORMAS Y VARIABLES GENERALES
    // SI SUCEDE SE ALMACENA LA INFORMACION DE TEMPORAL A PERMANENTE
    private fun finalizarclick() {
        val fg = FuncionesGenerales()
        val sinco = SincronizarOnline()
        val lts = LoadTaskSqlite()
        guardarpregenc()
        val observacionesgen = tv_Observacion.text.toString()
        val queryformartaF =
            "SELECT count(*) as cres FROM '201_PREGFORM' where idtarea='$idtarea' and ev=0 and idform<>'9999' and idform<>'9991' and idform<>'9992'"
        val rtaFormaF = fg.getQ1(requireContext(), queryformartaF)
        val valautoriza = fg.parametro(requireContext(), "AUTORIZA")
        val valfirmauser = fg.parametro(requireContext(), "FIRMA_USER")
        val cliw = fg.parametro(requireContext(), "IDCLIENTW")
        val fechaac = fg.fechaActual(1)
        val latitud_ult = fg?.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg?.parametro(requireContext(), "ULT_LON")
        var latact = latitud_ult
        var lonact = longitud_ult
        val rtag = if (valautoriza != "0" && valfirmauser != "0") {
            "1"
        } else {
            "0"
        }
        val nota = "Hora: " + fg.fechaActual(4) + " Usuario: " + fg.useruid(requireContext()) +  " Nota: " + observacionesgen
        if (rtaFormaF == "0" && rtag == "1") {
            try {
                fg.ejecDB(requireContext(),
                    "INSERT OR IGNORE INTO '301_RESPUESTAS' (idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,opt,rta1,rta2,obs,gpslat,gpslon,useruid) " +
                            "select idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,opt,rta1,rta2,obs,gpslat,gpslon,useruid from '301_RESPUESTAS_TEMP' WHERE idtarea='" + idtarea + "' order by 1,2,3,4,5,6,7 ASC;"
                )
                fg.ejecDB(requireContext(),"update '301_RESPUESTAS' set idclientw='$cliw' , fecha='$fechaac'")
                fg.ejecDB(requireContext(),
                "DELETE FROM '301_RESPUESTAS_TEMP' WHERE idtarea='$idtarea';"
                )
            } finally {
                try {
                    fg.ejecDB(requireContext(),
                        "INSERT OR IGNORE INTO '302_FOTOS_RESP' (idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid) " +
                                "select idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid from '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' order by 1,2,3,4,5,6 ASC;"
                    )
                    fg.ejecDB(requireContext(),"update '302_FOTOS_RESP' set idclientw='$cliw' , fecha='$fechaac'")
                    fg.ejecDB(requireContext(),
                        "DELETE FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='$idtarea';"
                    )
                } finally {
                    try{
                        try{
                            fg.act_estadotareaandsync(requireContext(),idtarea,"Finalizado","0","0")
                        }finally {
                            sinco.sincronizarTablasVF(requireContext())
                        }
                    }finally {
                        try{
                            lts.historynotes(requireContext(),nota)
                        }finally {
                            try {
                                fg.ins_historial(requireContext(), "0", latact, lonact)
                            } finally {
                                openFragment("101")
                            }
                        }
                    }
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "No puede finalizar hasta que responda todo el formulario",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun standbyclick() {
        guardarpregenc()
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

    // SE ALMACENAN EN BASE DE DATOS - TABLA DE PREGUNTAS TEMPORALES LOS VALORES DE TEXTO CORRESPONDIENTES A LAS VARIABLES GENERALES
    // (OBSERVACIONES - NOMBRE E IDENTIFICACION DEL CLIENTE)
    private fun guardarpregenc() {
        val fg = FuncionesGenerales()
        val observacionesgen = tv_Observacion.text.toString()
        val nombrecliente = tv_NombreCliente.text.toString()
        val identificacioncliente = tv_Identificacion.text.toString()

        fg.ejecDB(requireContext(),
            "UPDATE '301_RESPUESTAS_TEMP' SET obs='$observacionesgen' where idtarea='$idtarea' and idpreg='GEN_1'"
        )
        fg.ejecDB(requireContext(),
            "UPDATE '301_RESPUESTAS_TEMP' SET obs='$nombrecliente' where idtarea='$idtarea' and idpreg='GEN_3'"
        )
        fg.ejecDB(requireContext(),
            "UPDATE '301_RESPUESTAS_TEMP' SET obs='$identificacioncliente' where idtarea='$idtarea' and idpreg='GEN_4'"
        )

        obsgen = observacionesgen
        fg.act_param(requireContext(), "OBS_GEN", obsgen)
        nomcli = nombrecliente
        fg.act_param(requireContext(), "NOM_CLI", nomcli)
        identcli = identificacioncliente
        fg.act_param(requireContext(), "IDENT_CLI", identcli)
    }

    private fun irOpcion(pos: Int) {
        val fg = FuncionesGenerales()
        guardarpregenc()

        fg.act_param(requireContext(), "FORMA_ACT", grupo[pos].toString())
        fg.act_param(requireContext(), "NFORMA_ACT", ngrupo[pos].toString())
        fg.act_param(requireContext(), "TITULO_ACT", ngrupo[pos].toString())
        if (tipoform == "0" || (grupo[pos].toString() == "9991" || grupo[pos].toString() == "9992")) {
            fg.act_param(requireContext(),
                "PREGUNTA_ACT",
                fg.getQ1(requireContext(),
                    "SELECT idpreg FROM '204_PREGGEN' where idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + grupo[pos].toString() + "'")
            )
            fg.act_param(requireContext(), "ORDEN_ACT", grupo[pos].toString())
        }
        val querycrf =
            "SELECT count(*) as ctr FROM '202_PREGGRUP' where idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + grupo[pos].toString() + "'"
        canreggru = fg.getQ1(requireContext(), querycrf)

        if (canreggru != "0") {
            openFragment("98")
        } else {
            val querytipo =
                "SELECT tipo FROM '204_PREGGEN' where idtarea='" + idtarea + "' and idformulario='" + idformulario + "' and idform='" + grupo[pos].toString() + "'"
            val tpreg = fg.getQ1(requireContext(), querytipo)
            openFragment(tpreg)
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
        var instance: Forma? = null
    }
}