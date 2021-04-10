package com.example.tcgokotlin.data.model

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tcgokotlin.R

class MenuPorc(itemView: View) : RecyclerView.ViewHolder(itemView){
        var b: TextView
        var iv: TextView
        var ti: TextView
        init {
                b = itemView.findViewById<View>(R.id.tv_Nom_Menu) as TextView
                iv = itemView.findViewById<View>(R.id.tv_Val_Menu) as TextView
                ti = itemView.findViewById<View>(R.id.TV_Titulo_Anexo) as TextView
        }
}
