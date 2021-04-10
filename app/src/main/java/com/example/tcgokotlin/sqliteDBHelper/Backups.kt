package com.example.tcgokotlin.sqliteDBHelper

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.tcgokotlin.data.model.MenuFM
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Backups() {
    private var sd: File? = null
    private var cini: File? = null
    private var queryActVal: String? = null
    private var flujo: String? = null
    private val sourceDBName = "tcgoappdb.db"
    private val sourceDBBK = "tcgobk.db"
    private var canitems = 0
    private lateinit var listabk: Array<String?>

    fun backupdDatabase(contexto: Context) {
        try {
            try {
                cini = File(Environment.getExternalStorageDirectory(), "/TCGO_FILES/Databases/")
                if (!cini!!.exists()) {
                    cini!!.mkdirs()
                }
            } catch (exp: Exception) {
                Log.i("No CREATE DATABASESMKT:", exp.toString())
            } finally {
                try {
                    sd = File(
                        Environment.getExternalStorageDirectory()
                            .toString() + "/TCGO_FILES/Databases/",
                    )
                    if (!sd!!.exists()) {
                        sd!!.mkdirs()
                    }
                } catch (exp: Exception) {
                    Log.i("NO CREATE useruid:", exp.toString())
                } finally {
                    val targetDBName = "tcgoapp_" + fechaActual(2) + ".db"
                    var currentDBPath = ""
                    var backupDBPath = ""
                    val maxidbk = getQ1(contexto,
                        "Select ifnull(max(idbackup),0) as mib from '999_BACKUP_REGISTRO'")
                    val conidbk = getQ1(contexto,
                        "Select ifnull(count(idbackup),0) as mib from '999_BACKUP_REGISTRO'")

                    if (conidbk == "30"){
                        Borrarbackup_viejos(contexto)
                    }
                    val sqlquerryInsert =
                        "INSERT OR IGNORE INTO '999_BACKUP_REGISTRO' (idbackup,nbackup) VALUES " +
                                "(" + (maxidbk.toInt() + 1) + ",'" + targetDBName + "');"
                    try {
                        if (sd!!.canWrite()) {
                            try {
                                val now = Date()
                                currentDBPath =
                                    contexto.applicationInfo.dataDir + "/databases/" + sourceDBName
                                val dateFormat = fechaActual(2)
                                backupDBPath = targetDBName
                                val currentDB = File(currentDBPath)
                                if (currentDB.exists()){
                                val backupDB = File(sd, backupDBPath)
                                Log.i("backup", "backupDB=" + backupDB.absolutePath)
                                Log.i("backup", "sourceDB=" + currentDB.absolutePath)
                                val src = FileInputStream(currentDB).channel
                                val dst = FileOutputStream(backupDB).channel
                                dst.transferFrom(src, 0, src.size())
                                ejecbdbk(contexto, sqlquerryInsert)
                                src.close()
                                dst.close()
                                }else{
                                    restoreDB(contexto,0)
                                }
                            } finally {
                                val fullPath =
                                    Environment.getExternalStorageDirectory()
                                        .toString() + "/TCGO_FILES/Databases/"
                                val file = File(fullPath, targetDBName)
                                if (!(file.exists())) {
                                    val queryDelBK =
                                        "Delete from '999_BACKUP_REGISTRO' where nbackup='$targetDBName'"
                                    ejecbdbk(contexto, queryDelBK)
                                }
                            }
                        }
                    } finally {

                    }
                }

            }
        } catch (e: Exception) {
            Log.i("Backup Fallido", e.toString())
        }
    }

    fun restoreDB(contexto: Context, contador: Int): Boolean {
        var estado: Boolean = true
        try {
            try {
                cini = File(Environment.getExternalStorageDirectory(), "/TCGO_FILES/Databases/")
                if (!cini!!.exists()) {
                    cini!!.mkdirs()
                }
            } catch (exp: Exception) {
                Log.i("No CREATE DATABASESMKT:", exp.toString())
            } finally {
                try {
                    sd = File(
                        Environment.getExternalStorageDirectory()
                            .toString() + "/TCGO_FILES/Databases/",
                    )
                    if (!sd!!.exists()) {
                        sd!!.mkdirs()
                    }
                } catch (exp: Exception) {
                    Log.i("NO CREATE useruid:", exp.toString())
                } finally {
                    var currentDBPath = ""
                    var backupDBPath = ""
                    val maxidbk = getQ1(contexto,
                        "Select ifnull(max(idbackup),0) as mib from '999_BACKUP_REGISTRO'")
                    var maxint = maxidbk.toInt()
                    maxint = maxidbk.toInt() - contador
                    val nombk = getQ1(contexto,
                        "Select ifnull(nbackup,0) as mib from '999_BACKUP_REGISTRO' where idbackup='${maxint.toString()}'")
                    val targetDBName = nombk
                    val sqlquerryInsert =
                        "UPDATE '999_BACKUP_REGISTRO' restaurada=1  where idbackup='${maxint.toString()}'"
                    if (sd!!.canWrite()) {
                        val now = Date()
                        currentDBPath =
                            contexto.applicationInfo.dataDir + "/databases/" + sourceDBName
                        val dateFormat = fechaActual(2)
                        backupDBPath = targetDBName
                        val currentDB = File(currentDBPath)
                        val backupDB = File(sd, backupDBPath)
                        Log.i("backup", "backupDB=" + currentDB.absolutePath)
                        Log.i("backup", "sourceDB=" + backupDB.absolutePath)
                        val src = FileInputStream(backupDB).channel
                        val dst = FileOutputStream(currentDB).channel
                        try {
                            dst.transferFrom(src, 0, src.size())
                        } catch (e: Exception) {
                            estado = false
                            Log.i("No se pudo restaurar DBSQL:", e.toString())
                        } finally {
                            estado = true
                            ejecbdbk(contexto, sqlquerryInsert)
                        }
                        src.close()
                        dst.close()
                    }
                }
            }
        } catch (e: Exception) {
            Log.i("Backup Fallido", e.toString())
        }
        return estado
    }

    fun ejecbdbk(contexto: Context, sql: String) {
        val databasef = SQLiteDatabase.openDatabase(contexto.getDatabasePath(sourceDBBK).toString(),
            null,
            SQLiteDatabase.OPEN_READWRITE)
        try {
            databasef.execSQL(sql)
        } catch (e: SQLiteException) {
            Toast.makeText(contexto,
                "No se pudo ejecutar la sentencia SQL: $sql",
                Toast.LENGTH_LONG).show()
        }
        if (databasef.isOpen) databasef.close()
    }

    fun fechaActual(acc: Int): String {
        var fecha = ""
        var fechaml = ""
        if (acc == 1) {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = Date()
            fecha = dateFormat.format(date)
        } else if (acc == 2) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault())
            val date = Date()
            val fechaTemp = dateFormat.format(date)
            var st = StringTokenizer(fechaTemp)
            val fechaT = st.nextToken()
            val horaT = st.nextToken()
            st = StringTokenizer(fechaT, "-")
            fecha = st.nextToken() + st.nextToken() + st.nextToken()
            st = StringTokenizer(horaT, ":")
            fecha += st.nextToken() + st.nextToken() + st.nextToken() + st.nextToken()
        } else if (acc == 3) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date()
            fecha = dateFormat.format(date)
        } else if (acc == 4) {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date()
            fecha = dateFormat.format(date)
        }
        return fecha
    }

    @Throws(SQLException::class)
    fun rawQuery(contexto: Context, sqlQuery: String, whereV: Array<String?>?): Cursor {
        val databasef = SQLiteDatabase.openDatabase(contexto.getDatabasePath(sourceDBBK).toString(),
            null,
            SQLiteDatabase.OPEN_READWRITE)
        val cursordb = databasef.rawQuery(sqlQuery, whereV)
        if (databasef.isOpen) databasef.close()
        return cursordb
    }

    fun QueryObjeto(
        cont: Context,
        SQLQuery: String,
        whereV: Array<String>?,
    ): java.util.ArrayList<java.util.ArrayList<String?>?>? {
        val databasef = SQLiteDatabase.openDatabase(cont.getDatabasePath(sourceDBBK).toString(),
            null,
            SQLiteDatabase.OPEN_READWRITE)
        val c = databasef.rawQuery(SQLQuery, whereV)
        val sizeG = c.count
        if (sizeG == 0) {
            return null
        }

        var objeto: java.util.ArrayList<java.util.ArrayList<String?>?>? = java.util.ArrayList()
        if (sizeG > 0) {
            c.moveToFirst()
            do {
                var columns: java.util.ArrayList<String?>? = java.util.ArrayList()
                val size = c.columnCount
                for (k in 0 until size) {
                    //Por cada columna, campo, que lo guarde en el respectivo objeto
                    columns?.add(c.getString(k))
                }
                val col = columns
                objeto?.add(col)
            } while (c.moveToNext())

        } else {
            Toast.makeText(cont, "No existen registros para esa consulta", Toast.LENGTH_SHORT)
                .show()
        }
        if (databasef.isOpen) databasef.close()
        return objeto
    }


    fun getQ1(contexto: Context, SQL: String): String {
        var va: String = ""
        val objV = QueryObjeto(
            contexto, SQL, null
        )
        if (objV != null) {
            var valor = objV[0]
            va = valor?.get(0).toString()
        }
        return va
    }

    fun Borrarbackup(contexto: Context, nbackup: String, sqldelete: String) {
        val fullPath =
            Environment.getExternalStorageDirectory().toString() + "/TCGO_FILES/Databases/"
        try {
            val file = File(fullPath, nbackup)
            if (file.exists()) {
                try {
                    file.delete()
                } catch (exp: Exception) {
                    println("La aplicación no puede borrar el archivo y el resultado es: " + exp.toString())
                } finally {
                    ejecbdbk(contexto, sqldelete)
                }
            } else {
                println("No existe el archivo:$nbackup")
            }
        } catch (e: Exception) {
            Log.e("App", "Error al borrar el archivo " + e.message)
        }
    }

    fun Borrarbackup_viejos(contexto: Context) {
        val fullPath =
            Environment.getExternalStorageDirectory().toString() + "/TCGO_FILES/Databases/"
        val maxidbk =
            getQ1(contexto, "Select ifnull(max(idbackup),0) as mib from '999_BACKUP_REGISTRO'")
        val minidbk =
            getQ1(contexto, "Select ifnull(min(idbackup),0) as mib from '999_BACKUP_REGISTRO'")
        val conidbk =
            getQ1(contexto, "Select ifnull(count(idbackup),0) as mib from '999_BACKUP_REGISTRO'")
        var maxint = maxidbk.toInt()
        maxint = maxidbk.toInt() - 9
        val queryElimBK =
            "SELECT nbackup from  '999_BACKUP_REGISTRO' where idbackup>${minidbk.toInt() - 1} and idbackup<$maxint"
        val objElimBK = QueryObjeto(contexto, queryElimBK, null)
        if (objElimBK?.isNotEmpty() == true) {
            listabk = arrayOfNulls(objElimBK.size)
            for (op in objElimBK.indices) {
                var nbk = objElimBK[op]
                var nbkstr = nbk?.get(0).toString()
                val queryDelBK =
                    "Delete from '999_BACKUP_REGISTRO' where nbackup='$nbkstr'"
                val file = File(fullPath, nbkstr)
                if (file.exists()) {
                    try {
                        file.delete()
                    } catch (exp: Exception) {
                        println("La aplicación no puede borrar el archivo y el resultado es: " + exp.toString())
                    } finally {
                        ejecbdbk(contexto, queryDelBK)
                    }
                } else {
                    ejecbdbk(contexto, queryDelBK)
                    println("No existe el archivo:$nbkstr")
                }
            }
        }
    }
}