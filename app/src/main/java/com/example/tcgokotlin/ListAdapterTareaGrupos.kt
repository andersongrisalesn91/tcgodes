package com.example.tcgokotlin

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import com.example.tcgokotlin.utils.Common
import com.example.tcgokotlin.utils.Common.ListAdapterImgPhoto
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt


class ListAdapterTareaGrupos(var mContext: Context, var myObj: List<Any>, var mStringX: MutableMap<String, Any>, var mapFormStruct: MutableMap<String, Any?>) : ArrayAdapter<Any?>(mContext, 0, myObj) {
    var countImg = -1
    var cantItems: Int? = 0
    var cantGrup = 0
    var mapReq: MutableMap<String, Any?>? = null
    var mapConsumo: MutableMap<String, Any?>? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        listItem = LayoutInflater.from(mContext).inflate(R.layout.adapter_tarea_items, parent, false)
        val grupo = listItem.findViewById<TextView>(R.id.grupo)
        val txtCriterio = listItem.findViewById<TextView>(R.id.txtCriterio)
        val editObs = listItem.findViewById<EditText>(R.id.editObs)
        val txtPorcentaje = listItem.findViewById<TextView>(R.id.txtPorcentaje)
        val txtEstado = listItem.findViewById<TextView>(R.id.txtEstado)
        val txtCheck1 = listItem.findViewById<TextView>(R.id.txtCheck1)
        val txtCheck2 = listItem.findViewById<TextView>(R.id.txtCheck2)
        val txtCheck3 = listItem.findViewById<TextView>(R.id.txtCheck3)
        val checkConforme = listItem.findViewById<CheckBox>(R.id.checkConforme)
        val checkNoConforme = listItem.findViewById<CheckBox>(R.id.checkNoConforme)
        val checkNoAplica = listItem.findViewById<CheckBox>(R.id.checkNoAplica)
        val ImgPhoto = listItem.findViewById<ImageView>(R.id.ImgPhoto)
        val lineGroup = listItem.findViewById<LinearLayout>(R.id.lineGroup)
        val lineItem = listItem.findViewById<LinearLayout>(R.id.lineItem)
        val lineObs = listItem.findViewById<LinearLayout>(R.id.lineObs)
        lineItem.setOnClickListener { }
        val bool = (myObj[position] as Map<String?, Any>)["Bool"].toString()
        /** */
        if ("0" === bool) {
            val myObjSelected = myObj[position] as Map<String, Any>
            lineGroup.visibility = View.VISIBLE
            lineItem.visibility = View.GONE
            lineObs.visibility = View.GONE
            grupo.text = myObjSelected["grupo"] as String?
            var items = 0.0
            var cantItems = 0.0
            for (snapshotGroup in (myObjSelected["grupItems"] as ArrayList<Map<String?, Any?>>?)!!) {
                for (snapshotItem in (snapshotGroup["items"] as ArrayList<Map<String?, Any?>>?)!!) {
                    items = items + 1
                    var validation = true
                    try {
                        if (snapshotItem["active_check"].toString() != "0") {
                            if (snapshotItem["id_form_tip_foto"].toString() == "2") {
                                when (snapshotItem["conforme"].toString()) {
                                    "0" -> if (snapshotItem["img"] != null) {
                                    } else {
                                        validation = false
                                    }
                                    else -> if (snapshotItem["conforme"] != null) {
                                    } else {
                                        validation = false
                                    }
                                }
                            } else {
                                if (snapshotItem["conforme"] != null) {
                                } else {
                                    validation = false
                                }
                                if (snapshotItem["id_form_tip_foto"].toString() == "1") {
                                } else if (snapshotItem["img"] != null) {
                                } else {
                                    validation = false
                                }
                            }
                        } else {
                            if (snapshotItem["id_form_tip_foto"].toString() == "1") {
                            } else if (snapshotItem["img"] != null) {
                            } else {
                                validation = false
                            }
                        }
                        if (snapshotItem["id_form_tip_calif"].toString() == "1") {
                        } else if (snapshotItem["calif"] != null) {
                        } else {
                            validation = false
                        }
                        if (snapshotItem["id_form_tip_text"].toString() == "1" || snapshotItem["id_form_tip_text"].toString() == "2") {
                        } else if (!snapshotItem["obs"].toString().trim { it <= ' ' }.isEmpty()) {
                        } else {
                            validation = false
                        }
                    } catch (e: Exception) {
                        validation = false
                    }
                    if (validation) {
                        cantItems = cantItems + 1
                    } else {
                    }
                }
            }
            for (snapshotItem in (myObjSelected["items"] as ArrayList<Map<String?, Any?>>?)!!) {
                items = items + 1
                var validation = true
                try {
                    if (snapshotItem["active_check"].toString() != "0") {
                        if (snapshotItem["id_form_tip_foto"].toString() == "2") {
                            when (snapshotItem["conforme"].toString()) {
                                "0" -> if (snapshotItem["img"] != null) {
                                } else {
                                    validation = false
                                }
                                else -> if (snapshotItem["conforme"] != null) {
                                } else {
                                    validation = false
                                }
                            }
                        } else {
                            if (snapshotItem["conforme"] != null) {
                            } else {
                                validation = false
                            }
                            if (snapshotItem["id_form_tip_foto"].toString() == "1") {
                            } else if (snapshotItem["img"] != null) {
                            } else {
                                validation = false
                            }
                        }
                    } else {
                        if (snapshotItem["id_form_tip_foto"].toString() == "1") {
                        } else if (snapshotItem["img"] != null) {
                        } else {
                            validation = false
                        }
                    }
                    if (snapshotItem["id_form_tip_calif"].toString() == "1") {
                    } else if (snapshotItem["calif"] != null) {
                    } else {
                        validation = false
                    }
                    if (snapshotItem["id_form_tip_text"].toString() == "1" || snapshotItem["id_form_tip_text"].toString() == "2") {
                    } else if (!snapshotItem["obs"].toString().trim { it <= ' ' }.isEmpty()) {
                    } else {
                        validation = false
                    }
                } catch (e: Exception) {
                    validation = false
                }
                if (validation) {
                    cantItems += 1
                }
            }
            txtPorcentaje.text = "${((cantItems * 100 / items).roundToInt()).toString()}%"
            val arrayContenido = mapFormStruct["contenido"] as ArrayList<Any>?
            val mapGroup = arrayContenido!![position] as MutableMap<String, Any>
            mapGroup["pocenGrup"] = txtPorcentaje.text.toString()
            arrayContenido[position] = mapGroup
            mapFormStruct["contenido"] = arrayContenido
            val mapTask: MutableMap<String, Any> = HashMap()
            mapTask["formStruct"] = mapFormStruct
            Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)
            if (cantItems * 100 / items == 100.0) {
                txtEstado.text = "Finalizado"
                txtEstado.setTextColor(Color.parseColor("#ffffff"))
                txtEstado.setBackgroundColor(Color.parseColor("#ff99cc00"))
            } else {
            }
        } //Grupos (Pendiente)
        else if ("1" === bool) {
            val myObjSelected = myObj[position] as Map<String, Any?>
            val linearStars = listItem.findViewById<LinearLayout>(R.id.linearStars)
            val linearCheck = listItem.findViewById<LinearLayout>(R.id.linearCheck)
            val linearPhoto = listItem.findViewById<LinearLayout>(R.id.linearPhoto)
            val linearPhotoCode = listItem.findViewById<LinearLayout>(R.id.linearPhotoCode)
            val linearObs = listItem.findViewById<LinearLayout>(R.id.linearObs)
            val lineSelector = listItem.findViewById<LinearLayout>(R.id.lineSelector)
            val spinerSelector = listItem.findViewById<Spinner>(R.id.spinerSelector)
            val txtSelector = listItem.findViewById<TextView>(R.id.txtSelector)
            val ObsReq = listItem.findViewById<ImageView>(R.id.ObsReq)
            val ObsPhoto = listItem.findViewById<ImageView>(R.id.ObsPhoto)
            val imgStar1 = listItem.findViewById<ImageView>(R.id.imgStar1)
            val imgStar2 = listItem.findViewById<ImageView>(R.id.imgStar2)
            val imgStar3 = listItem.findViewById<ImageView>(R.id.imgStar3)
            val imgStar4 = listItem.findViewById<ImageView>(R.id.imgStar4)
            val imgStar5 = listItem.findViewById<ImageView>(R.id.imgStar5)
            val boolStar = arrayOf(false, false, false, false, false)
            val arrayContenido = mapFormStruct["contenido"] as ArrayList<Any>?
            var x: MutableMap<String, Any?> = HashMap()
            var y: MutableMap<String, Any?> = HashMap()
            var cont = 0
            for (j in (mStringX["itemClick"] as ArrayList<Int?>?)!!) {
                x = when (cont) {
                    0 -> arrayContenido!![j!!] as MutableMap<String, Any?>
                    else -> (x["grupItems"] as ArrayList<MutableMap<String, Any?>>?)!![j!!]
                }
                cont++
            }
            cantGrup = try {
                (x["grupItems"] as ArrayList<Any?>?)!!.size
            } catch (e: Exception) {
                0
            }
            val arrayItems = x["items"] as ArrayList<MutableMap<String, Any?>>?
            y = arrayItems!![position - cantGrup]
            val finalY = y
            val finalX = x
            checkConforme.setOnClickListener {
                checkConforme.isChecked = true
                checkNoConforme.isChecked = false
                checkNoAplica.isChecked = false
                if (myObjSelected["id_form_tip_foto"].toString() == "2") ObsPhoto.visibility = View.VISIBLE else ObsPhoto.visibility = View.GONE
                finalY["conforme"] = "1"
                saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
            }
            checkNoAplica.setOnClickListener {
                checkConforme.isChecked = false
                checkNoConforme.isChecked = false
                checkNoAplica.isChecked = true
                if (myObjSelected["id_form_tip_foto"].toString() == "2") ObsPhoto.visibility = View.VISIBLE else ObsPhoto.visibility = View.GONE
                finalY["conforme"] = "2"
                saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
            }
            checkNoConforme.setOnClickListener {
                checkConforme.isChecked = false
                checkNoConforme.isChecked = true
                checkNoAplica.isChecked = false
                ObsPhoto.visibility = View.GONE
                finalY["conforme"] = "0"
                saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
            }
            ObsPhoto.setOnClickListener { view -> Snackbar.make(view, "OPCIONAL", 1000).show() }
            ObsReq.setOnClickListener { view -> Snackbar.make(view, "OPCIONAL", 1000).show() }


            //Log.e("conforme",dataSnapshot.toString());
            if (myObjSelected["active_check"].toString() != "0") {
                linearCheck.visibility = View.VISIBLE
                txtCheck1.text = myObjSelected["label_check_con"].toString()
                txtCheck2.text = myObjSelected["label_check_nocon"].toString()
                txtCheck3.text = myObjSelected["label_check_noapli"].toString()
                try {
                    if (myObjSelected["conforme"].toString() == "0") {
                        checkNoConforme.callOnClick()
                    } else if (myObjSelected["conforme"].toString() == "2") {
                        checkNoAplica.callOnClick()
                    } else if (myObjSelected["conforme"].toString() == "1") {
                        checkConforme.callOnClick()
                    } else {
                    }
                } catch (e: Exception) {
                }
            } else {
                linearCheck.visibility = View.GONE
            }
            if (myObjSelected["id_form_tip_text"].toString() != "1") {
                linearObs.visibility = View.VISIBLE
                if (myObjSelected["id_form_tip_text"].toString() == "2") {
                    ObsReq.visibility = View.VISIBLE
                } else {
                    ObsReq.visibility = View.GONE
                }
                editObs.hint = myObjSelected["label_text"].toString()
                try {
                    editObs.setText(myObjSelected["obs"].toString())
                } catch (e: Exception) {
                }
                editObs.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                        finalY["obs"] = editObs.text.toString()
                        saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                    }

                    override fun afterTextChanged(editable: Editable) {}
                })
            } else {
                linearObs.visibility = View.GONE
            }
            if (myObjSelected["id_form_tip_calif"].toString() != "1") {
                linearStars.visibility = View.VISIBLE
                if (myObjSelected["id_form_tip_calif"].toString() == "2") {
                    imgStar1.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        imgStar2.setImageResource(R.drawable.ic_star_border_blue)
                        imgStar3.setImageResource(R.drawable.ic_star_border_blue)
                        imgStar4.setImageResource(R.drawable.ic_star_border_blue)
                        imgStar5.setImageResource(R.drawable.ic_star_border_blue)
                        finalY["calif"] = "1"
                        saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                    }
                    imgStar2.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        imgStar2.setImageResource(R.drawable.ic_star_blue)
                        imgStar3.setImageResource(R.drawable.ic_star_border_blue)
                        imgStar4.setImageResource(R.drawable.ic_star_border_blue)
                        imgStar5.setImageResource(R.drawable.ic_star_border_blue)
                        finalY["calif"] = "2"
                        saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                    }
                    imgStar3.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        imgStar2.setImageResource(R.drawable.ic_star_blue)
                        imgStar3.setImageResource(R.drawable.ic_star_blue)
                        imgStar4.setImageResource(R.drawable.ic_star_border_blue)
                        imgStar5.setImageResource(R.drawable.ic_star_border_blue)
                        finalY["calif"] = "3"
                        saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                    }
                    imgStar4.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        imgStar2.setImageResource(R.drawable.ic_star_blue)
                        imgStar3.setImageResource(R.drawable.ic_star_blue)
                        imgStar4.setImageResource(R.drawable.ic_star_blue)
                        imgStar5.setImageResource(R.drawable.ic_star_border_blue)
                        finalY["calif"] = "4"
                        saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                    }
                    imgStar5.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        imgStar2.setImageResource(R.drawable.ic_star_blue)
                        imgStar3.setImageResource(R.drawable.ic_star_blue)
                        imgStar4.setImageResource(R.drawable.ic_star_blue)
                        imgStar5.setImageResource(R.drawable.ic_star_blue)
                        finalY["calif"] = "5"
                        saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                    }
                    try {
                        if (myObjSelected["calif"].toString() == "1") imgStar1.callOnClick() else if (myObjSelected["calif"].toString() == "2") imgStar2.callOnClick() else if (myObjSelected["calif"].toString() == "3") imgStar3.callOnClick() else if (myObjSelected["calif"].toString() == "4") imgStar4.callOnClick() else imgStar5.callOnClick()
                    } catch (e: Exception) {
                    }
                } else if (myObjSelected["id_form_tip_calif"].toString() == "3") {
                    imgStar1.setOnClickListener {
                        if (boolStar[0]) {
                            imgStar1.setImageResource(R.drawable.ic_star_blue)
                            boolStar[0] = false
                            finalY["calif"] = "2"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        } else {
                            imgStar1.setImageResource(R.drawable.ic_star_half_blue)
                            boolStar[0] = true
                            finalY["calif"] = "1"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        }
                        imgStar2.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[1] = false
                        imgStar3.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[2] = false
                        imgStar4.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[3] = false
                        imgStar5.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[4] = false
                    }
                    imgStar2.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        boolStar[0] = false
                        if (boolStar[1]) {
                            imgStar2.setImageResource(R.drawable.ic_star_blue)
                            boolStar[1] = false
                            finalY["calif"] = "4"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        } else {
                            imgStar2.setImageResource(R.drawable.ic_star_half_blue)
                            boolStar[1] = true
                            finalY["calif"] = "3"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        }
                        imgStar3.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[2] = false
                        imgStar4.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[3] = false
                        imgStar5.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[4] = false
                    }
                    imgStar3.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        boolStar[0] = false
                        imgStar2.setImageResource(R.drawable.ic_star_blue)
                        boolStar[1] = false
                        if (boolStar[2]) {
                            imgStar3.setImageResource(R.drawable.ic_star_blue)
                            boolStar[2] = false
                            finalY["calif"] = "6"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        } else {
                            imgStar3.setImageResource(R.drawable.ic_star_half_blue)
                            boolStar[2] = true
                            finalY["calif"] = "5"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        }
                        imgStar4.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[3] = false
                        imgStar5.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[4] = false
                    }
                    imgStar4.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        boolStar[0] = false
                        imgStar2.setImageResource(R.drawable.ic_star_blue)
                        boolStar[1] = false
                        imgStar3.setImageResource(R.drawable.ic_star_blue)
                        boolStar[2] = false
                        if (boolStar[3]) {
                            imgStar4.setImageResource(R.drawable.ic_star_blue)
                            boolStar[3] = false
                            finalY["calif"] = "8"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        } else {
                            imgStar4.setImageResource(R.drawable.ic_star_half_blue)
                            boolStar[3] = true
                            finalY["calif"] = "7"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        }
                        imgStar5.setImageResource(R.drawable.ic_star_border_blue)
                        boolStar[4] = false
                    }
                    imgStar5.setOnClickListener {
                        imgStar1.setImageResource(R.drawable.ic_star_blue)
                        imgStar2.setImageResource(R.drawable.ic_star_blue)
                        imgStar3.setImageResource(R.drawable.ic_star_blue)
                        imgStar4.setImageResource(R.drawable.ic_star_blue)
                        if (boolStar[4]) {
                            imgStar5.setImageResource(R.drawable.ic_star_blue)
                            boolStar[4] = false
                            finalY["calif"] = "10"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        } else {
                            imgStar5.setImageResource(R.drawable.ic_star_half_blue)
                            boolStar[4] = true
                            finalY["calif"] = "9"
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        }
                    }
                    try {
                        if (myObjSelected["calif"].toString() == "1") imgStar1.callOnClick() else if (myObjSelected["calif"].toString() == "2") {
                            imgStar1.callOnClick()
                            imgStar1.callOnClick()
                        } else if (myObjSelected["calif"].toString() == "3") imgStar2.callOnClick() else if (myObjSelected["calif"].toString() == "4") {
                            imgStar2.callOnClick()
                            imgStar2.callOnClick()
                        } else if (myObjSelected["calif"].toString() == "5") imgStar3.callOnClick() else if (myObjSelected["calif"].toString() == "6") {
                            imgStar3.callOnClick()
                            imgStar3.callOnClick()
                        } else if (myObjSelected["calif"].toString() == "7") imgStar4.callOnClick() else if (myObjSelected["calif"].toString() == "8") {
                            imgStar4.callOnClick()
                            imgStar4.callOnClick()
                        } else if (myObjSelected["calif"].toString() == "9") imgStar5.callOnClick() else {
                            imgStar5.callOnClick()
                            imgStar5.callOnClick()
                        }
                    } catch (e: Exception) {
                    }
                }
            } else {
                linearStars.visibility = View.GONE
            }
            try {
                if (myObjSelected["id_selector"].toString() != "0") {
                    lineSelector.visibility = View.VISIBLE
                    txtSelector.setText(Common.listSelector!!.get((myObj[position] as Map<String?, Any>)["id_selector"].toString().toInt()))
                    Log.e("tcgoSelector", "1")



                    val arrayList: ArrayList<String> = Common.listSelectorElement!!.get(Common.listSelector!!.get((myObj[position] as Map<String?, Any>)["id_selector"].toString().toInt()))!!
                    val arrayAdapter = ArrayAdapter(mContext, android.R.layout.simple_spinner_item, arrayList)
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinerSelector.adapter = arrayAdapter
                    Log.e("tcgoSpiner", myObjSelected["selectorElement"].toString())
                    //                            for (Map<String, String> mapSpinerItem: )
                    spinerSelector.setSelection(1)
                    spinerSelector.onItemSelectedListener = object : OnItemSelectedListener {
                        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                            finalY["selectorElement"] = arrayAdapter.getItem(i)
                            saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, false)
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                    }
                } else {
                    lineSelector.visibility = View.GONE
                }
            } catch (e: Exception) {
            }
            if (myObjSelected["img"] != null) {
                ImgPhoto.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
            } else {
            }

