package com.example.tcgokotlin.Forms

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tcgokotlin.R
import com.example.tcgokotlin.Sincronizar.CargarEstados
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import kotlinx.android.synthetic.main.activity_form_tipo8.*
import java.io.*
import java.util.*


class FormFotos : Fragment() {
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
    private var tipo_form: String = ""
    private var foto_tom: String = ""
    private var cantfotosanexo: String = ""
    private var idpregact: String = ""
    private var opnact: String = ""
    private var useruid: String = ""
    private var fotostotal: String = ""
    private var maximoFotoscli: String = ""
    private var panact: String = ""
    private var path: String = ""
    private val carpetaIMG: String = ""
    private var nombreFoto: String = ""
    private var tipoFoto: String = ""
    private var textoFoto: String = ""
    private var iv: ImageView? = null
    private var conGen: ConsultaGeneral? = null
    private var recycler: RecyclerView? = null
    private var lista: ArrayList<RutaModel>? = null
    private lateinit var FotoID: Array<String?>
    private lateinit var FotoNOM: Array<String?>



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.activity_form_tipo8, container, false)
    }

    internal inner class Ruta(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvRuta: TextView
        var ivEliminar: ImageView

        init {
            tvRuta = itemView.findViewById<View>(R.id.tvRutaFoto) as TextView
            ivEliminar = itemView.findViewById<View>(R.id.ivEliminarFoto) as ImageView
        }
    }

    inner class RutaModel(var ruta: String)
    internal inner class RutaAdpater(var lista: ArrayList<RutaModel>) :
        RecyclerView.Adapter<Ruta>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Ruta {
            return Ruta(
                LayoutInflater.from(parent.context).inflate(R.layout.ver_ruta_foto, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Ruta, position: Int) {
            val rm = lista[position]
            holder.tvRuta.text = rm.ruta
            holder.tvRuta.setOnClickListener { mostrarfoto(position) }
            holder.ivEliminar.setOnClickListener { eliminarfoto(position) }

        }

        override fun getItemCount(): Int {
            return lista.size
        }
    }

    override fun onStart() {
        super.onStart()
        conGen = ConsultaGeneral()
        val fg = FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        iv = imageViewFoto
        idtarea = fg.parametro(requireContext(), "TAREA_ACT")
        ntarea = fg.parametro(requireContext(), "NTAREA_ACT")
        idformulario = fg.parametro(requireContext(), "FORMULARIO_ACT")
        nformulario = fg.parametro(requireContext(), "NFORMULARIO_ACT")
        idforma = fg.parametro(requireContext(), "FORMA_ACT")
        nforma = fg.parametro(requireContext(), "NFORMA_ACT")
        idgrupo = fg.parametro(requireContext(), "GRUPO_ACT")
        ngrupo = fg.parametro(requireContext(), "NGRUPO_ACT")
        idsgrupo = fg.parametro(requireContext(), "SUBGRUPO_ACT")
        nsgrupo = fg.parametro(requireContext(), "NSUBGRUPO_ACT")
        tipo_form = fg.parametro(requireContext(), "TIPO_GRP")
        panact = fg.parametro(requireContext(), "ULTIMAP")
        idpregact = fg.parametro(requireContext(), "PREGUNTA_ACT")
        opnact = fg.parametro(requireContext(), "OPN_ACT")
        useruid = fg.parametro(requireContext(), "USERUID")
        titulopant = fg.parametro(requireContext(), "TITULO_ACT")
        foto_tom = fg.parametro(requireContext(), "FTOM")
        var fotoact = fg.parametro(requireContext(), "IDFOTO_ACT")
        tipoFoto = fg.parametro(requireContext(), "TIPO_FOTO")
        cantfotosanexo = fg.getQ1(requireContext(),"SELECT count(*) FROM '302_FOTOS_RESP_TEMP' WHERE " +
                "idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + opnact + " and idform='" + idforma + "'")
        fotostotal = fg.getQ1(requireContext(),
            "Select count(*) from '302_FOTOS_RESP_TEMP' where idtarea='$idtarea' and idpreg<>'FIRMA_CLI' and idpreg<>'FIRMA_USER'")
        if (!fotoact.equals("0")) {
            try{
                val pathfa = Environment.getExternalStorageDirectory()
                    .toString() + "/TCGO_FILES/IMG/" + fotoact.toString()
                val bitmap = BitmapFactory.decodeFile(pathfa)
                iv?.setImageBitmap(bitmap)
            }catch (e: Exception){
                Log.i("No se puede mostrar la imagen:", e.toString())
                Toast.makeText(requireContext(),"No se puede mostrar la imagen, intentelo manualmente",Toast.LENGTH_LONG).show()
            }

        }

        recycler = recyclerViewFoto
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        recycler?.layoutManager = llm
        lista = ArrayList()

        TVNPantalla.setText(titulopant)
        maximoFotoscli = fg.parametro(requireContext(), "FOTOSCLI").toString()
        //Llenar el recycler si hay fotos tomadas, si no desplegar la toma de fotos
        if ((fotostotal.toInt() >= maximoFotoscli.toInt()) || (cantfotosanexo=="4" && (idforma=="9991" || idforma=="9991"))) {
            Toast.makeText(requireContext(),
                "Alcanzo el maximo de fotos permitidas, para tomar mas elimine una anterior o solicite que su compañia contacte con el administrador para solicitar capacidad adicional",
                Toast.LENGTH_LONG).show()
        } else {
            if (foto_tom.equals("1")) {
                listarfotos()
            } else {
                if (tipoFoto == "3" || tipoFoto == "5"){
                    val inflater =
                        requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    popuptxtfoto(inflater)
                }else {
                    tomarFoto()
                }
            }
        }


        BTAtras.visibility = View.GONE
        BTSiguiente.visibility = View.GONE

        BTRegresar.setOnClickListener {
            desplazamientos()
        }
        ivTomarFoto.setOnClickListener {
            if ((fotostotal.toInt() >= maximoFotoscli.toInt()) || (cantfotosanexo=="4" && (idforma=="9991" || idforma=="9991"))) {
                Toast.makeText(requireContext(),
                    "Alcanzo el maximo de fotos permitidas, para tomar mas elimine una anterior o solicite que su compañia contacte con el administrador para solicitar capacidad adicional",
                    Toast.LENGTH_LONG).show()
            } else {
                if (tipoFoto == "3" || tipoFoto == "5"){
                    val inflater =
                        requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    popuptxtfoto(inflater)
                }else {
                    tomarFoto()
                }
            }
        }
    }

    private fun popuptxtfoto(inflater: LayoutInflater) {
        val fg = FuncionesGenerales()
        val popUp = inflater.inflate(R.layout.txtfoto_layout, null)
        val titulo = popUp.findViewById<View>(R.id.tvTitulo) as TextView
        val motivo = popUp.findViewById<View>(R.id.etObservation) as EditText
        titulo.setText("Ingrese el texto de la para incluir en la imagen")
        val cancel = popUp.findViewById<View>(R.id.btnCancelarSB) as TextView
        val ok = popUp.findViewById<View>(R.id.btnIngresarST) as TextView
        val popupWindow = PopupWindow(popUp, LinearLayout.LayoutParams.MATCH_PARENT, 500, true)
        cancel.setOnClickListener { popupWindow.dismiss() }
        ok.setOnClickListener {
            if (motivo.text.toString().length >3 && motivo.text.toString().length <251){
                textoFoto = motivo.text.toString()
                tomarFoto()
                popupWindow.dismiss()
            }else{
                Toast.makeText(requireContext(),
                    "Debe escribir un texto para continuar, con una longitud de entre 4 y 250 caracteres",
                    Toast.LENGTH_LONG).show()
            }
        }
        popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
        popupWindow.showAtLocation(popUp, Gravity.CENTER, 0, 0)
    }

    @Throws(Exception::class)
    fun tomarFoto() {
        val two: Thread = object : Thread() {
            override fun run() {
                val fg = FuncionesGenerales()
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
                    fg.parametro(requireContext(), "TAREA_ACT")
                        .toString() + "_" + fg.fechaActual(2) + "_" + fg.parametro(requireContext(),
                        "PREGUNTA_ACT")
                        .toString() + "_" + fg.parametro(requireContext(), "OPN_ACT")
                        .toString() + ".jpg"

                path = Environment.getExternalStorageDirectory()
                    .toString() + "/TCGO_FILES/IMG/" + nombreFoto
                val imagen = File(carpetaIMG, nombreFoto)
                val uri = Uri.fromFile(imagen)
                camara.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(camara, TAKE_PICTURE)
            }
        }
        two.start()
        two.join()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {

        val fg = FuncionesGenerales()
        fg.act_param(requireContext(), "FTOM", "1")
            val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
            val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
            var latact = latitud_ult
            var lonact = longitud_ult
            fg.act_param(requireContext(), "IDFOTO_ACT",nombreFoto)
            try{
                if (tipoFoto == "3" || tipoFoto == "4" || tipoFoto == "5"){
                    val pathnew = Environment.getExternalStorageDirectory()
                        .toString() + "/TCGO_FILES/IMG/"

                    val bitmap = BitmapFactory.decodeFile(path)
                    val mutableBitmap: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    bitmap.recycle()

                    val canvas = Canvas(mutableBitmap)
                    canvas.rotate(90f)
                    val paint = Paint()
                    paint.color = Color.WHITE
                    paint.textSize = 20f
                    paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY)
                    var mText: String = fg.fechaActual(3)  + "\n"
                    var i = 25

                    when (tipoFoto) {
                        "3" -> mText = """$mText${textoFoto}""".trimIndent()
                        "4" -> mText = """$mText${latact},${lonact}""".trimIndent()
                        "5" -> mText = """$mText${latact},${lonact}${textoFoto}""".trimIndent()
                    }
                    for (line in mText.split("\n").toTypedArray()) {
                        i = i + 50
                    }
                    for (line in mText.split("\n").toTypedArray()) {
                        i = i - 50
                        canvas.drawText(line,
                            0,
                            line.toCharArray().size,
                            25f,
                            (mutableBitmap.height - i).toFloat(),
                            paint)
                    }

                    val archivo = File(pathnew, nombreFoto)

                    try {
                        val os: OutputStream = BufferedOutputStream(FileOutputStream(archivo))
                        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }catch (e:Exception) {
                Toast.makeText(requireContext(),"Ocurrio un problema al tomar la foto , verifique si se tomo la foto, de lo contrario tomela nuevamente",Toast.LENGTH_LONG).show()
            }


        fg.ejecDB(requireContext(),
            "UPDATE '204_PREGGEN' SET evalfoto='1' WHERE idtarea='" + idtarea + "' and idpreg='$idpregact'")

        val queryFoto =
            "INSERT INTO '302_FOTOS_RESP_TEMP' (idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid) VALUES('" +
                    idtarea + "' , '" + idformulario + "' , '" + idforma + "' , '" + idgrupo + "' , '" + idsgrupo + "' , '" + idpregact + "' , '" +
                    opnact + "' , '" + nombreFoto.substringBefore(".") + "' , '" + nombreFoto + "' , '" + latact + "' , '" + lonact + "' , '" + useruid + "');"
        fg.ejecDB(requireContext(), queryFoto)
            openFragment("100")
        } else {
            openFragment("100")
        }
    }

    fun Bitmap.rotate(degrees: Float, bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    private fun eliminarfoto(pos: Int) {
        val fg = FuncionesGenerales()
        fg.ejecDB(requireContext(),
            "DELETE FROM '302_FOTOS_RESP_TEMP' WHERE idfoto='" + FotoID[pos]?.toString() + "'"
        )
        fg.BorrarFoto(FotoID[pos].toString())

        val querypreguntasrtaF =
            "SELECT count(*) as cres FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='" + idpregact + "'"
        val rtapreguntasF = fg.getQ1(requireContext(), querypreguntasrtaF)
        val rtax = rtapreguntasF.toString()
        val trta = if (rtax.toInt() > 0) {
            1
        } else {
            0
        }
        fg.act_param(requireContext(), "FTOM", trta.toString())

        fg.ejecDB(requireContext(),
            "UPDATE '204_PREGGEN' SET evalfoto='" + trta + "' WHERE idtarea='" + idtarea + "' and idpreg='$idpregact'")

        listarfotos()
    }

    private fun mostrarfoto(pos: Int) {
        val uriImage =
            Uri.parse(Environment.getExternalStorageDirectory()
                .toString() + "/TCGO_FILES/IMG/" + FotoNOM[pos].toString())
        iv?.setImageURI(uriImage)
    }

    fun listarfotos() {
        val fg = FuncionesGenerales()
        lista!!.clear()
        val queryNombres =
            "SELECT nfoto,idfoto FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and idpreg='" + idpregact + "' and opn=" + opnact + " order by idfoto"
        val nFotos = conGen?.queryObjeto2val(context, queryNombres, null)
        if (nFotos != null) {
            FotoNOM = arrayOfNulls(nFotos.size)
            FotoID = arrayOfNulls(nFotos.size)
            for (i in nFotos.indices) {
                FotoNOM[i] = nFotos[i][0].toString()
                FotoID[i] = nFotos[i][1].toString()

                lista?.add(RutaModel(FotoID[i].toString()))
            }
            val ra = RutaAdpater(lista!!)
            recycler?.adapter = ra
        } else {
            val ra = RutaAdpater(lista!!)
            recycler?.adapter = ra
        }
    }

    fun desplazamientos() {
        val fg = FuncionesGenerales()
        fg.ejecDB(requireContext(),
            "UPDATE '204_PREGGEN' SET evalfoto='" + fg.parametro(requireContext(), "FTOM")
                .toString() + "' WHERE idtarea='" + idtarea + "' and idpreg='$idpregact'")
        if (idpregact.equals("ANEXO_1") || idpregact.equals("ANEXO_2")) {
            openFragment("10")
        } else {
            openFragment("11")
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

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    companion object {
        private const val TAKE_PICTURE = 1
        var instance: FormFotos? = null
    }
}