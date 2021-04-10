package com.example.tcgokotlin.data.model

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tcgokotlin.R

class MenuForm(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var b: TextView
        var iv: ImageView

        init {
                b = itemView.findViewById<View>(R.id.tv_Nom_Menu) as TextView
                iv = itemView.findViewById<View>(R.id.imgCBV) as ImageView
        }
}
