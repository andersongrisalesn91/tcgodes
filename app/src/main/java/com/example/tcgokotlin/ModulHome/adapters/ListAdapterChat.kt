package com.example.tcgokotlin.ModulHome.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.example.tcgokotlin.R
import com.example.tcgokotlin.data.model.Chat
import com.google.firebase.auth.FirebaseAuth
import kotlin.collections.ArrayList

class ListAdapterChat(private val mContext: Context, @SuppressLint("SupportAnnotationUsage") @LayoutRes arrayChats: ArrayList<Chat?>) : ArrayAdapter<Chat?>(mContext, 0, arrayChats as List<Chat?>) {
    private val arrayChats: ArrayList<Chat?> = arrayChats

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_chat_item, parent, false)
        val chat: Chat? = arrayChats[position]
        val linear = listItem!!.findViewById<LinearLayout>(R.id.linear)
        val linearIn = listItem.findViewById<LinearLayout>(R.id.linearIn)
        val linearOut = listItem.findViewById<LinearLayout>(R.id.linearOut)
        val txtName = listItem.findViewById<TextView>(R.id.txtName)
        val txtMessaje = listItem.findViewById<TextView>(R.id.txtMessaje)
        val txtMessaje2 = listItem.findViewById<TextView>(R.id.txtMessaje2)
        val txtHora = listItem.findViewById<TextView>(R.id.txtHora)
        val txtHora2 = listItem.findViewById<TextView>(R.id.txtHora2)
        txtMessaje.text = chat?.text
        //txtHora.setText(chat.getDate().getHours()+chat.getDate().getMinutes());
        if (chat?.remitente.equals(FirebaseAuth.getInstance().currentUser!!.uid)) {
            linear.visibility = View.VISIBLE
            linearOut.visibility = View.VISIBLE
            linearIn.visibility = View.GONE
            txtMessaje2.text = chat?.text
            txtHora2.text = chat?.date
        } else {
            txtName.text = chat?.name
            linear.visibility = View.GONE
            linearOut.visibility = View.GONE
            linearIn.visibility = View.VISIBLE
            txtMessaje.text = chat?.text
            txtHora.text = chat?.text
        }
        return listItem
    }

}