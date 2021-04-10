package com.example.tcgokotlin.sqliteDBHelper

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import android.widget.Toast

class OperacionesBDInterna() {

    private val dbname = "tcgoappdb.db"
    private var exiRestore = false
    var contadorbk = 0

    @Throws(SQLException::class)
    fun queryNoData(contexto: Context, sql: String?): Boolean {
        val databasef: SQLiteDatabase
        var exitoso = false
        val bk = Backups()

        try{
            databasef = SQLiteDatabase.openDatabase(contexto.getDatabasePath(dbname).toString(),
                null,
                SQLiteDatabase.OPEN_READWRITE)
            try{
                databasef.execSQL(sql)
                true.also { exitoso = it }
            }catch (e: SQLiteException) {
                Log.i("Consulta no ejec:", e.toString())
                Toast.makeText(contexto,
                    "No se pudo ejecutar la sentencia SQL: $sql",
                    Toast.LENGTH_LONG).show()
            }
        }catch (e: Exception){
        Log.i("DB corrupta : ", e.toString());
        while (!exiRestore){
            val minidbk = bk.getQ1(contexto,
                "Select ifnull(max(idbackup),0) as mib from '999_BACKUP_REGISTRO'");
            val est = bk.restoreDB(contexto, contadorbk);
            if (est){
                exitoso  = queryNoData2(contexto, sql);
            }
            contadorbk++;
        }
        return exitoso;
    }
        return exitoso
    }

    @Throws(SQLException::class)
    fun queryNoData2(contexto: Context, sql: String?): Boolean {
        val databasef: SQLiteDatabase
        var exitoso = false
        val bk = Backups()

        try{
            databasef = SQLiteDatabase.openDatabase(contexto.getDatabasePath(dbname).toString(),
                null,
                SQLiteDatabase.OPEN_READWRITE)
            try{
                databasef.execSQL(sql)
                true.also { exitoso = it }
            }catch (e: SQLiteException) {
                Log.i("Consulta no ejec:", e.toString())
                Toast.makeText(contexto,
                    "No se pudo ejecutar la sentencia SQL: $sql",
                    Toast.LENGTH_LONG).show()
            }
        }catch (e: Exception){
            Log.i("DB corrupta : ", e.toString());
            return exitoso;
        }finally {
            exiRestore = true
        }
        return exitoso
    }
}