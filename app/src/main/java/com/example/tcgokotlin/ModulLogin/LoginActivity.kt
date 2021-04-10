package com.example.tcgokotlin.ModulLogin

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.example.tcgokotlin.ModulMain.MainActivity
import com.example.tcgokotlin.R
import com.example.tcgokotlin.data.model.DataUser
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.utils.AnimationUtils.NotificationAlerter
import com.example.tcgokotlin.utils.Common
import com.example.tcgokotlin.utils.sharedPreferences.SesionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.MetadataChanges
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_update_information.*
import java.io.File
import java.util.*

class LoginActivity : AppCompatActivity() {
    var waitingDialog: AlertDialog? = null
    var carpetaIMG: File? = null
    var fc: MutableMap<String, Any?>? = null
    var images: MutableMap<String, Any?>? = null
    var imagesfc1: MutableMap<String, Any?>? = null
    var imagesfc2: MutableMap<String, Any?>? = null
    var imagesfl1: MutableMap<String, Any?>? = null
    var imagesfl2: MutableMap<String, Any?>? = null
    var imagesfs: String? = null
    var imagesftm: MutableMap<String, Any?>? = null
    var imagesfrs: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setDataBaseFirebase()

        if (checkPermission()) {
            try {
                try {
                    carpetaIMG = File(Environment.getExternalStorageDirectory(), "TCGO_FILES")
                    if (!carpetaIMG!!.exists()) {
                        carpetaIMG!!.mkdirs()
                    }
                } catch (e: java.lang.Exception) {
                    Log.i("Error Creando dirPrin", e.toString())
                } finally {
                    carpetaIMG = File(Environment.getExternalStorageDirectory(),
                        "TCGO_FILES/Databases")
                    if (!carpetaIMG!!.exists()) {
                        carpetaIMG!!.mkdirs()
                    }
                    carpetaIMG = File(Environment.getExternalStorageDirectory(), "TCGO_FILES/IMG")
                    if (!carpetaIMG!!.exists()) {
                        carpetaIMG!!.mkdirs()
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.i("Error Creando carpetas:", e.toString())
                try {
                    CreateTables()
                } catch (e: java.lang.Exception) {
                    Log.i("Error Creando tablas:", e.toString())
                }
            } finally {
                try {
                    CreateTables()
                } catch (e: java.lang.Exception) {
                    Log.i("Error Creando tablas:", e.toString())
                }
            }
            txt_forgot_password.setOnClickListener {
                showDialogForgotPwd()
            }
            btnSigIn.setOnClickListener { initLogin() }
        }
    }

    private fun setDatabaseSQLite() {
        val datadirectory = baseContext.applicationInfo.dataDir
        val dbpath = "$datadirectory/databases/"
        val dbname = "tcgoappdb.db"
        val path = dbpath + dbname
        var sqdb = SQLiteDatabase.openOrCreateDatabase(path, null)
        sqdb.pageSize = 65535
        sqdb.setMaximumSize(sqdb.maximumSize)
        sqdb.setMaxSqlCacheSize(100)
        sqdb.close()
    }

    private fun setDatabaseSQLiteBK() {
        val datadirectory = baseContext.applicationInfo.dataDir
        val dbpath = "$datadirectory/databases/"
        val dbname = "tcgobk.db"
        val path = dbpath + dbname
        var sqdb = SQLiteDatabase.openOrCreateDatabase(path, null)
        sqdb.close()
    }

    private fun CreateTables() {
        try {
            setDatabaseSQLite()
        } finally {
            try {
                setDatabaseSQLiteBK()
            } finally {
                var fg = FuncionesGenerales()
                var bk = Backups()
                bk.ejecbdbk(baseContext,"CREATE TABLE IF NOT EXISTS '999_BACKUP_REGISTRO' (idbackup INTEGER DEFAULT 0, nbackup TEXT DEFAULT '0',restaurada TEXT DEFAULT '0',fcr TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(idbackup))")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '100_DRIVERINFO' ( imei TEXT DEFAULT '0', useruid TEXT DEFAULT '0', nombres TEXT DEFAULT '0', apellidos TEXT DEFAULT '0',drivercc TEXT DEFAULT '0',password TEXT DEFAULT '0', email TEXT DEFAULT '0',estadodriver TEXT DEFAULT '0',idclientw TEXT DEFAULT '0',tipoveh TEXT DEFAULT '0',urlfotoconductor TEXT DEFAULT '0',urlfotoced1 TEXT DEFAULT '0', urlfotoced2 TEXT DEFAULT '0', urlfotolic1 TEXT DEFAULT '0', urlfotolic2 TEXT DEFAULT '0',urlfotoseguro TEXT DEFAULT '0',urlfototecnomec TEXT DEFAULT '0',urlfotoresibo TEXT DEFAULT '0',logeado INTEGER, PRIMARY KEY(useruid));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS params (pa TEXT DEFAULT '0', va TEXT DEFAULT '0', PRIMARY KEY(pa));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '101_REGISTRO' ( useruid TEXT DEFAULT '0', fecha TEXT DEFAULT '0', iniciojornada TEXT DEFAULT '0', finjornada TEXT DEFAULT '0',iniciobreak TEXT DEFAULT '0',finbreak TEXT DEFAULT '0',inicioalmuerzo TEXT DEFAULT '0',finalmuerzo TEXT DEFAULT '0',iniciopermiso TEXT DEFAULT '0',finpermiso TEXT DEFAULT '0',vh1nfoto TEXT DEFAULT '0',vh1hora TEXT DEFAULT '0',vh1lat TEXT DEFAULT '0',vh1lon TEXT DEFAULT '0',vh2nfoto TEXT DEFAULT '0',vh2hora TEXT DEFAULT '0',vh2lat TEXT DEFAULT '0',vh2lon TEXT DEFAULT '0',km1nfoto TEXT DEFAULT '0',km1hora TEXT DEFAULT '0',km1lat TEXT DEFAULT '0',km1lon TEXT DEFAULT '0',km2nfoto TEXT DEFAULT '0',km2hora TEXT DEFAULT '0',km2lat TEXT DEFAULT '0',km2lon TEXT DEFAULT '0',gpslat TEXT DEFAULT '0',gpslon TEXT DEFAULT '0',sinc INTEGER DEFAULT 0,vh1sinc INTEGER DEFAULT 0,vh2sinc INTEGER DEFAULT 0,km1sinc INTEGER DEFAULT 0,km2sinc INTEGER DEFAULT 0,idclientw TEXT DEFAULT '0', PRIMARY KEY(useruid,fecha));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '102_HISTORIAL' ( useruid TEXT DEFAULT '0', idtarea TEXT DEFAULT '0',fecha TEXT DEFAULT '0', idhistorial INTEGER DEFAULT 0,gpslat TEXT DEFAULT '0',gpslon TEXT DEFAULT '0',estado TEXT DEFAULT '0',estadorec TEXT DEFAULT '0', standby TEXT DEFAULT '0',fcr TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,sinc INTEGER DEFAULT 0,idclientw TEXT DEFAULT '0', PRIMARY KEY(useruid,idtarea,fecha,idhistorial));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '103_HISTORIAL_TAREA' ( useruid TEXT DEFAULT '0', idtarea TEXT DEFAULT '0',fecha TEXT DEFAULT 0, rechorain TEXT DEFAULT '0', rechorafin TEXT DEFAULT '0',rechorainret TEXT DEFAULT '0',reckmest TEXT DEFAULT '0',reckmreal TEXT DEFAULT '0',rectiempoest TEXT DEFAULT '0',rectiemporeal TEXT DEFAULT '0',tareatiempoest TEXT DEFAULT '0',tareatiemporeal TEXT DEFAULT '0',gpslat TEXT DEFAULT '0',gpslon TEXT DEFAULT '0', sinc INTEGER DEFAULT 0 , PRIMARY KEY(useruid,idtarea,fecha));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '103_HISTORIAL_NOTAS' ( useruid TEXT DEFAULT '0', idtarea TEXT DEFAULT '0',fecha TEXT DEFAULT 0, idhistnotas INTEGER DEFAULT 0,iduser TEXT DEFAULT '0',nota TEXT DEFAULT '0',tipousuario TEXT DEFAULT '0',fcr TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,sinc INTEGER DEFAULT 0 , PRIMARY KEY(useruid,idtarea,fecha,idhistnotas));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '104_TIPO_SERVICIO' ( idtiposerv TEXT DEFAULT '0', tiposervicio TEXT DEFAULT '0', PRIMARY KEY(idtiposerv));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '105_NO_EXITOSO' ( idnoexitoso TEXT DEFAULT '0', noexitoso TEXT DEFAULT '0', PRIMARY KEY(idnoexitoso));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '106_SELECTOR' ( idselector TEXT DEFAULT '0', selector TEXT DEFAULT '0', PRIMARY KEY(idselector));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '107_SEL_ELEMENTO' ( idselector TEXT DEFAULT '0', idselelemento TEXT DEFAULT '0', selelemento TEXT DEFAULT '0', PRIMARY KEY(idselector,idselelemento));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '108_FORMULARIOS' ( idtarea TEXT DEFAULT '0', idformulario TEXT DEFAULT '0', formulario TEXT DEFAULT '0',descripcion TEXT DEFAULT '0', conse TEXT DEFAULT '0',requi TEXT DEFAULT '0',renderfc TEXT DEFAULT '0',anexo1 TEXT DEFAULT '0',anexo2 TEXT DEFAULT '0',titdoc TEXT DEFAULT '0',titobserv TEXT DEFAULT '0',campo1 TEXT DEFAULT '0',campo2 TEXT DEFAULT '0',campo3 TEXT DEFAULT '0', sinc INTEGER DEFAULT 0, PRIMARY KEY(idtarea,idformulario));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '109_ESTADOS' (idestado TEXT DEFAULT '0', estado TEXT DEFAULT '0', PRIMARY KEY(idestado));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '110_TIPONOV' (idtiponov TEXT DEFAULT '0', tiponov TEXT DEFAULT '0', PRIMARY KEY(idtiponov));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '200_TAREAS' ( idtarea TEXT DEFAULT '0',tarea TEXT DEFAULT '0',fecha TEXT DEFAULT '0', tiposervicio TEXT DEFAULT '0', idclient TEXT DEFAULT '0', nomclient TEXT DEFAULT '0', pais TEXT DEFAULT '0',dpto TEXT DEFAULT '0',ciudad TEXT DEFAULT '0',direccion TEXT DEFAULT '0',tel1 TEXT DEFAULT '0',tel2 TEXT DEFAULT '0',email TEXT DEFAULT '0', enviarcorreo TEXT DEFAULT '0', eval TEXT DEFAULT '0', cdn TEXT  DEFAULT '0',estado TEXT DEFAULT '0',novedad TEXT DEFAULT '0',tiponovedad TEXT DEFAULT '0',standby TEXT DEFAULT '0',prioridad TEXT DEFAULT '0',tipo TEXT DEFAULT '0',campo1 TEXT DEFAULT '0',campo2 TEXT DEFAULT '0',campo3 TEXT DEFAULT '0',nota TEXT DEFAULT '0',valkey TEXT DEFAULT '0',horaini TEXT DEFAULT '0',horario TEXT DEFAULT '0',horafin TEXT DEFAULT '0',useruid TEXT DEFAULT '0',typeservice TEXT DEFAULT '0',estimado TEXT DEFAULT '0',motivoreag TEXT DEFAULT '0',motivonoex TEXT DEFAULT '0', activa TEXT DEFAULT '0', abierta TEXT DEFAULT '0', ejecfechain TEXT DEFAULT '0', ejecfechafin TEXT DEFAULT '0', gpslat TEXT DEFAULT '0', gpslon TEXT DEFAULT '0', sinc INTEGER DEFAULT 0,  PRIMARY KEY(idtarea,fecha));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '200_TAREAS_DIST' ( idtarea TEXT DEFAULT '0',abierta TEXT DEFAULT '0',activa TEXT DEFAULT '0',distancia REAL, gpslat TEXT DEFAULT '0', gpslon TEXT DEFAULT '0',  PRIMARY KEY(idtarea));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '201_PREGFORM' ( idtarea TEXT DEFAULT '0', idformulario TEXT DEFAULT '0', idform TEXT DEFAULT '0',form TEXT DEFAULT '0', ev INTEGER DEFAULT '0', cdn TEXT DEFAULT '0', sinc INTEGER DEFAULT 0 ,PRIMARY KEY(idtarea,idformulario,idform));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '202_PREGGRUP' ( idtarea TEXT DEFAULT '0', idformulario TEXT DEFAULT '0', idform TEXT DEFAULT '0',grpid TEXT DEFAULT '0', grp TEXT DEFAULT '0', ev TEXT DEFAULT '0', cdn TEXT DEFAULT '0', sinc INTEGER DEFAULT 0, PRIMARY KEY(idtarea,idformulario,idform,grpid));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '203_PREGSGRUP' ( idtarea TEXT DEFAULT '0', idformulario TEXT DEFAULT '0', idform TEXT DEFAULT '0',grpid TEXT DEFAULT '0',sgrpid TEXT DEFAULT '0', sgrp TEXT DEFAULT '0', ev TEXT DEFAULT '0', cdn TEXT DEFAULT '0', sinc INTEGER DEFAULT 0, PRIMARY KEY(idtarea,idformulario,idform,grpid,sgrpid));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '204_PREGGEN'  ( idtarea TEXT DEFAULT '0', idformulario TEXT DEFAULT '0', idform TEXT DEFAULT '0',grpid TEXT DEFAULT '0',sgrpid TEXT DEFAULT '0',orden INTEGER DEFAULT 0,idpreg TEXT DEFAULT '0',preg TEXT DEFAULT '0',pregtxt TEXT DEFAULT '0',idselector TEXT DEFAULT '0',tipotxt TEXT DEFAULT '0',cnd1 TEXT DEFAULT '0',cnd2 TEXT DEFAULT '0',cnd3 TEXT DEFAULT '0',cnd4 TEXT DEFAULT '0',eval TEXT DEFAULT '0',evalfoto TEXT DEFAULT '0',tipo TEXT DEFAULT '0',aplica TEXT DEFAULT '0',fotoap TEXT DEFAULT '0',fotoayuda TEXT DEFAULT '0',fotomin TEXT DEFAULT '0',fotomax TEXT DEFAULT '0',fototipo TEXT DEFAULT '0',r1 TEXT DEFAULT '0',r2 TEXT DEFAULT '0',ap TEXT DEFAULT 0,campo1 TEXT DEFAULT '0',campo2 TEXT DEFAULT '0',campo3 TEXT DEFAULT '0',labeltext TEXT DEFAULT '0',apcheck TEXT DEFAULT 0,aptxt TEXT DEFAULT 0,apfoto TEXT DEFAULT 0,apsel TEXT DEFAULT 0, sinc INTEGER DEFAULT 0,PRIMARY KEY(idtarea,idformulario,idform,grpid,sgrpid,idpreg));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '205_PREGGEND' ( idtarea TEXT DEFAULT '0', idformulario TEXT DEFAULT '0', idform TEXT DEFAULT '0',grpid TEXT DEFAULT '0',sgrpid TEXT DEFAULT '0',idpreg TEXT DEFAULT '0',opn INTEGER,opt TEXT DEFAULT '0',cnd TEXT DEFAULT '0', sinc INTEGER DEFAULT 0,PRIMARY KEY(idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '206_HISTORY_NOTES' (idtarea TEXT DEFAULT '0', fecha TEXT DEFAULT '0', idnote INTEGER DEFAULT 0, iduser TEXT DEFAULT '0', nota TEXT DEFAULT '0', tipouser TEXT DEFAULT '0', PRIMARY KEY(idtarea,fecha,idnote));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '301_RESPUESTAS' (idtarea TEXT NOT NULL, idformulario TEXT NOT NULL, idform TEXT NOT NULL,grpid TEXT NOT NULL,sgrpid TEXT NOT NULL,idpreg TEXT NOT NULL,preg TEXT DEFAULT '0',opn INTEGER DEFAULT 0,opt TEXT DEFAULT '0',rta1 TEXT DEFAULT '0',rta2 TEXT DEFAULT '0',obs TEXT DEFAULT '0',gpslat TEXT DEFAULT '0',gpslon TEXT DEFAULT '0',useruid TEXT DEFAULT '0',idclientw TEXT DEFAULT '0',fecha TEXT DEFAULT '0',fcr TIMESTAMP DEFAULT CURRENT_TIMESTAMP, sinc INTEGER DEFAULT 0 , PRIMARY KEY(idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '301_RESPUESTAS_TEMP' (idtarea TEXT NOT NULL, idformulario TEXT NOT NULL, idform TEXT NOT NULL,grpid TEXT NOT NULL,sgrpid TEXT NOT NULL,idpreg TEXT NOT NULL,preg TEXT DEFAULT '0',opn INTEGER DEFAULT 0,opt TEXT DEFAULT '0',rta1 TEXT DEFAULT '0',rta2 TEXT DEFAULT '0',obs TEXT DEFAULT '0',gpslat TEXT DEFAULT '0',gpslon TEXT DEFAULT '0',useruid TEXT DEFAULT '0',fcr TIMESTAMP DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY(idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '302_FOTOS_RESP' (idtarea TEXT DEFAULT '0', idformulario TEXT DEFAULT '0', idform TEXT DEFAULT '0',grpid TEXT DEFAULT '0',sgrpid TEXT DEFAULT '0',idpreg TEXT DEFAULT '0',opn INTEGER DEFAULT 0,idfoto TEXT DEFAULT '0',nfoto TEXT DEFAULT '0',gpslat TEXT DEFAULT '0',gpslon TEXT DEFAULT '0',useruid TEXT DEFAULT '0',idclientw TEXT DEFAULT '0',fecha TEXT DEFAULT '0',fcr TIMESTAMP DEFAULT CURRENT_TIMESTAMP , sinc INTEGER DEFAULT 0 , sincf INTEGER DEFAULT 0 , PRIMARY KEY(idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto));")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '302_FOTOS_RESP_TEMP' (idtarea TEXT DEFAULT '0', idformulario TEXT DEFAULT '0', idform TEXT DEFAULT '0',grpid TEXT DEFAULT '0',sgrpid TEXT DEFAULT '0',idpreg TEXT DEFAULT '0',opn INTEGER DEFAULT 0,idfoto TEXT DEFAULT '0',nfoto TEXT DEFAULT '0',gpslat TEXT DEFAULT '0',gpslon TEXT DEFAULT '0',useruid TEXT DEFAULT '0',fcr TIMESTAMP DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY(idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto));")
                fg.ejecDB(baseContext,
                    "CREATE VIEW IF NOT EXISTS '005_FOTOS_PREG' AS select '302_FOTOS_RESP_TEMP'.* from '302_FOTOS_RESP_TEMP' where idtarea in (select va from params where pa='TAREA_ACT') AND idformulario in (select va from params where pa='FORMULARIO_ACT') AND idform in (select va from params where pa='FORMA_ACT') AND grpid in (select va from params where pa='GRUPO_ACT') AND sgrpid in (select va from params where pa='SUBGRUPO_ACT') AND idpreg in (select va from params where pa='PREGUNTA_ACT') AND opn in (select va from params where pa='OPN_ACT');")
                fg.ejecDB(baseContext,
                    "CREATE TABLE IF NOT EXISTS '400_SINCRONIZAR' (orden INTEGER DEFAULT 0,ntabla TEXT DEFAULT '0',ncampo TEXT DEFAULT '0',PRIMARY KEY(orden));")
                fg.ejecDB(baseContext,
                    "CREATE VIEW IF NOT EXISTS '000_PREGUNTAS' AS select '204_PREGGEN'.idtarea , '204_PREGGEN'.idformulario , '204_PREGGEN'.idform ,'204_PREGGEN'.grpid,'204_PREGGEN'.sgrpid,'204_PREGGEN'.idpreg,'204_PREGGEN'.preg,ifnull('205_PREGGEND'.opn,'0') as opn,ifnull('205_PREGGEND'.opt,'0') as opt from '204_PREGGEN' LEFT JOIN '205_PREGGEND' on '204_PREGGEN'.idtarea = '205_PREGGEND'.idtarea and  '204_PREGGEN'.idformulario = '205_PREGGEND'.idformulario and  '204_PREGGEN'.idform = '205_PREGGEND'.idform and  '204_PREGGEN'.grpid = '205_PREGGEND'.grpid and  '204_PREGGEN'.sgrpid = '205_PREGGEND'.sgrpid and  '204_PREGGEN'.idpreg = '205_PREGGEND'.idpreg WHERE tipo <> '8' and tipo <> '9' and tipo <> '10' order by 1,2,3,4,5,6,8 ASC;")
                fg.ejecDB(baseContext,
                    "CREATE VIEW IF NOT EXISTS '001_SELECTORES' AS select '204_PREGGEN'.idtarea , '204_PREGGEN'.idformulario , '204_PREGGEN'.idform ,'204_PREGGEN'.grpid,'204_PREGGEN'.sgrpid,'204_PREGGEN'.idpreg,'107_SEL_ELEMENTO'.idselector,'107_SEL_ELEMENTO'.idselelemento,'107_SEL_ELEMENTO'.selelemento,(select va from params where pa='USERUID') as uid from '204_PREGGEN' JOIN '107_SEL_ELEMENTO' on '204_PREGGEN'.idselector = '107_SEL_ELEMENTO'.idselector order by 1,2,3,4,5,6,7,8 ASC;")
                fg.ejecDB(baseContext,
                    "CREATE VIEW IF NOT EXISTS '002_PREGUNTAS' AS  select '204_PREGGEN'.*  from '204_PREGGEN'  where idtarea in (select va from params where pa='TAREA_ACT') AND idformulario in (select va from params where pa='FORMULARIO_ACT') AND idform in (select va from params where pa='FORMA_ACT') AND grpid in (select va from params where pa='GRUPO_ACT') AND sgrpid in (select va from params where pa='SUBGRUPO_ACT');")
                fg.ejecDB(baseContext,
                    "CREATE VIEW IF NOT EXISTS '002_PREGUNTASB' AS  select '204_PREGGEN'.*  from '204_PREGGEN'  where idtarea in (select va from params where pa='TAREA_ACT') AND idformulario in (select va from params where pa='FORMULARIO_ACT') AND idform in (select va from params where pa='FORMA_ACT') AND grpid in (select va from params where pa='GRUPO_ACT') AND sgrpid in (select va from params where pa='SUBGRUPO_ACT') AND idpreg in (select va from params where pa='PREGUNTA_ACT');")
                fg.ejecDB(baseContext,
                    "CREATE VIEW IF NOT EXISTS '002_PREGUNTASC' AS select '204_PREGGEN'.*  from '204_PREGGEN'  where idtarea in (select va from params where pa='TAREA_ACT') AND idformulario in (select va from params where pa='FORMULARIO_ACT') AND idform in (select va from params where pa='FORMA_ACT') AND sgrpid in (select va from params where pa='SUBGRUPO_ACT');")
                fg.ejecDB(baseContext,
                    "CREATE VIEW IF NOT EXISTS '003_PREGUNTSF' AS  select '204_PREGGEN'.*  from '204_PREGGEN'  where idtarea in (select va from params where pa='TAREA_ACT') AND idformulario in (select va from params where pa='FORMULARIO_ACT') AND grpid in (select va from params where pa='GRUPO_ACT') AND sgrpid in (select va from params where pa='SUBGRUPO_ACT');")
                fg.ejecDB(baseContext,
                    "CREATE VIEW IF NOT EXISTS '004_PREGUNTASD' AS select '205_PREGGEND'.* from '205_PREGGEND' where idtarea in (select va from params where pa='TAREA_ACT') AND idformulario in (select va from params where pa='FORMULARIO_ACT') AND idform in (select va from params where pa='FORMA_ACT') AND grpid in (select va from params where pa='GRUPO_ACT') AND sgrpid in (select va from params where pa='SUBGRUPO_ACT') AND idpreg in (select va from params where pa='PREGUNTA_ACT');")
                fg.ejecDB(baseContext,
                    "INSERT OR IGNORE INTO params (pa) values ('USER') , ('PASS') ,('USERUID') , ('FECHA_ACT') , ('VERSION') , ('ULTIMAP') , ('TAREA_ACT'), ('NTAREA_ACT'), ('CLIENTEID'), ('CLIENTE_ACT') , ('NCLIENTE_ACT') , ('HORARIO_ACT') , ('FORMULARIO_ACT') , ('NFORMULARIO_ACT') , ('FORMA_ACT') , ('NFORMA_ACT') , ('TIPO_GRP') , ('TITULO_ACT')  ,  ('GRUPO_ACT') , ('NGRUPO_ACT')  , ('SUBGRUPO_ACT') , ('NSUBGRUPO_ACT') , ('PREGUNTA_ACT') , ('NPREGUNTA_ACT') , ('IDDRIVER') , ('TIPO_PREGUNTA') , ('TOTALFOTOSMAX') , ('TIPOCARGA') , ('ORDEN_ACT') ,  ('IDPREG_ACT') , ('ESTADO_ACT') , ('ESTADOREC_ACT') , ('OPN_ACT'), ('OBS_GEN'), ('AUTORIZA'), ('TIPO_FIRMA'), ('FIRMA_CLI'), ('FIRMA_USER'), ('NOM_CLI'), ('IDENT_CLI'), ('AP_CHECK'), ('AP_TXT'), ('AP_FOTO'), ('AP_SELECT'), ('RTA1'), ('TXT_VALOR'), ('FTOM'), ('RTA2'), ('TIPO_TXT'), ('TIPO_FOTO'), ('IDFOTO_ACT'), ('IDREQ_ACT'), ('AP_ANEXO1'), ('AP_ANEXO2'), ('EV_ANEXO1'), ('EV_ANEXO2'), ('EXI_NOEXIT'), ('NOVEDAD'), ('IDNOVEDAD'), ('ULT_LAT'), ('ULT_LON'), ('TAREA_LAT'), ('TAREA_LON'), ('CAMPOFENC'), ('RANGOMTS'), ('ERROR_101'), ('FOTOSCLI'), ('TIPO_FOTO'), ('IDCLIENTW'), ('TAREAREC_ANT'), ('CANTFOTO_ANEXOS');")
                fg.ejecDB(baseContext, "UPDATE params set va = '40' where pa='FOTOSCLI'")
                fg.ejecDB(baseContext, "UPDATE params set va = '500' where pa='RANGOMTS'")
                fg.ejecDB(baseContext,
                    "INSERT OR IGNORE INTO '109_ESTADOS' values ('0','Sin Iniciar Recorrido') , ('1','En recorrido') , ('2','En tarea') , ('3','Reporte Demora') , ('4','Recorrido Reanudado') , ('5','Recorrido Cancelado'), ('6','Finalizado');")
                fg.ejecDB(baseContext,
                    "INSERT OR IGNORE INTO '400_SINCRONIZAR' (orden,ntabla,ncampo) VALUES (1,'200_TAREAS','idtarea') , (2,'201_PREGFORM','idtarea') , (3,'202_PREGGRUP','idtarea') , (4,'203_PREGSGRUP','idtarea') , (5,'204_PREGGEN','idtarea') , (6,'205_PREGGEND','idtarea') ,(7,'301_RESPUESTAS','idtarea') , (8,'302_FOTOS_RESP','idtarea'), (9,'108_FORMULARIOS','idtarea');")
            }
        }
    }

    private fun guardarDriver() {
        var fg = FuncionesGenerales()
        fc = Common.documentUser?.get("fotoConductor") as MutableMap<String, Any?>?
        images = Common.documentUser?.get("images") as MutableMap<String, Any?>?
        imagesfc1 = images?.get("fotoCedulal1") as MutableMap<String, Any?>?
        imagesfc2 = images?.get("fotoCedulal2") as MutableMap<String, Any?>?
        imagesfl1 = images?.get("fotoLicencial1") as MutableMap<String, Any?>?
        imagesfl2 = images?.get("fotoLicencial2") as MutableMap<String, Any?>?
        imagesfs = images?.get("fotoSeguro").toString()
        imagesftm = images?.get("fotoTecnicomec") as MutableMap<String, Any?>?
        imagesfrs = images?.get("fotoresibo").toString()
        fg.act_param(baseContext, "USERUID", Common.documentUser?.get("UserUID").toString())
        fg.act_param(baseContext, "IDCLIENTW", Common.documentUser?.get("idClientw").toString())
        fg.act_param(baseContext, "FECHA_ACT", Common.formatDate.format(Date()) as String)
        fg.ejecDB(baseContext, "INSERT OR IGNORE INTO '100_DRIVERINFO' VALUES ('" +
                Common.documentUser?.get("IMEI").toString() + "' , '" +
                Common.documentUser?.get("UserUID").toString() + "' , '" +
                Common.documentUser?.get("nombres").toString() + "' , '" +
                Common.documentUser?.get("apellidos").toString() + "' , '" +
                Common.documentUser?.get("DriverCC").toString() + "' , '" +
                Common.documentUser?.get("password").toString() + "' , '" +
                Common.documentUser?.get("email").toString() + "' , '" +
                Common.documentUser?.get("estadoDriver").toString() + "' , '" +
                Common.documentUser?.get("idClientew").toString() + "' , '" +
                Common.documentUser?.get("typeVeh").toString() + "' , '" +
                fc?.get("url").toString() + "' , '" +
                imagesfc1?.get("url").toString() + "' , '" +
                imagesfc2?.get("url").toString() + "' , '" +
                imagesfl1?.get("url").toString() + "' , '" +
                imagesfl2?.get("url").toString() + "' , '" +
                imagesfs + "' , '" +
                imagesftm?.get("url").toString() + "' , '" +
                imagesfrs + "' , " +
                " 1 ); ")
        fg.ejecDB(baseContext,
            "INSERT OR IGNORE INTO '101_REGISTRO' (useruid,fecha,idclientw) VALUES ('" + Common.documentUser?.get(
                "UserUID")
                .toString() + "','" + fg.fechaActual(1) + "','" + Common.documentUser?.get("idClientw")
                .toString() + "');")
        val bk = Backups()
        bk.backupdDatabase(baseContext)

    }

    private fun setDataBaseFirebase() {
        try {
            Common.versionCode = this.packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        Common.auth = FirebaseAuth.getInstance()
        Common.setDbSettings()
        Common.mContext = this
        Common.mLayoutInflater = layoutInflater
        Common.versionListener()
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
            Log.e("tcgoPermisos", "ACCESS_COARSE_LOCATION")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
            Log.e("tcgoPermisos", "ACCESS_FINE_LOCATION")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
            Log.e("tcgoPermisos", "WRITE_EXTERNAL_STORAGE")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                0
            )
            Log.e("tcgoPermisos", "READ_PHONE_STATE")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 0)
            Log.e("tcgoPermisos", "INTERNET")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
            Log.e("tcgoPermisos", "CAMERA")
            return false
        }
        return true
    }

    @SuppressLint("LongLogTag")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            0 -> {
                if (checkPermission()) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun showDialogForgotPwd() {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.forget_your_password_single))
        alertDialog.setMessage(getString(R.string.please_entry_your_email))
        val inflater = this.layoutInflater
        val forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_pwd, null)
        val edtEmail = forgot_pwd_layout.findViewById<View>(R.id.edtEmail) as TextView
        val btnCancelar = forgot_pwd_layout.findViewById<Button>(R.id.btnCancelar)
        val btnReestablecer = forgot_pwd_layout.findViewById<Button>(R.id.btnReestablecer)
        alertDialog.setView(forgot_pwd_layout)
        val show = alertDialog.show()
        btnCancelar.setOnClickListener { show.dismiss() }
        btnReestablecer.setOnClickListener {
            if (edtEmail.text.isNotEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(edtEmail.text.toString().trim()).matches()) {
                    waitingDialog = SpotsDialog.Builder().setContext(this).build()
                    waitingDialog?.show()
                    sendEmailRecoveryPassword(waitingDialog, show, edtEmail)
                } else {
                    edtEmail.error = getString(R.string.email_is_not_valid)
                }
            } else {
                edtEmail.error = getString(R.string.this_field_is_required)
            }
        }
    }

    private fun sendEmailRecoveryPassword(
        waitingDialog: AlertDialog?, show: androidx.appcompat.app.AlertDialog, edtEmail: TextView,
    ) {
        Common.auth?.sendPasswordResetEmail(edtEmail.text.toString().trim { it <= ' ' })
            ?.addOnCompleteListener {
                show.dismiss()
                waitingDialog?.dismiss()
                NotificationAlerter.createAlert(getString(R.string.link_restore_password_sended),
                    this)
            }?.addOnFailureListener {
            show.dismiss()
            waitingDialog?.dismiss()
            NotificationAlerter.createAlertError(getString(R.string.not_allow_send_email_restore),
                this)
        }
    }

    private fun initLogin() {
        btnSigIn.isEnabled = false
        waitingDialog = SpotsDialog.Builder().setContext(this).build()
        waitingDialog?.show()
        if (validateFields(et_userLogin, et_passwordLogin)) {
            btnSigIn.isEnabled = true
            queryLogin(et_userLogin, et_passwordLogin, waitingDialog)
        } else {
            btnSigIn.isEnabled = true
            waitingDialog?.dismiss()
        }
    }

    private fun validateFields(edtEmail: TextView?, edtPassword: TextView?): Boolean {
        var isValid = true
        if (TextUtils.isEmpty(edtEmail?.text.toString())) {
            edtEmail?.error = getString(R.string.please_entry_number_id)
            isValid = false
        }
        if (TextUtils.isEmpty(edtPassword?.text.toString())) {
            edtPassword?.error = getString(R.string.please_entry_password)
            isValid = false
        } else {
            if (edtPassword?.text.toString().length < 6) {
                edtPassword?.error = getString(R.string.passwor_too_short)
                isValid = false
            }
        }
        return isValid
    }

    private fun queryLogin(edtEmail: TextView?, edtPassword: TextView?, dialog: AlertDialog?) {
        Common.dbDriversInformation = Common.db?.collection("DriversInformation")
        try {
            Common.listenerRegDriversInformation?.remove()
        } catch (e: Exception) {
        }
        Common.listenerRegDriversInformation = Common.dbDriversInformation?.whereEqualTo("driverCC",
            edtEmail?.text.toString())
            ?.addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (querySnapshot?.isEmpty == true) {
                    dialog?.dismiss()
                    NotificationAlerter.createAlertError(getString(R.string.user_is_not_registered),
                        this)
                    return@addSnapshotListener
                } else {
                    for (change in querySnapshot?.documentChanges ?: mutableListOf()) {
                        Common.auth?.signInWithEmailAndPassword(change.document["email"].toString(),
                            edtPassword?.text.toString())?.addOnSuccessListener {
                            ingresar()
                        }?.addOnFailureListener {
                            dialog?.dismiss()
                            NotificationAlerter.createAlertError(getString(R.string.please_verify_your_conection_internet),
                                this)
                        }
                        Common.documentUser = change.document.data
                        Common.documentUser?.put("key", querySnapshot?.documents?.get(0)?.id ?: 0)
                    }
                }
            }
    }

