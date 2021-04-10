package com.example.tcgokotlin.sqliteDBHelper

import android.content.Context
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.Sincronizar.CargarEstados
import com.example.tcgokotlin.Sincronizar.SincronizarOnline
import com.example.tcgokotlin.utils.Common
import java.util.*
import kotlin.math.round

class LoadTaskSqlite()  {

    fun cargarservices(contexto: Context, arrayser_load: ArrayList<MutableMap<String, Any>>) {
        val fg = FuncionesGenerales()
        val latitud_ult = fg?.parametro(contexto, "ULT_LAT")
        val longitud_ult = fg?.parametro(contexto, "ULT_LON")
        var latact = latitud_ult
        var lonact = longitud_ult
        val fechaact = fg.fechaActual(1)
        fg.ejecDB(contexto, "update '200_TAREAS' set abierta='0',activa='0'")
        for (mapService in arrayser_load) {
            val bk = Backups()
            bk.backupdDatabase(contexto)
            val idtarea = mapService.get("codetarea").toString()
            val tareaexiste = fg.getQ1(contexto,
                "select ifnull(count(idtarea),0) as te from '200_TAREAS' where idtarea='$idtarea' and fecha='" + fechaact + "'")
            if (tareaexiste != "0"){
                val historial = mapService.get("historial") as MutableMap<String, Any?>?
                val history = historial?.get("$fechaact") as MutableMap<String, Any?>?
                val historynotes =  history?.get("historyNote") as ArrayList<MutableMap<String, String>>
                val ejecucion = mapService.get("ejecucion") as MutableMap<String, String>
                val latlong = mapService.get("latLng") as MutableMap<String, Any?>?
                var tamnotes = historynotes.size.toInt()
                var cantop = 0
                for (it in historynotes) {
                    var sql = ""
                    var mapanote: MutableMap<String, String> = historynotes[cantop]
                    sql = "INSERT OR IGNORE INTO '206_HISTORY_NOTES' VALUES ('" + mapService.get("codetarea") + "','$fechaact','$cantop','" +
                            mapanote["iduser"] + "','" + mapanote["nota"] + "','" + mapanote["tipouser"] + "');"
                    fg.ejecDB(contexto, sql)
                    cantop++
                }
                val sql: String
                sql = "UPDATE '200_TAREAS' SET " +
                        "motivoreag='" + history.get("motivReag").toString()  + "' ,motivonoex='" + history.get(
                    "motivoNoEx").toString()  + "' ," +
                        "novedad='" + mapService.get("prsNoved").toString()  + "' ,tiponovedad='" + history.get(
                    "tipNov").toString()  + "' ," +
                        "estado='" + mapService.get("estado").toString()  + "' ,ejecfechain='" + ejecucion.get(
                    "fechain").toString()  + "' ," +
                        "gpslat='" + latlong?.get("latitude").toString()  + "' ,gpslon='" + latlong?.get(
                    "longitude").toString()  + "' ," +
                        "activa='" + history.get("active").toString()  + "' , abierta='" + ejecucion.get(
                    "abierta").toString() +
                        "' where idtarea='" + mapService.get("codetarea") + "';"
                fg.ejecDB(contexto, sql)
                if (latact=="0" || latact=="") latact = "0.0"
                if (lonact=="0" || lonact=="") lonact = "0.0"

                val distancia = Tools.calcCrow(
                    latact.toDouble() as Double, lonact.toDouble(),
                    latlong?.get("latitude").toString().toDouble(),
                    latlong?.get("longitude").toString().toDouble()
                ) * 1000
                var dist = round(distancia)

                fg.ejecDB(contexto,"UPDATE '200_TAREAS_DIST' SET " +
                        "gpslat='" + latlong?.get("latitude").toString() + "' , " +
                        "gpslon='" + latlong?.get("longitude").toString() + "' , " +
                        "activa='" + history.get("active").toString() + "' , " +
                        "abierta='" + ejecucion.get("abierta").toString() + "' , " +
                        "distancia=$dist where idtarea='" + mapService.get("codetarea").toString() + "';")

            } else {
                fg.ejecDB(contexto,
                    "delete from '200_TAREAS' where idtarea='" + mapService.get("codetarea")
                        .toString() + "' and fecha<>'" + fechaact + "';")
                val tarea = Common.listTipoServ!![mapService.get("typeService")].toString()
                val tiposerv = mapService.get("typeService").toString()
                val tipotarea = mapService.get("tipoTarea").toString()
                fg.act_param(contexto, "TAREA_ACT", mapService.get("codetarea").toString())
                fg.act_param(contexto, "ESTADO_ACT", mapService.get("estado").toString())
                val infocliente = mapService.get("infoCliente") as MutableMap<String?, Any?>?
                val infologic = mapService.get("infoLogic") as MutableMap<String, String>
                fg.act_param(contexto, "IDCLIENTW", infologic.get("idCliente").toString())
                val formstruct = mapService.get("formStruct") as MutableMap<String?, Any?>?
                val ejecucion = mapService.get("ejecucion") as MutableMap<String, String>
                val historial = mapService.get("historial") as MutableMap<String?, Any?>?
                val latlong = mapService.get("latLng") as MutableMap<String?, Any?>?
                val contenido = formstruct?.get("contenido") as ArrayList<MutableMap<String?, Any?>?>?
                val infogen = formstruct?.get("infogen") as MutableMap<String?, Any?>?
                val history = historial?.get("$fechaact") as MutableMap<String?, Any?>?
                val historynotes =  history?.get("historyNote") as ArrayList<MutableMap<String, String>>
                var tamnotes = historynotes.size.toInt()
                var cantop = 0
                for (it in historynotes) {
                    var sql = ""
                    var mapanote: MutableMap<String, String> = historynotes[cantop]
                    sql = "INSERT OR IGNORE INTO '206_HISTORY_NOTES' VALUES ('" + mapService.get("codetarea") + "','$fechaact','$cantop','" +
                            mapanote["iduser"] + "','" + mapanote["nota"] + "','" + mapanote["tipouser"] + "');"
                    fg.ejecDB(contexto, sql)
                    cantop++
                }

                fg.ins_historial(contexto, "0", latact, lonact)

                val sql: String?
                sql = "INSERT OR IGNORE INTO '200_TAREAS' VALUES ('" + mapService.get("codetarea").toString() + "','" +
                        tarea + "','" +
                        fechaact + "','" +
                        mapService.get("typeService").toString() + "','" +
                        infocliente?.get("identiClient").toString() + "','" +
                        infocliente?.get("nomClient").toString() + "','" +
                        infocliente?.get("pais").toString() + "','" +
                        infocliente?.get("depto").toString() + "','" +
                        infocliente?.get("ciudad").toString() + "','" +
                        infocliente?.get("direction").toString() + "','" +
                        infocliente?.get("tel1").toString() + "','" +
                        infocliente?.get("tel2").toString() + "','" +
                        infocliente?.get("email").toString() + "','" +
                        infocliente?.get("enviarCorreo").toString() + "','" +
                        "0','" +
                        "0','" +
                        mapService.get("estado").toString() + "','" +
                        mapService.get("prsNoved").toString() + "','" +
                        history.get("tipNov").toString() + "','" +
                        mapService.get("standBy").toString() + "','" +
                        mapService.get("prioridad").toString() + "','" +
                        mapService.get("tipoTarea").toString() + "','" +
                        mapService.get("campo1").toString() + "','" +
                        mapService.get("campo2").toString() + "','" +
                        mapService.get("campo3").toString() + "','" +
                        mapService.get("nota").toString() + "','" +
                        mapService.get("key").toString() + "','" +
                        "0','" +
                        "0','" +
                        "0','" +
                        fg.parametro(contexto, "USERUID") + "','" +
                        mapService.get("typeService").toString() + "','" +
                        "0','" +
                        history.get("motivReag").toString() + "','" +
                        history.get("motivoNoEx").toString() + "','" +
                        history.get("active").toString() + "','" +
                        ejecucion.get("abierta").toString() + "','" +
                        ejecucion.get("fechain").toString() + "','" +
                        ejecucion.get("fechafin").toString() + "','" +
                        latlong?.get("latitude").toString() + "','" +
                        latlong?.get("longitude").toString() + "'," +
                        "0)"
                fg.ejecDB(contexto, sql)

                if (latact=="0" || latact=="") latact = "0.0"
                if (lonact=="0" || lonact=="") lonact = "0.0"

                val distancia = Tools.calcCrow(
                    latact.toDouble() as Double, lonact.toDouble(),
                    latlong?.get("latitude").toString().toDouble(),
                    latlong?.get("longitude").toString().toDouble()
                ) * 1000
                var dist = round(distancia)

                fg.ejecDB(contexto,"INSERT OR IGNORE INTO '200_TAREAS_DIST' VALUES " +
                        "('" + mapService.get("codetarea").toString() + "','" +
                        ejecucion.get("abierta").toString() + "','" +
                        history.get("active").toString() + "'," +
                        dist + ",'" +
                        latlong?.get("latitude").toString() + "','" +
                        latlong?.get("longitude").toString() + "');")

                if (tipotarea.equals("0")) {
                    fg.ejecDB(contexto,
                        "INSERT OR IGNORE INTO '108_FORMULARIOS' (idtarea,idformulario,formulario) VALUES " +
                                "('" + idtarea + "' , '999' , '" + tarea + "') ;"
                    )
                    val cond =
                        "select ifnull(count(rta2),0) as filtro from ''301_RESPUESTAS_TEMP'' where idpreg=''GEN_5'' and rta2=''1'' and idtarea=''" + idtarea + "'';"

                    fg.ejecDB(contexto,
                        "INSERT OR IGNORE INTO '201_PREGFORM' ( idtarea ,idformulario, idform ,form, cdn ) VALUES " +
                                "('" + idtarea + "' , '999' , '1' , 'Estado de la Tarea','0') , " +
                                "('" + idtarea + "' , '999' , '2' , 'Foto Visita del lugar','" + cond + "') , " +
                                "('" + idtarea + "' , '999' , '3' , 'Foto Documento soporte','" + cond + "') , " +
                                "('" + idtarea + "' , '999' , '4' , 'Foto Entrega mercancia al Cliente','" + cond + "') ;"
                    )

                    fg.ejecDB(contexto,
                        "INSERT OR IGNORE INTO '301_RESPUESTAS_TEMP' " +
                                "( idtarea ,idformulario , idform ,grpid,sgrpid,idpreg,preg,rta1,useruid) VALUES " +
                                "('" + idtarea + "' , '999' , '9999' , '0' , '0' , 'GEN_1' , 'Observaciones Generales','0','" + fg.parametro(
                            contexto,
                            "USERUID") + "') , " +
                                "('" + idtarea + "' , '999' , '9999' , '0' , '0' , 'GEN_2' , 'Autoriza el Tratamiento de mis datos personales','1','" + fg.parametro(
                            contexto,
                            "USERUID") + "') , " +
                                "('" + idtarea + "' , '999' , '9999' , '0' , '0' , 'GEN_3' , 'Ingrese el Nombre del Cliente','0','" + fg.parametro(
                            contexto,
                            "USERUID") + "') , " +
                                "('" + idtarea + "' , '999' , '9999' , '0' , '0' , 'GEN_4' , 'Ingrese la Cedula del Cliente','0','" + fg.parametro(
                            contexto,
                            "USERUID") + "') ; "
                    )

                    val regins = fg.getQ1(contexto,
                        "select ifnull(count(*),0) as cr from '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and (idpreg='FIRMA_CLI' or idpreg='FIRMA_USER');").toString()
                    if (regins.equals("0")){
                        fg.ejecDB(contexto,
                            "INSERT OR IGNORE INTO '302_FOTOS_RESP_TEMP' " +
                                    "( idtarea ,idformulario , idform ,grpid,sgrpid,idpreg,useruid) VALUES " +
                                    "('" + idtarea + "' , '999' , '9999' , '0' , '0' , 'FIRMA_CLI' , '" + fg.parametro(
                                contexto,
                                "USERUID"
                            ) + "') , " +
                                    "('" + idtarea + "' , '999' , '9999' , '0' , '0' , 'FIRMA_USER' , '" + fg.parametro(
                                contexto,
                                "USERUID"
                            ) + "') ; "
                        )
                    }

                    fg.ejecDB(contexto,
                        "INSERT OR IGNORE INTO '204_PREGGEN' " +
                                "( idtarea ,idformulario , idform ,grpid,sgrpid,orden,idpreg,preg,pregtxt,tipo,aplica,fotoap,fotomin,fotomax,fototipo,r1,r2,ap,cnd1) VALUES " +
                                "('" + idtarea + "' , '999' , '1' , '0' , '0' , '1' , 'GEN_5' , 'Seleccione el Estado de la Tarea','Observaciones','2','1','1','1','4','0','3','250','1','0') , " +
                                "('" + idtarea + "' , '999' , '2' , '0' , '0' , '2' , 'GEN_6' , 'Capture la Foto de la Visita del lugar','0','8','1','1','1','4','2','0','0','1','" + cond + "') , " +
                                "('" + idtarea + "' , '999' , '3' , '0' , '0' , '3' , 'GEN_7' , 'Capture la Foto Documento soporte','0','8','1','1','1','4','2','0','0','1','" + cond + "') , " +
                                "('" + idtarea + "' , '999' , '4' , '0' , '0' , '4' , 'GEN_8' , 'Capture la Foto Entrega mercancia al Cliente','0','8','1','1','1','4','2','0','0','1','" + cond + "') ; "
                    )
                    fg.ejecDB(contexto,
                        "INSERT OR IGNORE INTO '205_PREGGEND' " +
                                "( idtarea ,idformulario , idform ,grpid,sgrpid,idpreg,opn,opt) VALUES " +
                                "('" + idtarea + "' , '999' , '1' , '0' , '0' , 'GEN_5' , '1' , 'Existosa') , " +
                                "('" + idtarea + "' , '999' , '1' , '0' , '0' , 'GEN_5' , '2' , 'No Existosa') ; "
                    )

                } else {
                    val idformulario = infogen?.get("id_form").toString() as String
                    fg.ejecDB(contexto,
                        "INSERT OR IGNORE INTO '108_FORMULARIOS' VALUES " +
                                "('" + idtarea + "' , '" + idformulario
                                + "' , '" + infogen?.get("nombre_form").toString()
                                + "' , '" + infogen?.get("descrip").toString()
                                + "' , '" + infogen?.get("conse").toString()
                                + "' , '" + infogen?.get("requi").toString()
                                + "' , '" + infogen?.get("render_form_consumo").toString()
                                + "' , '" + infogen?.get("nombre_anexo_1").toString()
                                + "' , '" + infogen?.get("nombre_anexo_2").toString()
                                + "' , '" + infogen?.get("titulo_doc").toString()
                                + "' , '" + infogen?.get("titulo_obser").toString()
                                + "' , '" + infogen?.get("campo_1").toString()
                                + "' , '" + infogen?.get("campo_2").toString()
                                + "' , '" + infogen?.get("campo_3").toString() + "',0) ;"
                    )
                    val ap_anexo1 = infogen?.get("requi").toString()
                    val ap_anexo2 = infogen?.get("render_form_consumo").toString()
                    val nom_anexo1 = infogen?.get("nombre_anexo_1").toString()
                    val nom_anexo2 = infogen?.get("nombre_anexo_2").toString()

                    fg.ejecDB(contexto,
                        "INSERT OR IGNORE INTO '301_RESPUESTAS_TEMP' " +
                                "( idtarea ,idformulario , idform ,grpid,sgrpid,idpreg,preg,rta1,useruid) VALUES " +
                                "('" + idtarea + "' , '" + idformulario + "' , '9999' , '0' , '0' , 'GEN_1' , 'Observaciones Generales','0','" + fg.parametro(
                            contexto,
                            "USERUID") + "') , " +
                                "('" + idtarea + "' , '" + idformulario + "' , '9999' , '0' , '0' , 'GEN_2' , 'Autoriza el Tratamiento de mis datos personales','1','" + fg.parametro(
                            contexto,
                            "USERUID") + "') , " +
                                "('" + idtarea + "' , '" + idformulario + "' , '9999' , '0' , '0' , 'GEN_3' , 'Ingrese el Nombre del Cliente','0','" + fg.parametro(
                            contexto,
                            "USERUID") + "') , " +
                                "('" + idtarea + "' , '" + idformulario + "' , '9999' , '0' , '0' , 'GEN_4' , 'Ingrese la Cedula del Cliente','0','" + fg.parametro(
                            contexto,
                            "USERUID") + "') ; "
                    )
                    val regins = fg.getQ1(contexto,
                        "select ifnull(count(*),0) as cr from '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "' and (idpreg='FIRMA_CLI' or idpreg='FIRMA_USER');").toString()
                    if (regins.equals("0")){
                        fg.ejecDB(contexto,
                            "INSERT OR IGNORE INTO '302_FOTOS_RESP_TEMP' " +
                                    "( idtarea ,idformulario , idform ,grpid,sgrpid,idpreg,useruid) VALUES " +
                                    "('" + idtarea + "' , '" + idformulario + "' , '9999' , '0' , '0' , 'FIRMA_CLI' , '" + fg.parametro(
                                contexto,
                                "USERUID"
                            ) + "') , " +
                                    "('" + idtarea + "' , '" + idformulario + "' , '9999' , '0' , '0' , 'FIRMA_USER' , '" + fg.parametro(
                                contexto,
                                "USERUID"
                            ) + "') ; "
                        )
                    }
                    if (ap_anexo1.equals("1")) {
                        fg.ejecDB(contexto,
                            "INSERT OR IGNORE INTO '201_PREGFORM' ( idtarea ,idformulario, idform ,form ) VALUES " +
                                    "('" + idtarea + "' , '" + idformulario + "' , '9991' , '" + nom_anexo1 + "') ;"
                        )
                        fg.ejecDB(contexto,
                            "INSERT OR IGNORE INTO '204_PREGGEN' " +
                                    "( idtarea ,idformulario , idform ,grpid,sgrpid,orden,idpreg,preg,pregtxt,tipo,aplica,fotoap,fotomin,fotomax,fototipo,r1,r2,ap) VALUES " +
                                    "('" + idtarea + "' , '" + idformulario + "' , '9991' , '0' , '0' , '0' , 'ANEXO_1' , '" + nom_anexo1 + "','0','10','1','0','0','0','2','1','3','1') ; "
                        )
                    }
                    if (ap_anexo2.equals("1")) {
                        fg.ejecDB(contexto,
                            "INSERT OR IGNORE INTO '201_PREGFORM' ( idtarea ,idformulario, idform ,form ) VALUES " +
                                    "('" + idtarea + "' , '" + idformulario + "' , '9992' , '" + nom_anexo2 + "') ;"
                        )
                        fg.ejecDB(contexto,
                            "INSERT OR IGNORE INTO '204_PREGGEN' " +
                                    "( idtarea ,idformulario , idform ,grpid,sgrpid,orden,idpreg,preg,pregtxt,tipo,aplica,fotoap,fotomin,fotomax,fototipo,r1,r2,ap) VALUES " +
                                    "('" + idtarea + "' , '" + idformulario + "' , '9992' , '0' , '0' , '0' , 'ANEXO_2' , '" + nom_anexo2 + "','0','10','1','0','0','0','2','1','3','1') ; "
                        )
                    }

                    var cont: Int = 1
                    var tam = contenido?.size as Int
                    for (forma in contenido) {
                        val formae = forma?.get("grupo").toString()
                        val sqlforma =
                            "INSERT OR IGNORE INTO '201_PREGFORM' ( idtarea ,idformulario, idform ,form ) VALUES " +
                                    "('" + idtarea + "' , '" + idformulario + "' , '" + cont.toString() + "' , '" + formae + "') ;"
                        fg.ejecDB(contexto, sqlforma)
                        val grupitems =
                            forma?.get("grupItems") as ArrayList<MutableMap<String, Any?>?>?
                        val itemsgen =
                            forma?.get("items") as ArrayList<MutableMap<String, Any?>?>?
                        var congrp: Int = 1
                        val tamgrp = grupitems?.size as Int
                        if (tamgrp > 0) {
                            for (grupitem in grupitems) {
                                val nombre = grupitem?.get("nombre").toString()
                                fg.ejecDB(contexto,
                                    "INSERT OR IGNORE INTO '202_PREGGRUP' ( idtarea ,idformulario, idform ,grpid , grp ) VALUES " +
                                            "('" + idtarea + "' , '" + idformulario + "' , '" + cont.toString() + "' , '" + congrp.toString() + "' , '" + nombre + "') ; "
                                )
                                val sgrupitems =
                                    grupitem?.get("items") as ArrayList<MutableMap<String, Any?>?>?
                                var consgrp: Int = 1
                                var tamsgrp = sgrupitems?.size as Int
                                for (sgrupitem in sgrupitems) {
                                    val idform_items =
                                        sgrupitem?.get("id_form_items").toString()
                                    val criterio = sgrupitem?.get("criterio").toString()
                                    val label_text = sgrupitem?.get("label_text").toString()
                                    var idform_tipo_calif =
                                        sgrupitem?.get("id_form_tip_calif").toString()
                                    if (idform_tipo_calif.equals("")) { idform_tipo_calif="0"}
                                    var idform_tipo_foto =
                                        sgrupitem?.get("id_form_tip_foto").toString()
                                    if (idform_tipo_foto.equals("")) { idform_tipo_foto="0"}
                                    var idform_tipo_text =
                                        sgrupitem?.get("id_form_tip_text").toString()
                                    if (idform_tipo_text.equals("")) { idform_tipo_text="0"}
                                    val active_check = sgrupitem?.get("active_check").toString()
                                    val label_check_con =
                                        sgrupitem?.get("label_check_con").toString()
                                    val label_check_nocon =
                                        sgrupitem?.get("label_check_nocon").toString()
                                    val label_check_noapli =
                                        sgrupitem?.get("label_check_noapli").toString()
                                    var estado = sgrupitem?.get("estado").toString()
                                    var idcliente = sgrupitem?.get("id_client").toString()
                                    var idselector = sgrupitem?.get("id_selector").toString()
                                    if (idselector.equals("")) { idselector="0"}
                                    var aplicacheck: String? = "0"
                                    var aplicacalif: String? = "0"
                                    var aplicatxt: String? = "0"
                                    var aplicafoto: String? = "0"
                                    var aplicaselector: String? = "0"
                                    var txtobl: String? = "0"
                                    var txtmin: String? = "0"
                                    var txtmax: String? = "0"

                                    if ((idselector != "0")  && (idselector != "")) {
                                        aplicaselector = "1"
                                    }
                                    if (active_check.equals("1")) {
                                        aplicacheck = "1"
                                    }
                                    if ((idform_tipo_calif != "1")  && (idform_tipo_calif != "")) {
                                        aplicacalif = "1"
                                    }
                                    if ((idform_tipo_foto != "1") && (idform_tipo_foto != "0")  && (idform_tipo_foto != "")) {
                                        aplicafoto = "1"
                                    }
                                    if ((idform_tipo_text != "1") && (idform_tipo_text != "0")  && (idform_tipo_text != "")) {
                                        aplicatxt = "1"
                                    }
                                    if (idform_tipo_text.equals("3")) {
                                        txtobl = "1"
                                        txtmin = "3"
                                        txtmax = "250"
                                    }
                                    val cadena = "('" + idtarea +
                                            "' , '" + idformulario +
                                            "' , '" + cont.toString() +
                                            "' , '" + congrp.toString() +
                                            "' , '" + idform_items.toString() +
                                            "' , 'F" + idformulario + "FM" + cont.toString() + "G" + congrp.toString() + "SG" + idform_items.toString()

                                    fg.ejecDB(contexto,
                                        "INSERT OR IGNORE INTO '203_PREGSGRUP' ( idtarea ,idformulario, idform ,grpid ,sgrpid, sgrp ) VALUES " +
                                                "('" + idtarea + "' , '" + idformulario + "' , '" + cont.toString() + "' , '" + congrp.toString() + "' , '" + idform_items.toString() + "' , '" + criterio + "') ; "
                                    )
                                    fg.ejecDB(contexto,
                                        "INSERT OR IGNORE INTO '204_PREGGEN' " +
                                                "( idtarea ,idformulario , idform ,grpid,sgrpid,orden,idpreg,preg,pregtxt,tipo,aplica,fotoap,fotomin,fotomax,fototipo,tipotxt,r1,r2,ap,idselector,campo1,campo2,campo3,labeltext,apcheck,aptxt,apfoto,apsel) VALUES " +
                                                "('" + idtarea +
                                                "' , '" + idformulario +
                                                "' , '" + cont.toString() +
                                                "' , '" + congrp.toString() +
                                                "' , '" + idform_items.toString() +
                                                "' , '" + consgrp.toString() +
                                                "' , 'F" + idformulario + "FM" + cont.toString() + "G" + congrp.toString() + "SG" + idform_items.toString() + "_CHK" + consgrp.toString() +
                                                "' , '" + criterio +
                                                "' , '0','11','1','" + aplicafoto + "','1','5','" + idform_tipo_foto.toString() + "','" + idform_tipo_text.toString() + "','" + txtmin + "','" + txtmax + "','1','" + idselector.toString() +
                                                "','" + label_check_con + "','" + label_check_nocon + "','" + label_check_noapli + "','" + label_text + "','" + aplicacheck + "','" + aplicatxt + "','" + aplicafoto + "','" + aplicaselector + "') ; "
                                    )

                                    consgrp++
                                }
                                congrp++
                            }
                        }

                        var consitem: Int = 1
                        val tamitemg = itemsgen?.size as Int
                        if (tamitemg > 0) {
                            for (itemg in itemsgen) {
                                val idform_items = itemg?.get("id_form_items").toString()
                                val criterio = itemg?.get("criterio").toString()
                                val label_text = itemg?.get("label_text").toString()
                                val idform_tipo_calif =
                                    itemg?.get("id_form_tip_calif").toString()
                                val idform_tipo_foto =
                                    itemg?.get("id_form_tip_foto").toString()
                                val idform_tipo_text =
                                    itemg?.get("id_form_tip_text").toString()
                                val active_check = itemg?.get("active_check").toString()
                                val label_check_con =
                                    itemg?.get("label_check_con").toString()
                                val label_check_nocon =
                                    itemg?.get("label_check_nocon").toString()
                                val label_check_noapli =
                                    itemg?.get("label_check_noapli").toString()
                                var estado = itemg?.get("estado").toString()
                                var idcliente = itemg?.get("id_client").toString()
                                val idselector = itemg?.get("id_selector").toString()
                                var aplicacheck: String? = "0"
                                var aplicacalif: String? = "0"
                                var aplicatxt: String? = "0"
                                var aplicafoto: String? = "0"
                                var aplicaselector: String? = "0"
                                var txtobl: String? = "0"
                                var txtmin: String? = "0"
                                var txtmax: String? = "0"

                                if ((idselector != "0")  && (idselector != "")) {
                                    aplicaselector = "1"
                                }
                                if (active_check.equals("1")) {
                                    aplicacheck = "1"
                                }
                                if ((idform_tipo_calif != "1") && (idform_tipo_calif != "")) {
                                    aplicacalif = "1"
                                }
                                if ((idform_tipo_foto != "1") && (idform_tipo_foto != "0") && (idform_tipo_foto != "")) {
                                    aplicafoto = "1"
                                }
                                if ((idform_tipo_text != "1") && (idform_tipo_text != "0") && (idform_tipo_text != "")) {
                                    aplicatxt = "1"
                                }
                                if (idform_tipo_text.equals("3")) {
                                    txtobl = "1"
                                    txtmin = "3"
                                    txtmax = "250"
                                }
                                val cadena = "('" + idtarea +
                                        "' , '" + idformulario +
                                        "' , '" + cont.toString() +
                                        "' , '" + congrp.toString() +
                                        "' , '" + idform_items.toString() +
                                        "' , 'F" + idformulario + "FM" + cont.toString() + "G" + congrp.toString() + "SG" + idform_items.toString()

                                fg.ejecDB(contexto,
                                    "INSERT OR IGNORE INTO '202_PREGGRUP' ( idtarea ,idformulario, idform ,grpid , grp ) VALUES " +
                                            "('" + idtarea + "' , '" + idformulario + "' , '" + cont.toString() + "' , '" + congrp.toString() + "' , '" + criterio + "') ; "
                                )

                                fg.ejecDB(contexto,
                                    "INSERT OR IGNORE INTO '204_PREGGEN' " +
                                            "( idtarea ,idformulario , idform ,grpid,sgrpid,orden,idpreg,preg,pregtxt,tipo,aplica,fotoap,fotomin,fotomax,fototipo,tipotxt,r1,r2,ap,idselector,campo1,campo2,campo3,labeltext,apcheck,aptxt,apfoto,apsel) VALUES " +
                                            "('" + idtarea +
                                            "' , '" + idformulario +
                                            "' , '" + cont.toString() +
                                            "' , '" + congrp.toString() +
                                            "' , '" + idform_items.toString() +
                                            "' , '" + consitem.toString() +
                                            "' , 'F" + idformulario + "FM" + cont.toString() + "G" + congrp.toString() + "SG" + idform_items.toString() + "_" + consitem.toString() +
                                            "' , '" + criterio +
                                            "' , '0','11','1','" + aplicafoto + "','1','5','" + idform_tipo_foto.toString() + "','" + idform_tipo_text.toString() + "','" + txtmin + "','" + txtmax + "','1','" + idselector.toString() +
                                            "','" + label_check_con + "','" + label_check_nocon + "','" + label_check_noapli + "','" + label_text + "','" + aplicacheck + "','" + aplicatxt + "','" + aplicafoto + "','" + aplicaselector + "') ; "
                                )
                                consitem++
                                congrp++
                            }
                        }
                        cont++
                    }
                }

            }
        }
        calcdistancia(contexto)
    }

    fun calcdistancia(contexto: Context){
        val fg = FuncionesGenerales()
        val conGen = ConsultaGeneral()
        val latitud_ult = fg.parametro(contexto, "ULT_LAT")
        val longitud_ult = fg.parametro(contexto, "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        var canitems = 0
        val queryDIST =
            "SELECT idtarea,gpslat,gpslon FROM '200_TAREAS_DIST' where activa='1' or abierta='1' order by 1 asc"
        val objDIST =
            conGen.queryObjeto2val(contexto, queryDIST, null)

        if (objDIST.isNotEmpty()) {
            for (op in objDIST.indices) {
                var tareasel:String = ""
                var distancia:Double = 0.0
                var dist:Double = 0.0
                try{
                    tareasel = objDIST[op][0].toString()
                    distancia = Tools.calcCrow(
                        latdbl, londbl,
                        objDIST[op][1].toString().toDouble(),
                        objDIST[op][2].toString().toDouble()
                    ) * 1000
                     dist = round(distancia)
                }finally {
                    fg.ejecDB(contexto,"UPDATE '200_TAREAS_DIST' set distancia='$dist' where idtarea='$tareasel'")
                }
                canitems++
            }
        }
    }

    fun resetParametros(contexto: Context) {
        var fg = FuncionesGenerales()
        fg.act_param(contexto, "ULTIMAP", "0")
        fg.act_param(contexto, "NOVEDAD", "0")
        fg.act_param(contexto, "IDNOVEDAD", "0")
        fg.act_param(contexto, "NCLIENTE_ACT", "0")
        fg.act_param(contexto, "HORARIO_ACT", "0")
        fg.act_param(contexto, "FORMULARIO_ACT", "0")
        fg.act_param(contexto, "NFORMULARIO_ACT", "0")
        fg.act_param(contexto, "FORMA_ACT", "0")
        fg.act_param(contexto, "NFORMA_ACT", "0")
        fg.act_param(contexto, "TITULO_ACT", "0")
        fg.act_param(contexto, "GRUPO_ACT", "0")
        fg.act_param(contexto, "NGRUPO_ACT", "0")
        fg.act_param(contexto, "SUBGRUPO_ACT", "0")
        fg.act_param(contexto, "NSUBGRUPO_ACT", "0")
        fg.act_param(contexto, "PREGUNTA_ACT", "0")
        fg.act_param(contexto, "NPREGUNTA_ACT", "0")
        fg.act_param(contexto, "TIPO_PREGUNTA", "0")
        fg.act_param(contexto, "TIPOCARGA", "0")
        fg.act_param(contexto, "ORDEN_ACT", "0")
        fg.act_param(contexto, "OPN_ACT", "0")
        fg.act_param(contexto, "OBS_GEN", "0")
        fg.act_param(contexto, "AUTORIZA", "0")
        fg.act_param(contexto, "TIPO_FIRMA", "0")
        fg.act_param(contexto, "FIRMA_CLI", "0")
        fg.act_param(contexto, "FIRMA_USER", "0")
        fg.act_param(contexto, "NOM_CLI", "0")
        fg.act_param(contexto, "IDENT_CLI", "0")
        fg.act_param(contexto, "AP_CHECK", "0")
        fg.act_param(contexto, "AP_TXT", "0")
        fg.act_param(contexto, "AP_FOTO", "0")
        fg.act_param(contexto, "AP_SELECT", "0")
        fg.act_param(contexto, "RTA1", "0")
        fg.act_param(contexto, "RTA2", "0")
        fg.act_param(contexto, "TXT_VALOR", "0")
        fg.act_param(contexto, "FTOM", "0")
        fg.act_param(contexto, "TIPO_TXT", "0")
        fg.act_param(contexto, "TIPO_FOTO", "0")
    }

    fun historynotes(contexto: Context, nota: String){
        val fg = FuncionesGenerales()
        val so = SincronizarOnline()
        val idtarea = fg.tarea_act(contexto)
        val fecha = fg.fechaActual(1)
        val hora = fg.fechaActual(4)
        val iduser = fg.useruid(contexto).toString()
        val note = "Hora:$hora Usuario:$iduser Nota: $nota"
        val tipouser = "Campo"
        var maxidnote = fg.getQ1(contexto,
            "Select ifnull(max(idnote),0) as idnota from '206_HISTORY_NOTES' where idtarea='$idtarea' and fecha='$fecha'").toInt()
        var contnote = fg.getQ1(contexto,
            "Select count(idnote) as contnota from '206_HISTORY_NOTES' where idtarea='$idtarea' and fecha='$fecha'").toInt()
        if (contnote!=0){
            if (maxidnote != null) {
                maxidnote = maxidnote + 1
            }
        }
        try{
            fg.ejecDB(contexto,
                "INSERT OR IGNORE INTO '206_HISTORY_NOTES' VALUES ('$idtarea','$fecha','$maxidnote','$iduser','$note','$tipouser');")
        }finally {
            val ce = CargarEstados()
            ce.ActualizarhistoryNotes(contexto)
        }
    }
}