package com.example.tcgokotlin.Sincronizar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class VerificarConex {

    public boolean revisarconexión(Context cont){

        ConnectivityManager connectivityManager = (ConnectivityManager)cont.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean network = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Si hay conexión a Internet en este momento
            network = true;
        }

        boolean wifi = conectadoWifi(cont);
        boolean datos = conectadoRedMovil(cont);

        if((wifi == true) || (datos == true) || (network == true)){
            return true;
        } else {
            return false;
        }
    }

    protected Boolean conectadoWifi(Context cont){
        ConnectivityManager connectivity = (ConnectivityManager)cont.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected Boolean conectadoRedMovil(Context cont){
        ConnectivityManager connectivity = (ConnectivityManager)cont.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }
}