    private fun setDataUser() {
        val dataUser = DataUser(Common.auth?.currentUser?.uid, Common.auth?.currentUser?.email)
        val sm = SesionManager(this)
        sm.setDataUser(dataUser)

    }

    private fun ingresar() {
        try {
            if (Common.documentUser?.get("IMEI") != null && Common.documentUser?.get("IMEI") != "") {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                if (Common.documentUser?.get("IMEI").toString() == Common.getDeviceId(this)) {
                    try {
                        guardarDriver()
                    } finally {
                        setDataUser()
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                } else {
                    waitingDialog?.dismiss()
                    NotificationAlerter.createAlertError(getString(R.string.not_login_this_device),
                        this)
                }
            } else {
                val upIMEI: MutableMap<String, Any> = HashMap()
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                upIMEI["IMEI"] = Common.getDeviceId(this)
                Common.dbDriversInformation?.document(Common.documentUser?.get("key").toString())
                    ?.update(
                        upIMEI)
                try {
                    guardarDriver()
                } finally {
                    setDataUser()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            val upIMEI: MutableMap<String, Any> = HashMap()
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            upIMEI["IMEI"] = Common.getDeviceId(this)
            Common.dbDriversInformation?.document(Common.documentUser?.get("key").toString())
                ?.update(
                    upIMEI)
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}