//                    if (!dataSnapshot.child("id_form_foto_code").getValue().toString().equals("1")){
//                        linearPhotoCode.setVisibility(View.VISIBLE);
//                        linearPhotoCode.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//
//                            }
//                        });
//                    }
            if ((myObj[position] as Map<String?, Any>)["id_form_tip_foto"].toString() != "1") {
                linearPhoto.visibility = View.VISIBLE
                ImgPhoto.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        val alertDialog = AlertDialog.Builder(mContext)
                        val inflater = (mContext as Activity).layoutInflater
                        val layout_take_photo = inflater.inflate(R.layout.layout_take_photo, null)
                        val imgAdd = layout_take_photo.findViewById<ImageView>(R.id.imgAdd)
                        val imgDell = layout_take_photo.findViewById<ImageView>(R.id.imgDell)
                        val gridView = layout_take_photo.findViewById<GridView>(R.id.gridView)
                        val imgCerrar = layout_take_photo.findViewById<ImageView>(R.id.imgCerrar)
                        val linImgText = layout_take_photo.findViewById<LinearLayout>(R.id.linImgText)
                        val editImgText = layout_take_photo.findViewById<EditText>(R.id.editImgText)
                        val btnImgText = layout_take_photo.findViewById<Button>(R.id.btnImgText)
                        imgDell.visibility = View.GONE
                        if ((myObj[position] as Map<String?, Any>)["id_form_tip_foto"].toString() == "3" || (myObj[position] as Map<String?, Any>)["id_form_tip_foto"].toString() == "5") {
                            Common.txtPhoto = ""
                            linImgText.visibility = View.VISIBLE
                            btnImgText.setOnClickListener { editImgText.setText("") }
                            editImgText.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                                    Common.txtPhoto = editImgText.text.toString()
                                }

                                override fun afterTextChanged(editable: Editable) {}
                            })
                        } else {
                            linImgText.visibility = View.GONE
                        }
                        Common.waitingDialog!!.setOnDismissListener(DialogInterface.OnDismissListener {
                            cargarImagenes(view, gridView, imgDell, finalY["img"] as ArrayList<Map<String, Any>>?)
                            ImgPhoto.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
                        })
                        cargarImagenes(view, gridView, imgDell, finalY["img"] as ArrayList<Map<String, Any>>?)
                        imgAdd.setOnClickListener {
                            if (Common.boolPhotos) {
                                Common.ifSave = "item"
                                Common.mapArrayImg = ArrayList()
                                if (finalY["img"] != null) {
                                    Common.mapArrayImg = finalY["img"] as ArrayList<Map<String, Any>>?
                                } else {
                                }
                                Common.cantGrup = cantGrup
                                saveItem(arrayItems, position, finalY, finalX, arrayContenido!!, true)
                               // Common.dispatchTakePictureIntent(imgAdd)
                                Common.typePhoto = (myObj[position] as Map<String?, Any>)["id_form_tip_foto"].toString()

                                //                                            if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) return;
                                //                                            ContentValues values = new ContentValues();
                                //                                            values.put(MediaStore.Images.Media.TITLE, "New Picture");
                                //                                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                                //                                            Common.imageUri = mContext.getContentResolver().insert(
                                //                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                //                                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                //                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Common.imageUri);
                                //                                            Common.ListAdapterImgPhoto=true;
                                //                                            Common.urlListAdapterImgPhoto= mStringX.get("key").toString()+"/items/"+gItem.get("key")+"/img/"+countImg;
                                //                                            ((Activity) mContext).startActivityForResult(intent, 1);
                            } else {
                                Toast.makeText(mContext, "No puedes tomar más fotos.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        //buscar imagen y eliminar
                        imgDell.setOnClickListener {
                            FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(mStringX["key"].toString() + "/items/" + myObjSelected["key"] + "/img").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshotImg: DataSnapshot) {
                                    countImg = 0
                                    for (snapshotItem in dataSnapshotImg.children) {
                                        countImg++
                                    }
                                    //                                                FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(mStringX.get("key").toString()+"/items/"+gItem.get("key")+"/img/"+(countImg-1)).removeValue();
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                        }
                        alertDialog.setView(layout_take_photo)
                        val show = alertDialog.show()
                        imgCerrar.setOnClickListener { show.dismiss() }
                    }

                    private fun cargarImagenes(view: View, gridView: GridView, imgDell: ImageView, listImg: ArrayList<Map<String, Any>>?) {
                        val arrayList2 = ArrayList<Any>()
                        try {
                            countImg = listImg!!.size
                            for (snapshotItem in listImg) {
                                countImg++
                                val items: MutableMap<String, Any> = HashMap()
                                items["url"] = snapshotItem["url"].toString()
                                items["typePhoto"] = (myObj[position] as Map<String?, Any>)["id_form_tip_foto"].toString()
                                arrayList2.add(items)
                            }
                        } catch (e: Exception) {
                            countImg = 0
                        }
                        if (countImg != 0) {
                            imgDell.visibility = View.VISIBLE
                        } else {
                            imgDell.visibility = View.GONE
                        }
                       // val mAdapterImgPhoto = ListAdapterImgPhoto(mContext, arrayList2, mStringX["key"].toString() + "/items/" + myObjSelected["key"])
                        //gridView.adapter = mAdapterImgPhoto
                    }
                })
            } else {
                linearPhoto.visibility = View.GONE
            }
            lineItem.visibility = View.VISIBLE
            txtCriterio.text = myObjSelected["criterio"] as String?
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE)
        } //Items
        else if ("2" === bool) {
            lineGroup.visibility = View.GONE
            lineItem.visibility = View.GONE
            lineObs.visibility = View.VISIBLE
            val btnAddFirma1 = listItem.findViewById<ImageView>(R.id.btnAddFirma1)
            val btnAddFirma2 = listItem.findViewById<ImageView>(R.id.btnAddFirma2)
            val editObsGeneral = listItem.findViewById<EditText>(R.id.editObsGeneral)
            val checkDatos = listItem.findViewById<CheckBox>(R.id.checkDatos)
            val editName = listItem.findViewById<EditText>(R.id.editName)
            val editCC = listItem.findViewById<EditText>(R.id.editCC)
            Common.firma = false
            checkDatos.setOnCheckedChangeListener { compoundButton, b ->
                val updateCheck: MutableMap<String, Any> = HashMap()
                updateCheck["processDataClient"] = if (b) "1" else "0"
                Log.e("TcgoApp", "p")
                Common.dbServices!!.document(mStringX["key"].toString()).update(updateCheck)
            }
            val mapInfogen = mapFormStruct["infogen"] as MutableMap<String, Any>?
            btnAddFirma1.setOnClickListener {
                Common.waitingDialog!!.setOnDismissListener(DialogInterface.OnDismissListener {
                    if (Common.isConnected) {
                        try {
                            Picasso.get()
                                    .load(mapInfogen!!["firma1"].toString())
                                    .placeholder(R.drawable.itcgo).into(btnAddFirma1)
                        } catch (e: Exception) {
                        }
                        try {
                            Picasso.get()
                                    .load(mapInfogen!!["firma2"].toString())
                                    .placeholder(R.drawable.itcgo).into(btnAddFirma2)
                        } catch (e: Exception) {
                        }
                    } else {
                        try {
                            var strNameImg = mapInfogen!!["firma1"].toString()
                            strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                            strNameImg = strNameImg.replace("?alt=media", "")
                            val f = File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                            Picasso.get().load(f).into(btnAddFirma1)
                        } catch (e: Exception) {
                        }
                        try {
                            var strNameImg = mapInfogen!!["firma2"].toString()
                            strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                            strNameImg = strNameImg.replace("?alt=media", "")
                            val f = File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                            Picasso.get().load(f).into(btnAddFirma2)
                        } catch (e: Exception) {
                        }
                    }
                })
                if (editName.text.toString() != "" && editCC.text.toString() != "") {
                    Common.txtFirma = """
                        ${editName.text}
                        CC: ${editCC.text}
                        """.trimIndent()
                } else if (editName.text.toString() != "") {
                    Common.txtFirma = editName.text.toString()
                } else if (editCC.text.toString() != "") {
                    Common.txtFirma = "CC: " + editCC.text.toString()
                } else {
                    Common.txtFirma = ""
                }
                Common.nameFirma = "firma1"
                //mContext.startActivity(Intent(mContext, ActivityFirma::class.java))
            }
            btnAddFirma2.setOnClickListener {
                Common.waitingDialog!!.setOnDismissListener(DialogInterface.OnDismissListener {
                    if (Common.isConnected) {
                        try {
                            Picasso.get()
                                    .load(mapInfogen!!["firma1"].toString())
                                    .placeholder(R.drawable.itcgo).into(btnAddFirma1)
                        } catch (e: Exception) {
                        }
                        try {
                            Picasso.get()
                                    .load(mapInfogen!!["firma2"].toString())
                                    .placeholder(R.drawable.itcgo).into(btnAddFirma2)
                        } catch (e: Exception) {
                        }
                    } else {
                        try {
                            var strNameImg = mapInfogen!!["firma1"].toString()
                            strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                            strNameImg = strNameImg.replace("?alt=media", "")
                            val f = File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                            Picasso.get().load(f).into(btnAddFirma1)
                        } catch (e: Exception) {
                        }
                        try {
                            var strNameImg = mapInfogen!!["firma2"].toString()
                            strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                            strNameImg = strNameImg.replace("?alt=media", "")
                            val f = File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                            Picasso.get().load(f).into(btnAddFirma2)
                        } catch (e: Exception) {
                        }
                    }
                })
                Common.txtFirma = ""
                Common.nameFirma = "firma2"
               // mContext.startActivity(Intent(mContext, ActivityFirma::class.java))
            }
            if (Common.isConnected) {
                try {
                    Picasso.get()
                            .load(mapInfogen!!["firma1"].toString())
                            .placeholder(R.drawable.itcgo).into(btnAddFirma1)
                } catch (e: Exception) {
                }
                try {
                    Picasso.get()
                            .load(mapInfogen!!["firma2"].toString())
                            .placeholder(R.drawable.itcgo).into(btnAddFirma2)
                } catch (e: Exception) {
                }
            } else {
                try {
                    var strNameImg = mapInfogen!!["firma1"].toString()
                    strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                    strNameImg = strNameImg.replace("?alt=media", "")
                    val f = File("/storage/emulated/0/Android/data/com.app.tcgo/files/Pictures/$strNameImg")
                    Picasso.get().load(f).into(btnAddFirma1)
                } catch (e: Exception) {
                }
                try {
                    var strNameImg = mapInfogen!!["firma2"].toString()
                    strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                    strNameImg = strNameImg.replace("?alt=media", "")
                    val f = File("/storage/emulated/0/Android/data/com.app.tcgo/files/Pictures/$strNameImg")
                    Picasso.get().load(f).into(btnAddFirma2)
                } catch (e: Exception) {
                }
            }
            //            try{ Picasso.get().load(mapInfogen.get("firma1").toString()).into(btnAddFirma1); } catch (Exception e){ }
//            try{ Picasso.get().load(mapInfogen.get("firma2").toString()).into(btnAddFirma2); } catch (Exception e){ }
            try {
                if (mStringX["processDataClient"].toString() == "1") checkDatos.isChecked = true else checkDatos.isChecked = false
            } catch (e: Exception) {
            }
            try {
                editObsGeneral.setText(mapInfogen!!["obsGeneral"].toString())
            } catch (e: Exception) {
            }
            editObsGeneral.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    mapInfogen!!["obsGeneral"] = editObsGeneral.text.toString()
                    mapFormStruct["infogen"] = mapInfogen
                    val mapTask: MutableMap<String, Any> = HashMap()
                    mapTask["formStruct"] = mapFormStruct
                    Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)
                }

                override fun afterTextChanged(editable: Editable) {}
            })
        } //Firma
        else if ("3" === bool) {
            lineGroup.visibility = View.VISIBLE
            lineItem.visibility = View.GONE
            lineObs.visibility = View.GONE
            val gItem = myObj[position] as Map<String, Any>
            grupo.text = gItem["grupo"] as String?
            val arrayContenido = mapFormStruct["contenido"] as ArrayList<Any>?
            var x: MutableMap<String?, Any?> = HashMap()
            var y: MutableMap<String?, Any?> = HashMap()
            var cont = 0
            for (j in (mStringX["itemClick"] as ArrayList<Int?>?)!!) {
                when (cont) {
                    0 -> y = arrayContenido!![j!!] as MutableMap<String?, Any?>
                    else -> x = (y["grupItems"] as ArrayList<MutableMap<String?, Any?>>?)!![j!!]
                }
                cont++
            }
            x = (y["grupItems"] as ArrayList<MutableMap<String?, Any?>>?)!![position]
            var items = 0.0
            var cantItems = 0.0
            try {
                for (snapshotGroup in (x["grupItems"] as ArrayList<Map<String?, Any?>>?)!!) {
                    for (snapshotItem in (snapshotGroup["items"] as ArrayList<Map<String?, Any?>>?)!!) {
                        items = items + 1
                        var validation = true
                        try {
                            if (snapshotItem["active_check"].toString() != "0") {
                                if (snapshotItem["id_form_tip_foto"].toString() == "2") {
                                    when (snapshotItem["conforme"].toString()) {
                                        "0" -> if (snapshotItem["img"] != null) {
                                        } else {
                                            validation = false
                                        }
                                        else -> if (snapshotItem["conforme"] != null) {
                                        } else {
                                            validation = false
                                        }
                                    }
                                } else {
                                    if (snapshotItem["conforme"] != null) {
                                    } else {
                                        validation = false
                                    }
                                    if (snapshotItem["id_form_tip_foto"].toString() == "1") {
                                    } else if (snapshotItem["img"] != null) {
                                    } else {
                                        validation = false
                                    }
                                }
                            } else {
                                if (snapshotItem["id_form_tip_foto"].toString() == "1") {
                                } else if (snapshotItem["img"] != null) {
                                } else {
                                    validation = false
                                }
                            }
                            if (snapshotItem["id_form_tip_calif"].toString() == "1") {
                            } else if (snapshotItem["calif"] != null) {
                            } else {
                                validation = false
                            }
                            if (snapshotItem["id_form_tip_text"].toString() == "1" || snapshotItem["id_form_tip_text"].toString() == "2") {
                            } else if (!snapshotItem["obs"].toString().trim { it <= ' ' }.isEmpty()) {
                            } else {
                                validation = false
                            }
                        } catch (e: Exception) {
                            validation = false
                        }
                        if (validation) {
                            cantItems = cantItems + 1
                        } else {
                        }
                    }
                }
            } catch (e: Exception) {
            }
            for (snapshotItem in (x["items"] as ArrayList<Map<String?, Any?>>?)!!) {
                items = items + 1
                var validation = true
                try {
                    if (snapshotItem["active_check"].toString() != "0") {
                        if (snapshotItem["id_form_tip_foto"].toString() == "2") {
                            when (snapshotItem["conforme"].toString()) {
                                "0" -> if (snapshotItem["img"] != null) {
                                } else {
                                    validation = false
                                }
                                else -> if (snapshotItem["conforme"] != null) {
                                } else {
                                    validation = false
                                }
                            }
                        } else {
                            if (snapshotItem["conforme"] != null) {
                            } else {
                                validation = false
                            }
                            if (snapshotItem["id_form_tip_foto"].toString() == "1") {
                            } else if (snapshotItem["img"] != null) {
                            } else {
                                validation = false
                            }
                        }
                    } else {
                        if (snapshotItem["id_form_tip_foto"].toString() == "1") {
                        } else if (snapshotItem["img"] != null) {
                        } else {
                            validation = false
                        }
                    }
                    if (snapshotItem["id_form_tip_calif"].toString() == "1") {
                    } else if (snapshotItem["calif"] != null) {
                    } else {
                        validation = false
                    }
                    if (snapshotItem["id_form_tip_text"].toString() == "1" || snapshotItem["id_form_tip_text"].toString() == "2") {
                    } else if (!snapshotItem["obs"].toString().trim { it <= ' ' }.isEmpty()) {
                    } else {
                        validation = false
                    }
                } catch (e: Exception) {
                    validation = false
                }
                if (validation) {
                    cantItems = cantItems + 1
                } else {
                }
            }
            txtPorcentaje.text = ((Math.round(cantItems * 100 / items) as Int).toString() + "%")
            x["pocenGrup"] = txtPorcentaje.text.toString()
            val mapGrouItems = y["grupItems"] as ArrayList<Map<String?, Any?>>?
            mapGrouItems!![position] = x
            y["grupItems"] = mapGrouItems
            arrayContenido!![(mStringX["itemClick"] as ArrayList<Int?>?)!![0]!!] = y
            mapFormStruct["contenido"] = arrayContenido
            val mapTask: MutableMap<String, Any> = HashMap()
            mapTask["formStruct"] = mapFormStruct
            Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)

