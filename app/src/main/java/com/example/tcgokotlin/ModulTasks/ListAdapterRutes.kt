package com.example.tcgokotlin.ModulTasks

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.R
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.utils.Common
import com.rengwuxian.materialedittext.Colors
import kotlinx.android.synthetic.main.adapter_item_rutes.view.*
import java.util.*


class ListAdapterRutes(
    private val mContext: Context,
    private @SuppressLint("SupportAnnotationUsage") @LayoutRes val posiblesArray: ArrayList<MutableMap<String, Any>>,
    private val mNumRute: Int
) :   ArrayAdapter<MutableMap<String, Any>?>(mContext,
    0,
    posiblesArray as ArrayList<MutableMap<String, Any>?>) {
    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val fg = FuncionesGenerales()
        var listItem = convertView
        if (listItem == null) listItem = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_rutes,
            parent,
            false)
        val servicio = posiblesArray[position]
        val mapHistory = (servicio["historial"] as MutableMap<*, *>?)!![Common.formatDate.format(
            Date())] as MutableMap<*, *>?
        val txtDateRango = listItem!!.findViewById<TextView>(R.id.txtDateRange)
        val typeService = listItem.findViewById<TextView>(R.id.typeService)
        val txtStandBy = listItem.findViewById<TextView>(R.id.txtStandBy)
        val imgPrioridad = listItem.findViewById<ImageView>(R.id.imgPrioridad)
        val imageGeocerca = listItem.findViewById<ImageView>(R.id.imageGeocerca)
        val txtNomClient = listItem.findViewById<TextView>(R.id.txtNomClient)
        val txtDirection = listItem.findViewById<TextView>(R.id.txtDirection)
        val txtCodTarea = listItem.findViewById<TextView>(R.id.txtCodTarea)
        val txtEstado1 = listItem.findViewById<TextView>(R.id.txtEstado1)
        val txtEstado2 = listItem.findViewById<TextView>(R.id.txtEstado2)
        val contornopercent = listItem.findViewById<TextView>(R.id.contornopercent)
        val sqlpercent = "select ifnull(avg(ev),0) from '201_PREGFORM' where idtarea='" + servicio["codetarea"].toString() + "' and idform<>'9999' and idform<>'9991' and idform<>'9992'"
        val percentEject = fg.getQ1(context, sqlpercent)
        val cantgrp = fg.getQ1(context, "select ifnull(count(*),0) from '201_PREGFORM' where  idtarea='" + servicio["codetarea"].toString() + "' and idform<>'9999' and idform<>'9991' and idform<>'9992'")
        val tipotarea = fg.getQ1(context, "select tipo from '200_TAREAS' where idtarea='" + servicio["codetarea"].toString() + "'")
        val esttarea = fg.getQ1(context, "select estado from '200_TAREAS' where idtarea='" + servicio["codetarea"].toString() + "'")

        if (percentEject.toDouble()<50){
            contornopercent.setBackgroundResource(R.drawable.btmenu_blancocbg)
        }else if (percentEject.toDouble()<80){
            contornopercent.setBackgroundResource(R.drawable.btmenu_yellowcbg)
        }else{
            contornopercent.setBackgroundResource(R.drawable.btmenu_greencbg)
        }

        val menordist:Double = fg.getQ1(context,"select min(distancia) as dmin from '200_TAREAS_DIST'").toDouble()
        val tmin = fg.getQ1(context,"select idtarea from '200_TAREAS_DIST' where distancia=$menordist")
        val cercano = if (tmin.toString() == servicio["codetarea"].toString()) "1" else "0"

        val valorpercent = percentEject + "%"
        contornopercent.text = valorpercent

        txtCodTarea.setText(servicio["codetarea"].toString().substring(11))
        val estado_item = fg.getQ1(context,
            "Select estado from '200_TAREAS' where idtarea='" + servicio["codetarea"].toString() + "'")
        txtEstado1.text = "$estado_item"
        txtEstado2.text = "$estado_item"
        val lineRow = listItem.findViewById<LinearLayout>(R.id.lineRow)
        lineRow.setBackgroundColor(Color.parseColor("#FFFFFF"))
        imgPrioridad.visibility = View.GONE
        txtDateRango.text = "${mapHistory?.get("TareaRangoIn")} - ${mapHistory?.get("TareaRangoFin")}"
        typeService.text = Common.listTipoServ?.get(servicio["typeService"])
        txtNomClient.text = (servicio["infoCliente"] as MutableMap<*, *>)["nomClient"].toString()
        txtDirection.text = (servicio["infoCliente"] as MutableMap<*, *>)["direction"].toString()

        if (servicio["idRel"] != "") {
            listItem.tvODS.visibility = View.VISIBLE
            listItem.tvODS.text = "${context.getString(R.string.title_ods)} ${servicio["idRel"]}"
        }
        if (servicio["standBy"].toString() == "1" && estado_item == "Pendiente") {
            txtStandBy.visibility = View.VISIBLE
        }
        if (servicio["prioridad"]!!.equals("VIP")) {
            imgPrioridad.visibility = View.VISIBLE
            lineRow.setBackgroundColor(Color.parseColor("#FAFAD2"))
        }
        if (estado_item.compareTo("Pendiente") == 0) {
            txtEstado1.visibility = View.VISIBLE
            txtEstado2.visibility = View.GONE
        } else {
            txtEstado2.visibility = View.VISIBLE
            txtEstado1.visibility = View.GONE
        }
        if (cercano == "1") {
            imageGeocerca.visibility = View.VISIBLE
        } else {
            imageGeocerca.visibility = View.GONE
        }
        val background = listItem.viewColor.background
        when(estado_item) {
            "Pendiente" -> {
                val color = ContextCompat.getColor(context, R.color.task_finish)
                DrawableCompat.setTint(background, color)
            }
            "Finalizada" -> {
                val color = ContextCompat.getColor(context, R.color.task_finish)
                DrawableCompat.setTint(background, color)
            }
            "Cancelada" -> {
                val color = ContextCompat.getColor(context, R.color.task_cancel)
                DrawableCompat.setTint(background, color)
            }
            "No exitosa" -> {
                val color = ContextCompat.getColor(context, R.color.task_not_successful)
                DrawableCompat.setTint(background, color)
            }
            else -> {
                val history = servicio["historial"] as? MutableMap<String, Any>
                val currentDate = history?.get(Tools.returnCurrentTime()) as? MutableMap<String, Any>
                val finDateString = currentDate?.get("TareaRangoFin").toString()
                val finDate = Tools.convertStringInDate(finDateString)
                val currentHour = Tools.convertStringInDate(Tools.getCurrentHour())
                val millis = currentHour?.time?.let { finDate?.time?.minus(it) }
                val hours = (millis?.div((1000 * 60 * 60)))?.toInt()
                if (hours != null) {
                    when {
                        hours > 4 -> {
                            val color = ContextCompat.getColor(context, R.color.time_major_four)
                            DrawableCompat.setTint(background, color)
                        }
                        hours in 2..4 -> {
                            val color = ContextCompat.getColor(context,
                                R.color.time_between_four_two)
                            DrawableCompat.setTint(background, color)
                        }
                        hours == 1 -> {
                            val color = ContextCompat.getColor(context, R.color.time_one)
                            DrawableCompat.setTint(background, color)
                        }
                        hours == 0 -> {
                            val color = ContextCompat.getColor(context, R.color.time_zero)
                            DrawableCompat.setTint(background, color)
                        }

                    }
                }
            }
        }
        return listItem
    }
}
