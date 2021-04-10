package com.example.tcgokotlin.Forms

import android.graphics.Bitmap
import android.location.Criteria
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.example.tcgokotlin.OnSignedCaptureListener
import com.example.tcgokotlin.R
import com.example.tcgokotlin.SignatureDialogFragment
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import kotlinx.android.synthetic.main.activity_form_tipo9.*
import kotlinx.android.synthetic.main.activity_form_tipo9.BTAtras
import kotlinx.android.synthetic.main.activity_form_tipo9.BTRegresar
import kotlinx.android.synthetic.main.activity_form_tipo9.BTSiguiente
import kotlinx.android.synthetic.main.activity_form_tipo9.TVNPantalla
import java.io.File
import java.io.FileOutputStream
import java.util.*


class FormTipo9 : Fragment() , OnSignedCaptureListener {
    var fg: FuncionesGenerales? = null
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
    private var opnact: String = ""
    private var useruid: String = ""
    private var orden: String = ""
    private var tpant: String = ""
    private var panact: String = ""
    private var firmact: String = ""
    private var myContext: FragmentActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.activity_form_tipo9, container, false)
        
    }

    private fun showDialog() {
        val dialogFragment = SignatureDialogFragment(this)
        dialogFragment.show((activity as AppCompatActivity).supportFragmentManager, "signature")
    }

    override fun onSignatureCaptured(bitmap: Bitmap, fileUri: String) {
        val conGen = ConsultaGeneral()

        val carpetaIMG = File(
            Environment.getExternalStorageDirectory().toString() + "/TCGO_FILES/",
            "IMG"
        )

        if (!carpetaIMG.exists()) {
            carpetaIMG.mkdirs()
        }

        var nombreFoto =
            fg?.parametro(requireContext(),"TAREA_ACT")
                .toString() + "_" + fg?.fechaActual(2) + "_" + fg?.parametro(requireContext(),"PREGUNTA_ACT")
                .toString() + "_" + fg?.parametro(requireContext(),"OPN_ACT").toString() + ".jpg"

        var path = Environment.getExternalStorageDirectory()
            .toString() + "/TCGO_FILES/IMG/"

        val file: File = File(path, nombreFoto.toString())
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)

        fg?.ejecDB(requireContext(),"UPDATE '204_PREGGEN' SET eval='1' WHERE idtarea='" + idtarea + "' and idpreg='$idpregact'")
        Act_Eval(1)
        val queryFoto =
            "INSERT INTO '302_FOTOS_RESP_TEMP' (idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid) VALUES('" +
                    idtarea + "' , '" + idformulario + "' , '" + idforma + "' , '" + idgrupo + "' , '" + idsgrupo + "' , '" + idpregact + "' , '" +
                    opnact + "' , '" + nombreFoto?.substringBefore(".") + "' , '" + nombreFoto + "' , '0' , '0' , '" + useruid + "');"
        fg?.ejecDB(requireContext(),queryFoto)
        val uriImage =
            Uri.parse(
                Environment.getExternalStorageDirectory()
                    .toString() + "/TCGO_FILES/IMG/" + nombreFoto.toString()
            )
        IVFirma?.setImageURI(uriImage)
    }

    override fun onStart() {
        super.onStart()
        val criteria = Criteria()
        fg = FuncionesGenerales()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
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
        val queryfirma = "SELECT nfoto FROM '005_FOTOS_PREG' order by idfoto"
        firmact = fg?.getQ1(requireContext(),queryfirma).toString()
        orden = fg?.getQ1(requireContext(),"select orden from '002_PREGUNTASB'").toString()
        if (!firmact.equals("0") && !firmact.equals(null) && !firmact.equals("") ){
            val uriImage =
                Uri.parse(
                    Environment.getExternalStorageDirectory()
                        .toString() + "/TCGO_FILES/IMG/" + firmact.toString()
                )
            IVFirma?.setImageURI(uriImage)
        }
        TVNPantalla.setText(titulopant)
        //Llenar el recycler con lo que contenga path
        ivCapturarFirma.setOnClickListener{
            showDialog()
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

    fun desplazamientos(Boton: Int) {
        val conGen = ConsultaGeneral()
        val querypreguntasrtaF =
            "SELECT ifnull(count(*),0) as cres FROM '005_FOTOS_PREG'"
        var rtax = fg?.getQ1(requireContext(),querypreguntasrtaF)

        val trta = if (rtax != "0") 1 else 0

        fg?.ejecDB(requireContext(),
            "UPDATE '204_PREGGEN' SET eval='$trta' WHERE idtarea='$idtarea' and idpreg='$idpregact'"
        )
        if (Boton != 3) {
            var querysigp = if (Boton == 2) {
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
                context, querysigp, null)

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
                fg?.act_param(requireContext(),"FORMA_ACT", mino[0][1].toString())
                fg?.act_param(requireContext(),
                    "NFORMA_ACT",
                    fg?.getQ1(requireContext(),"Select form from '201_PREGFORM' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "'")
                        .toString()
                )
                fg?.act_param(requireContext(),"GRUPO_ACT", mino[0][2].toString())
                fg?.act_param(requireContext(),
                    "NGRUPO_ACT",
                    fg?.getQ1(requireContext(),"Select grp from '202_PREGGRUP' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "' and grpid='" + mino[0][2].toString() + "'")
                        .toString()
                )
                fg?.act_param(requireContext(),"SUBGRUPO_ACT", mino[0][3].toString())
                fg?.act_param(requireContext(),
                    "NSUBGRUPO_ACT",
                    fg?.getQ1(requireContext(),"Select sgrp from '203_PREGSGRUP' where idtarea='" + idtarea + "' and idform='" + mino[0][1].toString() + "' and grpid='" + mino[0][2].toString() + "' and sgrpid='" + mino[0][3].toString() + "'")
                        .toString()
                )
                fg?.act_param(requireContext(),"ORDEN_ACT", mino[0][4].toString())
                fg?.act_param(requireContext(),"PREGUNTA_ACT", mino[0][5].toString())
                fg?.act_param(requireContext(),"NPREGUNTA_ACT", mino[0][6].toString())
                fg?.act_param(requireContext(),"TITULO_ACT", mino[0][6].toString())
                fg?.act_param(requireContext(),"IDFOTO_ACT", "0")
                val Tipox = mino[0][0].toString()
                Act_Eval(trta)
                openFragment(Tipox)
            } else {
                try {
                    Act_Eval(trta)
                } finally {
                    openFragment(tpant)
                }
            }
        } else {
            try {
                Act_Eval(trta)
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
        var instance: FormTipo9? = null
    }
}