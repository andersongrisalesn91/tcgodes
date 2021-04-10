package com.example.tcgokotlin.sqliteDBHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class ConsultaGeneral {
    private final String dbname = "tcgoappdb.db";
    private Boolean exiRestore = false;
    Integer contadorbk = 0;
    //Realizar una consulta con raw y devolver un objeto
    public ArrayList<String> [] queryObjeto(Context cont, String SQLQuery, String [] whereV){
        Backups bk = new Backups();
        SQLiteDatabase databasef;
        Cursor c;
        int sizeG = 0;
        try{
        databasef = SQLiteDatabase.openDatabase(cont.getDatabasePath(dbname).toString(), null, SQLiteDatabase.OPEN_READWRITE);
        c = databasef.rawQuery(SQLQuery, whereV);


            sizeG = c.getCount();
            if(sizeG == 0){
                return null;
            }
        }catch (Exception e){
            Log.i("DB corrupta : ", e.toString());
            ArrayList<String> []  valquery = null;
            while (!exiRestore){
                String minidbk = bk.getQ1(cont,"Select ifnull(max(idbackup),0) as mib from '999_BACKUP_REGISTRO'");
                boolean est = bk.restoreDB(cont,contadorbk);
                if (est){
                   valquery  = queryObjetobk(cont,SQLQuery,whereV);
                }
                contadorbk++;
            }
            return valquery;
        }

        ArrayList<String> [] objeto = new ArrayList[sizeG];
        for (int n = 0; n < sizeG; n++){
            objeto[n] = new ArrayList<>();
        }
        int ind = 0;
        if(sizeG > 0){
            c.moveToFirst();
            do{
                int size = c.getColumnCount();
                for (int k = 0; k < size; k++){
                    //Por cada columna, campo, que lo guarde en el respectivo objeto
                    objeto[ind].add(c.getString(k));
                }
                ind++;
            }while(c.moveToNext());
        } else {
            Toast.makeText(cont, "No existen registros para esa consulta", Toast.LENGTH_SHORT).show();
        }
        if (databasef.isOpen()) databasef.close();
        return objeto;
    }

    public ArrayList<String> [] queryObjeto2val(Context cont, String SQLQuery, String [] whereV){
        Backups bk = new Backups();
        SQLiteDatabase databasef;
        Cursor c;
        int sizeG = 0;
        try{
            databasef = SQLiteDatabase.openDatabase(cont.getDatabasePath(dbname).toString(), null, SQLiteDatabase.OPEN_READWRITE);
            c = databasef.rawQuery(SQLQuery, whereV);
            sizeG = c.getCount();
            if(sizeG == 0){
                return null;
            }
        }catch (Exception e){
            Log.i("DB corrupta : ", e.toString());
            ArrayList<String> []  valquery = null;
            while (!exiRestore){
                String minidbk = bk.getQ1(cont,"Select ifnull(max(idbackup),0) as mib from '999_BACKUP_REGISTRO'");
                boolean est = bk.restoreDB(cont,contadorbk);
                if (est){
                    valquery  = queryObjetobk(cont,SQLQuery,whereV);
                }
                contadorbk++;
            }
            return valquery;
        }
        ArrayList<String> [] objeto = new ArrayList[sizeG];
        for (int n = 0; n < sizeG; n++){
            objeto[n] = new ArrayList<>();
        }
        int ind = 0;
        if(sizeG > 0){
            c.moveToFirst();
            do{
                int size = c.getColumnCount();
                for (int k = 0; k < size; k++){
                    //Por cada columna, campo, que lo guarde en el respectivo objeto
                    objeto[ind].add(c.getString(k));
                }
                ind++;
            }while(c.moveToNext());
        } else {
            Toast.makeText(cont, "No existen registros para esa consulta", Toast.LENGTH_SHORT).show();
        }
        if (databasef.isOpen()) databasef.close();
        return objeto;
    }

    public ArrayList<String> [] queryObjetobk(Context cont, String SQLQuery, String [] whereV){
        SQLiteDatabase databasef = SQLiteDatabase.openDatabase(cont.getDatabasePath(dbname).toString(), null, SQLiteDatabase.OPEN_READWRITE);
        Cursor c = databasef.rawQuery(SQLQuery, whereV);
        int sizeG = 0;
        try{
            sizeG = c.getCount();
            if(sizeG == 0){
                return null;
            }
        }catch (Exception ex){
            return null;
        }finally {
            exiRestore = true;
        }

        ArrayList<String> [] objeto = new ArrayList[sizeG];
        for (int n = 0; n < sizeG; n++){
            objeto[n] = new ArrayList<>();
        }
        int ind = 0;
        if(sizeG > 0){
            c.moveToFirst();
            do{
                int size = c.getColumnCount();
                for (int k = 0; k < size; k++){
                    //Por cada columna, campo, que lo guarde en el respectivo objeto
                    objeto[ind].add(c.getString(k));
                }
                ind++;
            }while(c.moveToNext());
        } else {
            Toast.makeText(cont, "No existen registros para esa consulta", Toast.LENGTH_SHORT).show();
        }
        if (databasef.isOpen()) databasef.close();
        return objeto;
    }
}