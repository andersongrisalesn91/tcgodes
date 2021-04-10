package com.example.tcgokotlin.sqliteDBHelper

import android.location.LocationManager
import android.os.Build
import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class ProcesosTablas() {

    fun actualizar301_temp(contexto: Context,idtarea: String, idpreg: String, rta: String) {
        val operaciones = OperacionesBDInterna()
        operaciones.queryNoData(contexto,"UPDATE '301_RESPUESTAS_TEMP' SET rta2='" + rta + "' WHERE idtarea='" + idtarea + "' and idpreg='" + idpreg + "'")
    }

    fun actualizar301_temp10(contexto: Context,idtarea: String, idpreg: String, opcion: String, rta1: String, rta2: String) {
        val operaciones = OperacionesBDInterna()
        operaciones.queryNoData(contexto,"UPDATE '301_RESPUESTAS_TEMP' SET rta1='" + rta1 + "' , rta2='" + rta2 + "' WHERE idtarea='$idtarea' and idpreg='$idpreg' and opn=" + opcion)
    }
}