//                    final Map<String, Object> porc = new HashMap<>();
//                    porc.put("pocenGrup", txtPorcentaje.getText().toString());
//                    FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(mStringX.get("key").toString()+"/grupItems/"+gItem.get("key")).updateChildren(porc);
            if (cantItems * 100 / items == 100.0) {
                txtEstado.text = "Finalizado"
                txtEstado.setTextColor(Color.parseColor("#ffffff"))
                txtEstado.setBackgroundColor(Color.parseColor("#ff99cc00"))
            } else {
            }
        } //Sub-Grupos (Pendiente)
        else if ("4" === bool) {

            //ImageView imgEdit = listItem.findViewById(R.id.imgEdit);
            lineGroup.visibility = View.VISIBLE
            lineGroup.setOnClickListener { }
            lineItem.visibility = View.GONE
            lineObs.visibility = View.GONE
            txtPorcentaje.visibility = View.GONE
            //txtEstado.setVisibility(View.GONE);
            try {
                grupo.text = (myObj[position] as Map<String?, Any>)["txt"].toString()
            } catch (e: Exception) {
                grupo.text = "REQUISICIONES"
            }
            grupo.gravity = View.TEXT_ALIGNMENT_CENTER
            //imgEdit.setVisibility(View.GONE);
            mapReq = HashMap()
            mapReq = if (mapFormStruct["req"] != null) {
                mapFormStruct["req"] as MutableMap<String, Any?>?
            } else {
                HashMap()
            }
            cantItems = 0
            try {
                cantItems = (mapReq!!["items"] as ArrayList<Map<String?, Any?>?>?)!!.size
            } catch (e: Exception) {
            }
            txtEstado.text = cantItems.toString() + ""
            lineGroup.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val alertDialog = AlertDialog.Builder(mContext)
                    val inflater = (mContext as Activity).layoutInflater
                    val layout_tarera_req = inflater.inflate(R.layout.layout_tarea_req, null)
                    val editObsGeneral = layout_tarera_req.findViewById<EditText>(R.id.editObsGeneral)
                    val imgCerrar = layout_tarera_req.findViewById<ImageView>(R.id.imgCerrar)
                    val btnAddReq = layout_tarera_req.findViewById<ImageView>(R.id.btnAddReq)
                    val btnDellReq = layout_tarera_req.findViewById<ImageView>(R.id.btnDellReq)
                    val listReq = layout_tarera_req.findViewById<ListView>(R.id.listReq)
                    val nombre_form = layout_tarera_req.findViewById<TextView>(R.id.nombre_form)
                    try {
                        nombre_form.text = (myObj[position] as Map<String?, Any>)["txt"].toString()
                    } catch (e: Exception) {
                        nombre_form.text = "REQUISICIONES"
                    }
                    btnDellReq.visibility = View.GONE
                    cargarDatosList(editObsGeneral, listReq, btnAddReq, btnDellReq, true)
                    btnAddReq.setOnClickListener {
                        val mapTask: MutableMap<String, Any> = HashMap()
                        val avatarUpdate: MutableMap<String, Any> = HashMap()
                        avatarUpdate["cant"] = ""
                        avatarUpdate["comment"] = ""
                        var listItemReq: ArrayList<Map<String, Any>?>? = ArrayList()
                        if (mapReq!!["items"] != null) {
                            listItemReq = mapReq!!["items"] as ArrayList<Map<String, Any>?>?
                        } else {
                        }
                        listItemReq!!.add(avatarUpdate)
                        mapReq!!["items"] = listItemReq
                        mapFormStruct["req"] = mapReq
                        mapTask["formStruct"] = mapFormStruct
                        Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)
                        cargarDatosList(editObsGeneral, listReq, btnAddReq, btnDellReq, true)
                    }
                    btnDellReq.setOnClickListener {
                        val listItemReq = mapReq!!["items"] as ArrayList<Map<String, Any>>?
                        listItemReq!!.removeAt(listItemReq.size - 1)
                        mapReq!!["items"] = listItemReq
                        mapFormStruct["req"] = mapReq
                        val mapTask: MutableMap<String, Any> = HashMap()
                        mapTask["formStruct"] = mapFormStruct
                        Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)
                        cargarDatosList(editObsGeneral, listReq, btnAddReq, btnDellReq, false)
                    }
                    editObsGeneral.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                            mapReq!!["obsReq"] = editObsGeneral.text.toString()
                            mapFormStruct["req"] = mapReq
                            val mapTask: MutableMap<String, Any> = HashMap()
                            mapTask["formStruct"] = mapFormStruct
                            Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)
                        }

                        override fun afterTextChanged(editable: Editable) {}
                    })
                    if (cantItems == null || cantItems == 0) {
                        btnAddReq.callOnClick()
                    } else {
                    }
                    alertDialog.setView(layout_tarera_req)
                    val show = alertDialog.show()
                    imgCerrar.setOnClickListener { show.dismiss() }
                }

                private fun cargarDatosList(editObsGeneral: EditText, listReq: ListView, btnAddReq: ImageView, btnDellReq: ImageView, b: Boolean) {
                    var arrayList2: ArrayList<Any?>? = ArrayList()
                    if (mapReq!!["items"] as ArrayList<Any?>? != null) {
                        arrayList2 = mapReq!!["items"] as ArrayList<Any?>?
                    } else {
                    }
                    cantItems = arrayList2!!.size
                    try {
                        editObsGeneral.setText(mapReq!!["obsReq"].toString())
                    } catch (e: Exception) {
                    }
                    if (cantItems == 0 && b) {
                        btnAddReq.callOnClick()
                    }
                    if (cantItems != 0) {
                        btnDellReq.visibility = View.VISIBLE
                    } else {
                        btnDellReq.visibility = View.GONE
                    }
                   // val mAdapterChat = ListAdapterTareaReq(mContext, arrayList2, true, mapFormStruct, mStringX)
                    listReq.adapter = null
                  //  listReq.adapter = mAdapterChat
                    txtEstado.text = cantItems.toString() + ""
                }
            })
        } //requicisiones
        else if ("5" === bool) {

            //ImageView imgEdit = listItem.findViewById(R.id.imgEdit);
            lineGroup.visibility = View.VISIBLE
            lineItem.visibility = View.GONE
            lineObs.visibility = View.GONE
            txtPorcentaje.visibility = View.GONE
            //txtEstado.setVisibility(View.GONE);
            try {
                grupo.text = (myObj[position] as Map<String?, Any>)["txt"].toString()
            } catch (e: Exception) {
                grupo.text = "CONSUMOS"
            }
            grupo.gravity = View.TEXT_ALIGNMENT_CENTER
            //imgEdit.setVisibility(View.GONE);
            mapConsumo = if (mapFormStruct["consumo"] != null) {
                mapFormStruct["consumo"] as MutableMap<String, Any?>?
            } else {
                HashMap()
            }
            cantItems = 0
            try {
                cantItems = (mapConsumo!!["items"] as ArrayList<Map<String?, Any?>?>?)!!.size
            } catch (e: Exception) {
            }
            txtEstado.text = cantItems.toString() + ""
            lineGroup.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val alertDialog = AlertDialog.Builder(mContext)
                    val inflater = (mContext as Activity).layoutInflater
                    val layout_tarera_req = inflater.inflate(R.layout.layout_tarea_req, null)
                    val editObsGeneral = layout_tarera_req.findViewById<EditText>(R.id.editObsGeneral)
                    val imgCerrar = layout_tarera_req.findViewById<ImageView>(R.id.imgCerrar)
                    val btnAddReq = layout_tarera_req.findViewById<ImageView>(R.id.btnAddReq)
                    val btnDellReq = layout_tarera_req.findViewById<ImageView>(R.id.btnDellReq)
                    val listReq = layout_tarera_req.findViewById<ListView>(R.id.listReq)
                    val nombre_form = layout_tarera_req.findViewById<TextView>(R.id.nombre_form)
                    try {
                        nombre_form.text = (myObj[position] as Map<String?, Any>)["txt"].toString()
                    } catch (e: Exception) {
                        nombre_form.text = "CONSUMOS"
                    }
                    btnDellReq.visibility = View.GONE
                    editObsGeneral.setText("")
                    cargarDatosList(editObsGeneral, listReq, btnAddReq, btnDellReq, true)
                    btnAddReq.setOnClickListener {
                        val mapTask: MutableMap<String, Any> = HashMap()
                        val avatarUpdate: MutableMap<String, Any> = HashMap()
                        avatarUpdate["cant"] = ""
                        avatarUpdate["comment"] = ""
                        var listItemReq: ArrayList<Map<String, Any>?>? = ArrayList()
                        if (mapConsumo!!["items"] != null) {
                            listItemReq = mapConsumo!!["items"] as ArrayList<Map<String, Any>?>?
                        } else {
                        }
                        listItemReq!!.add(avatarUpdate)
                        mapConsumo!!["items"] = listItemReq
                        mapFormStruct["consumo"] = mapConsumo
                        mapTask["formStruct"] = mapFormStruct
                        Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)
                        cargarDatosList(editObsGeneral, listReq, btnAddReq, btnDellReq, true)
                    }
                    btnDellReq.setOnClickListener {
                        val listItemReq = mapConsumo!!["items"] as ArrayList<Map<String, Any>>?
                        listItemReq!!.removeAt(listItemReq.size - 1)
                        mapConsumo!!["items"] = listItemReq
                        mapFormStruct["consumo"] = mapConsumo
                        val mapTask: MutableMap<String, Any> = HashMap()
                        mapTask["formStruct"] = mapFormStruct
                        Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)
                        cargarDatosList(editObsGeneral, listReq, btnAddReq, btnDellReq, false)
                    }
                    editObsGeneral.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                            mapConsumo!!["obsReq"] = editObsGeneral.text.toString()
                            mapFormStruct["consumo"] = mapConsumo
                            val mapTask: MutableMap<String, Any> = HashMap()
                            mapTask["formStruct"] = mapFormStruct
                            Common.dbServices!!.document(mStringX["key"].toString()).update(mapTask)
                        }

                        override fun afterTextChanged(editable: Editable) {}
                    })
                    if (cantItems == null || cantItems == 0) {
                        btnAddReq.callOnClick()
                    } else {
                    }
                    alertDialog.setView(layout_tarera_req)
                    val show = alertDialog.show()
                    imgCerrar.setOnClickListener { show.dismiss() }
                }

                private fun cargarDatosList(editObsGeneral: EditText, listReq: ListView, btnAddReq: ImageView, btnDellReq: ImageView, b: Boolean) {
                    var arrayList2: ArrayList<Any?>? = ArrayList()
                    if (mapConsumo!!["items"] as ArrayList<Any?>? != null) {
                        arrayList2 = mapConsumo!!["items"] as ArrayList<Any?>?
                    } else {
                    }
                    cantItems = arrayList2!!.size
                    try {
                        editObsGeneral.setText(mapConsumo!!["obsReq"].toString())
                    } catch (e: Exception) {
                    }
                    if (cantItems == 0 && b) {
                        btnAddReq.callOnClick()
                    }
                    if (cantItems != 0) {
                        btnDellReq.visibility = View.VISIBLE
                    } else {
                        btnDellReq.visibility = View.GONE
                    }
                  //  val mAdapterChat = ListAdapterTareaReq(mContext, arrayList2, false, mapFormStruct, mStringX)
                    listReq.adapter = null
                    ///listReq.adapter = mAdapterChat
                    txtEstado.text = cantItems.toString() + ""
                }
            })
        } //Consumos
        else if ("6" === bool) {
            Log.e("6", "")
            //            //ImageView imgEdit = listItem.findViewById(R.id.imgEdit);
//            lineGroup.setVisibility(View.VISIBLE);
//            lineGroup.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
//            lineItem.setVisibility(View.GONE);
//            lineObs.setVisibility(View.GONE);
//            txtPorcentaje.setVisibility(View.GONE);
//            //txtEstado.setVisibility(View.GONE);
//            try { grupo.setText(((Map<String, Object>) myObj.get(position)).get("txt").toString());
//            } catch (Exception e){ grupo.setText("LECTOR DE CÓDIGOS"); }
//            grupo.setGravity(View.TEXT_ALIGNMENT_CENTER);
//            //imgEdit.setVisibility(View.GONE);
//
//            FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(Common.recIdTarea+"/formStruct/code")
//                    .addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            cantItems = 0;
//                            for (DataSnapshot snapshotItem : dataSnapshot.child("items").getChildren()) { cantItems++; }
//                            txtEstado.setText(cantItems+"");
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) { }
//                    });
//
//            lineGroup.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
//                    LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
//                    final View layout_tarera_req = inflater.inflate(R.layout.layout_tarea_req, null);
//
//                    final EditText editObsGeneral = layout_tarera_req.findViewById(R.id.editObsGeneral);
//                    final ImageView imgCerrar = layout_tarera_req.findViewById(R.id.imgCerrar);
//                    final ImageView btnAddReq = layout_tarera_req.findViewById(R.id.btnAddReq);
//                    final ImageView btnDellReq = layout_tarera_req.findViewById(R.id.btnDellReq);
//                    final ListView listReq = layout_tarera_req.findViewById(R.id.listReq);
//                    final TextView nombre_form = layout_tarera_req.findViewById(R.id.nombre_form);
//                    try { nombre_form.setText(((Map<String, Object>) myObj.get(position)).get("txt").toString());
//                    } catch (Exception e){ nombre_form.setText("LECTOR DE CÓDIGOS"); }
//
//                    btnDellReq.setVisibility(View.GONE);
//                    editObsGeneral.setVisibility(View.GONE);
//
//                    cargarDatosList(editObsGeneral,listReq,btnAddReq,btnDellReq, true);
//
//                    btnAddReq.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Intent i = new Intent(mContext, CameraActivity.class);
//                            i.putExtra("ItemCant",cantItems+"");
//                            mContext.startActivity(i);
//                        }
//                    });
//
//                    btnDellReq.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(Common.recIdTarea+"/formStruct/req")
//                                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                            cantItems = 0;
//                                            for (DataSnapshot snapshotItem : dataSnapshot.child("items").getChildren()) {
//                                                cantItems++;
//                                            }
//                                            FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(Common.recIdTarea+"/formStruct/req/items/"+(cantItems-1)).removeValue();
//                                            cargarDatosList(editObsGeneral,listReq,btnAddReq,btnDellReq, false);
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
//                                    });
//                        }
//                    });
//
//                    editObsGeneral.addTextChangedListener(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
//                        @Override
//                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                            final Map<String, Object> avatarUpdate = new HashMap<>();
//                            avatarUpdate.put("obsReq", editObsGeneral.getText().toString());
//                            FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(Common.recIdTarea+"/formStruct/req").updateChildren(avatarUpdate);
//                        }
//                        @Override
//                        public void afterTextChanged(Editable editable) { }
//                    });
//
//                    alertDialog.setView(layout_tarera_req);
//                    final AlertDialog show = alertDialog.show();
//
//                    imgCerrar.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) { show.dismiss(); }
//                    });
//                }
//
//                private void cargarDatosList(final EditText editObsGeneral, final ListView listReq, final ImageView btnAddReq, final ImageView btnDellReq, final boolean b) {
//                    FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(Common.recIdTarea+"/formStruct/code")
//                            .addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    try {
//                                        editObsGeneral.setText(dataSnapshot.child("obsReq").getValue().toString());}
//                                    catch (Exception e) { }
//                                    ArrayList<Object> arrayList2 = new ArrayList<>();
//                                    cantItems = 0;
//                                    for (DataSnapshot snapshotItem : dataSnapshot.child("items").getChildren()) {
//                                        cantItems++;
//                                        Map<String, Object> items = new HashMap<>();
//                                        items.put("key",snapshotItem.getKey());
//                                        arrayList2.add(items);
//                                    }
//                                    if (cantItems == 0 && b){
//                                        btnAddReq.callOnClick();
//                                    }
//                                    if (cantItems!=0){ btnDellReq.setVisibility(View.VISIBLE); } else { btnDellReq.setVisibility(View.GONE); }
//                                    //ListAdapterTareaReq mAdapterChat = new ListAdapterTareaReq(mContext, arrayList2,true);
//                                    listReq.setAdapter(null);
//                                    //listReq.setAdapter(mAdapterChat);
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) { }
//                            });
//                }
//            });
        } //prueba
        return listItem
    }

    private fun saveItem(arrayItems: ArrayList<MutableMap<String, Any?>>, position: Int, finalY: MutableMap<String, Any?>, finalX: MutableMap<String, Any?>, arrayContenido: ArrayList<Any>, ifPhoto: Boolean) {
        Common.mapTask = HashMap()
        if (!ifPhoto) {
            if (cantGrup == 1) {
                arrayItems[position - cantGrup] = finalY
                finalX["items"] = arrayItems
                arrayContenido[(mStringX["itemClick"] as ArrayList<Int?>?)!![0]!!] = finalX
                mapFormStruct["contenido"] = arrayContenido
                Common.mapTask!!.put("formStruct", mapFormStruct)
            } else if (cantGrup == 2) {
                arrayItems[position - cantGrup] = finalY
                finalX["items"] = arrayItems
                val mapSelGroup = arrayContenido[(mStringX["itemClick"] as ArrayList<Int?>?)!![0]!!] as MutableMap<String, Any?>
                val arrayGroup = mapSelGroup["grupItems"] as ArrayList<MutableMap<String, Any?>>?
                arrayGroup!![(mStringX["itemClick"] as ArrayList<Int?>?)!![1]!!] = finalX
                mapSelGroup["grupItems"] = arrayGroup
                mapFormStruct["contenido"] = mapSelGroup
                Common.mapTask!!.put("formStruct", mapFormStruct)
            }
            Common.key = mStringX["key"].toString()
            Common.saveTask()
        } else {
            Common.mapFormStruct = mapFormStruct
            Common.mStringX = mStringX
            Common.cantGrup = cantGrup
            Common.arrayItems = arrayItems
            Common.position = position
            Common.finalY = finalY
            Common.finalX = finalX
            Common.arrayContenido = arrayContenido
        }
    } //    private void guardarDatos(String conforme,String editTxt,String gItem) {
}