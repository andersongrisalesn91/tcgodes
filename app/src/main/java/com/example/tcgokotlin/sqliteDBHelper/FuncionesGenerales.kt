package com.example.tcgokotlin.sqliteDBHelper

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.tcgokotlin.R
import com.example.tcgokotlin.Sincronizar.CargarEstados
import com.example.tcgokotlin.data.model.MenuFM
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FuncionesGenerales() {
    var sd: File? = null
    var cini: File? = null
    var queryActVal: String? = null
    var flujo: String? = null

    fun parametro(contexto: Context, param: String): String {
        val conGen = ConsultaGeneral()
        var idC = ""
        val queryCli = "SELECT va FROM params WHERE pa=?"
        val objeto = conGen.queryObjeto(
            contexto, queryCli, arrayOf(param)
        )
        idC = if (objeto == null || objeto.size < 1) {
            return ""
        } else {
            objeto[0][0].toString()
        }
        return idC
    }

    fun clienteActual(contexto: Context): String {
        val conGen = ConsultaGeneral()
        var idC = ""
        val queryCli = "SELECT va FROM params WHERE pa=?"
        val objeto = conGen.queryObjeto(
            contexto, queryCli, arrayOf("CLIENTE_ACT")
        )
        idC = if (objeto == null || objeto.isEmpty()) {
            return ""
        } else {
            objeto[0][0].toString()
        }

        return idC
    }

    fun getlat(contexto: Context): String {
        val conGen = ConsultaGeneral()
        var idC = ""
        val querygps = "SELECT gpslat FROM '200_TAREAS' WHERE idtarea='" + tarea_act(contexto) + "'"
        val respgps = getQ1(contexto, querygps)
        return respgps
    }

    fun getlon(contexto: Context): String {
        val conGen = ConsultaGeneral()
        var idC = ""
        val querygps = "SELECT gpslon FROM '200_TAREAS' WHERE idtarea='" + tarea_act(contexto) + "'"
        val respgps = getQ1(contexto, querygps)
        return respgps
    }

    fun getlatp(contexto: Context, idtarea: String): String {
        val conGen = ConsultaGeneral()
        var idC = ""
        val querygps = "SELECT gpslat FROM '200_TAREAS' WHERE idtarea='$idtarea'"
        val respgps = getQ1(contexto, querygps)
        return respgps
    }

    fun getlonp(contexto: Context, idtarea: String): String {
        val conGen = ConsultaGeneral()
        var idC = ""
        val querygps = "SELECT gpslon FROM '200_TAREAS' WHERE idtarea='$idtarea'"
        val respgps = getQ1(contexto, querygps)
        return respgps
    }

    fun act_param(contexto: Context, paramupdate: String, valorupdate: String) {
        ejecDB(contexto, "update params set va='$valorupdate' where pa='$paramupdate'")
    }

    fun val_param(contexto: Context, pa: String): String {
        val conGen = ConsultaGeneral()
        var pact = ""
        val queryCli = "SELECT va FROM params WHERE pa='$pa'"
        val objeto = conGen.queryObjeto2val(
            contexto, queryCli, null
        )
        pact = if (objeto == null || objeto.size < 1) {
            return pact
        } else {
            objeto[0][0].toString()
        }
        return pact
    }

    fun tarea_actual(contexto: Context, idtarea: String): String {
        var pact = ""
        val queryest = "SELECT estado FROM '200_TAREAS' WHERE idtarea='$idtarea'"
        val objeto = getQ1(contexto, queryest)
        return objeto
    }

    fun standby_actual(contexto: Context, idtareastdby: String): String {
        var pact = ""
        val querySTDBY = "SELECT standby FROM '200_TAREAS' WHERE idtarea='$idtareastdby'"
        val objeto = getQ1(contexto, querySTDBY)
        return objeto.toString()
    }

    fun fotoencabezado(contexto: Context, varenc: String): String {
        val querVE =
            "SELECT ifnull(" + varenc + "nfoto,'0') FROM '101_REGISTRO' where useruid='" + useruid(
                contexto) +
                    "' and fecha='" + fechaActual(1) + "';"
        val nfoto = getQ1(contexto, querVE)
        return nfoto
    }


    fun ins_historial(contexto: Context, standby: String, gpslat: String, gpslon: String) {
        try {
            act_param(contexto, "ULT_LAT", gpslat)
            act_param(contexto, "ULT_LON", gpslon)
        } finally {
            val fecha = fechaActual(1)
            var idhistorial = "0"
            try {
                val querymaxid =
                    "select ifnull(max(idhistorial),0) as mr from '102_HISTORIAL' where useruid='" + useruid(
                        contexto) + "' and idtarea='" + tarea_act(contexto) + "' and fecha='" + fecha + "'"
                idhistorial = getQ1(contexto, querymaxid)
            } finally {
                val queryCli =
                    "INSERT INTO '102_HISTORIAL'(useruid,idtarea,fecha,idhistorial,gpslat,gpslon,estado,estadorec,standby,idclientw) VALUES('" + useruid(
                        contexto) + "' , '" + tarea_act(contexto) + "' , '" + fecha + "' , '" + ((idhistorial.toInt() + 1).toString()) + "' , '" + gpslat + "' , '" + gpslon + "' , '" + estado_act(
                        contexto) + "' , '" + estadorec_act(contexto) + "' , '" + standby + "' , '" + parametro(
                        contexto,
                        "IDCLIENTW") + "');"
                ejecDB(contexto, queryCli)
            }
        }
    }

    fun act_estadotarea(contexto: Context) {
        val ce = CargarEstados()
        val fecha = fechaActual(1)
        val queryCli = "UPDATE '200_TAREAS' SET estado='" + parametro(contexto,
            "ESTADO_ACT") + "' , novedad='" + novedad(contexto) + "' , tiponovedad='" + idnovedad(
            contexto) + "' where idtarea='" + tarea_act(contexto) + "';"
        try {
            ejecDB(contexto, queryCli)
        } finally {
            ce.ActualizarServicesp(contexto,tarea_act(contexto))
        }
    }

    fun act_estadotareaandsync(contexto: Context,idtarea:String,estado:String,estadorec:String,stdby:String) {
        val ce = CargarEstados()
        val fecha = fechaActual(1)
        act_param(contexto, "ESTADO_ACT", estado)
        val querySTDBY =
            "UPDATE '200_TAREAS' SET standby='$stdby'  where  idtarea='$idtarea';"
        ejecDB(contexto, querySTDBY)
        act_param(contexto, "ESTADOREC_ACT", estadorec)
        val queryCli = "UPDATE '200_TAREAS' SET estado='" + estado + "' , novedad='" + novedad(contexto) + "' , tiponovedad='" + idnovedad(
            contexto) + "' where idtarea='" + idtarea + "';"
        try {
            ejecDB(contexto, queryCli)
        } finally {
            ce.ActualizarServicesp(contexto,tarea_act(contexto))
        }
    }

    fun act_estadotareap(contexto: Context, idtarea: String) {
        val ce = CargarEstados()
        val fecha = fechaActual(1)
        val queryCli = "UPDATE '200_TAREAS' SET estado='" + parametro(contexto,
            "ESTADO_ACT") + "' , novedad='" + novedad(contexto) + "' , tiponovedad='" + idnovedad(
            contexto) + "' where idtarea='" + idtarea + "';"
        try {
            ejecDB(contexto, queryCli)
        } finally {
            ce.ActualizarServicesp(contexto,idtarea)
        }
    }

    fun act_estadotareac(contexto: Context, idtarea: String, estado: String) {
        val ce = CargarEstados()
        val fecha = fechaActual(1)
        act_param(contexto, "ESTADO_ACT", estado)
        val querytar =
            "UPDATE '200_TAREAS' SET estado='" + estado + "' where idtarea='" + idtarea + "';"
        val querynotar =
            "UPDATE '200_TAREAS' SET estado='Pendiente' where estado='$estado' and idtarea<>'" + idtarea + "';"
        try {
            try{
                ejecDB(contexto, querynotar)
                ejecDB(contexto, querytar)
            }finally {
                ce.ActualizarServicesp(contexto, idtarea)
            }
        } finally {
            act_estadosall(contexto)
        }
    }

    fun act_estadotareapro(contexto: Context, idtarea: String, estado: String) {
        val ce = CargarEstados()
        val fecha = fechaActual(1)
        val cantrec = getQ1(contexto,
            "SELECT ifnull(count(idtarea),'0') FROM '200_TAREAS' where estado='En Recorrido' or estado='En Proceso'")
        if (cantrec != "0") {
            act_param(contexto, "ESTADO_ACT", estado)
            val querytar =
                "UPDATE '200_TAREAS' SET estado='" + estado + "' , novedad='" + novedad(contexto) + "' , tiponovedad='" + idnovedad(
                    contexto) + "' where idtarea='" + idtarea + "';"
            val querynotar =
                "UPDATE '200_TAREAS' SET estado='Pendiente' where estado='$estado' and idtarea<>'" + idtarea + "';"
            try {
                ejecDB(contexto, querytar)
                ejecDB(contexto, querynotar)
                ce.ActualizarServicesp(contexto, idtarea)
            } finally {
                act_estadosall(contexto)
            }
        } else {
            ejecDB(contexto,
                "update '200_TAREAS' set estado='Pendiente' where estado='$estado';")
        }
    }

    fun act_recorridosapend(contexto: Context) {
        val ce = CargarEstados()
        try{
            ejecDB(contexto,"update '200_TAREAS' set estado='Pendiente' where estado='En Recorrido';")
        }finally {
            ce.ActualizarServicesp(contexto, tarea_act(contexto))
        }
    }

    fun act_estadosall(contexto: Context) {
        val conGen = ConsultaGeneral()
        val ce = CargarEstados()
        val cantrec = getQ1(contexto,
            "SELECT ifnull(count(idtarea),'0') FROM '200_TAREAS' where (activa='1' or abierta='1')")
        if (cantrec != "0") {
            val queryTREC =
                "SELECT idtarea FROM '200_TAREAS' where (activa='1' or abierta='1')"
            val objTREC =
                conGen.queryObjeto2val(contexto, queryTREC, null)
            if (objTREC.isNotEmpty()) {
                for (op in objTREC.indices) {
                    ce.ActualizarServicesp(contexto, objTREC[op][0])
                }
            }
        }
    }

    fun act_standby(contexto: Context, stdby: String) {
        val queryCli =
            "UPDATE '200_TAREAS' SET standby='" + stdby + "'  where  idtarea='" + tarea_act(contexto) + "';"
        ejecDB(contexto, queryCli)
    }

    fun ejecDB(contexto: Context, sql: String) {
        val operaciones = OperacionesBDInterna()
        val estado = operaciones.queryNoData(contexto, sql)
    }

    fun pantallaactual(contexto: Context): String {
        val conGen = ConsultaGeneral()
        var pact = ""
        val queryCli = "SELECT va FROM params WHERE pa='ULTIMAP'"
        val objeto = conGen.queryObjeto(
            contexto, queryCli, null
        )
        pact = if (objeto == null || objeto.size < 1) {
            return pact
        } else {
            objeto[0][0].toString()
        }
        return pact
    }

    fun tcsig(contexto: Context, tablasinc: String, tablaact: String, tipocampo: Int): String {
        val conGen = ConsultaGeneral()
        var valor = "0"
        val idtabla_act = idtablaact(contexto, tablasinc, tablaact).toInt()
        val cantidad_reg = cant_reg(contexto, tablaact).toInt()
        val ord = idtabla_act.toInt().plus(1);
        val queryCli = "SELECT ntabla,ncampo FROM '$tablasinc' WHERE orden=" + ord.toString()
        if (idtabla_act.toInt() != cantidad_reg.toInt()) {
            val objeto = conGen.queryObjeto(
                contexto, queryCli, null
            )
            valor = if (objeto == null || objeto.size < 1) {
                return valor
            } else {
                if (tipocampo == 1) {
                    objeto[0][0].toString()
                } else {
                    objeto[0][1] as String
                }
            }
        } else {
            return valor
        }

        return valor
    }

    fun idtablaact(contexto: Context, tablasinc: String, tablaact: String): String {
        val conGen = ConsultaGeneral()
        var valor: String = "0"
        val query = "SELECT orden FROM '$tablasinc' WHERE ntabla='" + tablaact + "';"
        val objeto = conGen.queryObjeto(
            contexto, query, null
        )
        valor = if (objeto == null || objeto.isEmpty()) {
            return valor
        } else {
            objeto[0][0].toString()
        }
        return valor
    }

    fun cant_reg(contexto: Context, ntabla: String): String {
        val conGen = ConsultaGeneral()
        var valor: String = "0"
        val queryCli = "SELECT count(*) as CR FROM '$ntabla';"
        val objeto = conGen.queryObjeto(
            contexto, queryCli, null
        )
        valor = if (objeto == null || objeto.isEmpty()) {
            return valor
        } else {
            objeto[0][0].toString()
        }
        return valor
    }


    fun getQ(contexto: Context, SQL: String): Array<out ArrayList<String>>? {
        val conGen = ConsultaGeneral()
        return conGen.queryObjeto(
            contexto, SQL, null
        )
    }

    fun getQ1(contexto: Context, SQL: String): String {
        val conGen = ConsultaGeneral()
        var va: String = ""
        val objV = conGen.queryObjeto(
            contexto, SQL, null
        )
        if (objV != null) {
            va = objV[0][0].toString()
        }
        return va
    }

    fun getQS(contexto: Context, SQL: String): Array<String?> {
        val conGen = ConsultaGeneral()
        val va: Array<String?>
        var cantcol = 0
        val objV = conGen.queryObjeto2val(
            contexto, SQL, null
        )
        va = arrayOfNulls(objV.size)
        if (objV.isNotEmpty()) {
            while (cantcol < objV.size) {
                va[cantcol] = objV[0][cantcol].toString()
                cantcol++
            }
        }
        return va
    }

    fun tarea_act(contexto: Context): String {
        val conGen = ConsultaGeneral()
        val queryAud = "SELECT va FROM params WHERE pa=?"
        val idCE = conGen.queryObjeto(contexto, queryAud, arrayOf("TAREA_ACT"))
        return idCE[0][0].toString()
    }

    fun opn_act(contexto: Context): String {
        val conGen = ConsultaGeneral()
        val queryAud = "SELECT va FROM params WHERE pa=?"
        val idCE = conGen.queryObjeto(contexto, queryAud, arrayOf("OPN_ACT"))
        return idCE[0][0].toString()
    }

    fun idpreg_act(contexto: Context): String {
        val conGen = ConsultaGeneral()
        val queryAud = "SELECT va FROM params WHERE pa=?"
        val idCE = conGen.queryObjeto(contexto, queryAud, arrayOf("PREGUNTA_ACT"))
        return idCE[0][0].toString()
    }


    fun estadorec_txt(estrec: Int): String {
        var ret: String = ""
        when (estrec) {
            0 -> {
                ret = "Sin Iniciar recorrido"
            }
            1 -> {
                ret = "En recorrido"
            }
            2 -> {
                ret = "En tarea"
            }
            3 -> {
                ret = "Reporte Demora"
            }
            4 -> {
                ret = "Recorrido Reanudado"
            }
            5 -> {
                ret = "Recorrido Cancelado"
            }
        }
        return ret
    }

    fun typeservice(contexto: Context): String {
        val typeservicecode = getQ1(contexto,
            "SELECT ifnull(typeservice,'0') FROM '200_TAREAS' WHERE idtarea='" + tarea_act(contexto) + "' and fecha='" + fechaActual(
                1) + "'")
        val typeservicetxt = getQ1(contexto,
            "SELECT ifnull(tiposervicio,'No guardado') FROM '104_TIPO_SERVICIO' WHERE idtiposerv='" + typeservicecode + "'")
        return typeservicetxt
    }

    fun haytarproceso(contexto: Context): String {
        val proceso = getQ1(contexto,
            "SELECT ifnull(count(*),'0') FROM '200_TAREAS' WHERE estado='En Proceso' and fecha='" + fechaActual(
                1) + "' and (activa='1' or abierta='1')")
        val tarproc = if (proceso != "0") {
            "1"
        } else {
            "0"
        }
        return tarproc
    }

    fun tarprocesoact(contexto: Context): String {
        val proceso = getQ1(contexto,
            "SELECT ifnull(count(*),'0') FROM '200_TAREAS' WHERE estado='En Proceso' and idtarea='" + tarea_act(
                contexto) + "' and fecha='" + fechaActual(1) + "' and (activa='1' or abierta='1')")
        val tarproc = if (proceso != "0") {
            "1"
        } else {
            "0"
        }
        return tarproc
    }

    fun haytarrecorrido(contexto: Context): String {
        val proceso = getQ1(contexto,
            "SELECT ifnull(count(*),'0') FROM '200_TAREAS' WHERE estado='En Recorrido' and fecha='" + fechaActual(
                1) + "' and (activa='1' or abierta='1')")
        val tarproc = if (proceso != "0") {
            "1"
        } else {
            "0"
        }
        return tarproc
    }


    fun tarrecorridoact(contexto: Context): String {
        val proceso = getQ1(contexto,
            "SELECT ifnull(count(*),'0') FROM '200_TAREAS' WHERE estado='En Recorrido' and idtarea='" + tarea_act(
                contexto) + "' and fecha='" + fechaActual(1) + "' and (activa='1' or abierta='1')")
        val tarproc = if (proceso != "0") {
            "1"
        } else {
            "0"
        }
        return tarproc
    }
    fun tarstandbyact(contexto: Context): String {
        val standby = getQ1(contexto,
            "SELECT standby FROM '200_TAREAS' WHERE  idtarea='" + tarea_act(contexto) + "'")
        return standby
    }

    fun idtarearecact(contexto: Context): String {
        val tarecact = getQ1(contexto,
            "SELECT ifnull(idtarea,'0') FROM '200_TAREAS' WHERE estado='En Recorrido' and  and (activa='1' or abierta='1')").toString()
        return tarecact
    }

    fun standby_act(contexto: Context): String {
        val maxidhistorial = getQ1(contexto,
            "SELECT ifnull(max(idhistorial),'0') FROM '102_HISTORIAL' WHERE idtarea='" + tarea_act(
                contexto) + "' and standby<>'0'")
        val stanby_txt = getQ1(contexto,
            "SELECT standby FROM '102_HISTORIAL' WHERE idtarea='" + tarea_act(contexto) + "' and idhistorial='" + maxidhistorial + "'")
        return stanby_txt
    }

    fun key_act(contexto: Context): String {
        val valkey = getQ1(contexto,
            "SELECT valkey FROM '200_TAREAS' WHERE idtarea='" + tarea_act(contexto) + "'")
        return valkey
    }


    fun estado_act(contexto: Context): String {
        val conGen = ConsultaGeneral()
        val queryAud = "SELECT va FROM params WHERE pa=?"
        val idCE = conGen.queryObjeto(contexto, queryAud, arrayOf("ESTADO_ACT"))
        return idCE[0][0].toString()
    }

    fun estadorec_act(contexto: Context): String {
        val conGen = ConsultaGeneral()
        val queryAud = "SELECT va FROM params WHERE pa=?"
        val idCE = conGen.queryObjeto(contexto, queryAud, arrayOf("ESTADOREC_ACT"))
        return idCE[0][0].toString()
    }

    fun useruid(contexto: Context): String {
        val conGen = ConsultaGeneral()
        val queryAud = "SELECT va FROM params WHERE pa=?"
        val idCE = conGen.queryObjeto(contexto, queryAud, arrayOf("USERUID"))
        return idCE[0][0].toString()
    }

    fun novedad(contexto: Context): String {
        val conGen = ConsultaGeneral()
        val queryAud = "SELECT va FROM params WHERE pa=?"
        val idCE = conGen.queryObjeto(contexto, queryAud, arrayOf("NOVEDAD"))
        return idCE[0][0].toString()
    }

    fun idnovedad(contexto: Context): String {
        val conGen = ConsultaGeneral()
        val queryAud = "SELECT va FROM params WHERE pa=?"
        val idCE = conGen.queryObjeto(contexto, queryAud, arrayOf("IDNOVEDAD"))
        return idCE[0][0].toString()
    }

    fun fechaActual(acc: Int): String {
        var fecha = ""
        if (acc == 1) {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = Date()
            fecha = dateFormat.format(date)
        } else if (acc == 2) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date()
            val fechaTemp = dateFormat.format(date)
            var st = StringTokenizer(fechaTemp)
            val fechaT = st.nextToken()
            val horaT = st.nextToken()
            st = StringTokenizer(fechaT, "-")
            fecha = st.nextToken() + st.nextToken() + st.nextToken()
            st = StringTokenizer(horaT, ":")
            fecha += st.nextToken() + st.nextToken() + st.nextToken()
        } else if (acc == 3) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date()
            fecha = dateFormat.format(date)
        } else if (acc == 4) {
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = Date()
            fecha = dateFormat.format(date)
        }
        return fecha
    }

    fun getFototom(contexto: Context, ord: Int): String {
        val conGen = ConsultaGeneral()
        var idO = ""
        val queryftact =
            "SELECT count(*) as co FROM '302_FOTOS_RESP_TEMP' where ïdtarea='" + tarea_act(contexto) + "'and preg='" + idpreg_act(
                contexto) + "' and opn=" + opn_act(contexto)
        val ord = conGen.queryObjeto2val(
            contexto, queryftact, null
        )
        idO = if (ord[0][0].toString() == "0") {
            "0"
        } else {
            "1"
        }
        return idO
    }

    fun BorrarFoto(idfoto: String) {
        val fullPath = Environment.getExternalStorageDirectory().toString() + "/TCGO_FILES/IMG/"
        try {
            val file = File(fullPath, idfoto)
            if (file.exists()) {
                try {
                    file.delete()
                } catch (exp: Exception) {
                    println("La aplicación no puede borrar el archivo y el resultado es: " + exp.toString())
                }
            } else {
                println("No existe el archivo:$idfoto")
            }
        } catch (e: Exception) {
            Log.e("App", "Error al borrar el archivo " + e.message)
        }
    }

}