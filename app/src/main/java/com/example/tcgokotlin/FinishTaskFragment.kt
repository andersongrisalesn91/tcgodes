package com.example.tcgokotlin

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.utils.Common
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import io.paperdb.Paper
import kotlinx.android.synthetic.main.layout_rute_form.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FinishTaskFragment : Fragment() {

    var  positionSelected = -1
    val  mapLatLng = Common.selectedService!!["latLng"] as MutableMap<String, Any>?
    val  mapInfoCliente = Common.selectedService!!["infoCliente"] as MutableMap<String, Any>?
    val  mapHistoryDates = Common.selectedService!!["historial"] as MutableMap<String, Any?>?
    val  mapHistory = mapHistoryDates!![Common.formatDate.format(Date())] as MutableMap<String, Any?>?
    var boolExito = true
    var  boolFirma = false
    var  bool1 = 0
    var  bool2:Int = 0
    var  bool3:Int = 0

    var riderLatDest: String? = null
    var riderLngDest: String? = null

    var database = FirebaseDatabase.getInstance()

    var arrayService = ArrayList<MutableMap<String,Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arrayService = Tools.chargeArrayService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_rute_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       setListeners()
    }

    private fun setListeners() {
        if (Common.selectedService!!["prioridad"].toString().equals("VIP")) {
            imgPrioridad.visibility = View.VISIBLE
        } else {
            imgPrioridad.visibility = View.GONE
        }
        /*=====================       FIRMA DIGITAL   =====================*/
        checkDatos.setOnCheckedChangeListener { compoundButton, b ->
            val updateCheck: MutableMap<String, Any> = HashMap()
            updateCheck["processDataClient"] = if (b) "1" else "0"
            android.util.Log.e("tcgoapp", "p")
            Common.dbServices?.document(Common.recIdTarea)?.update(updateCheck)
        }
        btnAddFirma1.setOnClickListener {
            if (editName.text.toString() != "" && editCC.text.toString() != "") {
                Common.txtFirma = editName.text.toString() + "\nCC: " + editCC.text.toString()
            } else if (editName.text.toString() != "") {
                Common.txtFirma = editName.text.toString()
            } else if (editCC.text.toString() != "") {
                Common.txtFirma = "CC: " + editCC.text.toString()
            } else {
                Common.txtFirma = ""
            }
            if (Tools.getMetros(mapLatLng!!["latitude"].toString().toDouble(), mapLatLng["longitude"].toString().toDouble()) <= 200) {
                Common.firma = true
            } else {
                Toast.makeText(requireContext(), getString(R.string.you_must_200_meters_of_task), Toast.LENGTH_SHORT).show()
            }
        }

        /*=====================   FIN FIRMA DIGITAL   =====================*/
        txtDateRange.text = mapHistory?.get("TareaRangoIn").toString() + " - " + mapHistory?.get("TareaRangoFin").toString()
        txtTypeService.text = Common.listTipoServ!!.get(Common.selectedService!!.get("typeService").toString())
        txtEstado.text = Common.selectedService!!.get("estado").toString()
        txtPiezas.text = Common.selectedService!!.get("piezaCant").toString()
        txtNomClient.text = mapInfoCliente!!.get("nomClient").toString()
        txtDirection.text = mapInfoCliente.get("direction").toString()
        txtTel.text = mapInfoCliente.get("tel1").toString() + " - " + mapInfoCliente.get("tel2").toString()
        txtTimeEstimado.text = mapHistory?.get("TareaTiempoEst").toString()
        val arrayList = ArrayList<String>()
        for (entry: Map.Entry<Int?, String> in Common.listNoEx!!.entries) {
            arrayList.add(entry.value)
        }
        //llNoExitoso
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,arrayList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnRazon.adapter = adapter

        edtObservaciones.setText(Common.selectedService!!["rateObs"].toString())
        if (Common.selectedService!!["typeService"].toString().equals("RECOGIDA")) {
            txt3.text = "Fotos de soporte"
        } else {
            txt3.text = "Fotos entrega mercancía al cliente"
        }
        //Funciones
        rgTareas.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                when (checkedId) {
                    R.id.rbExitoso -> {
                        boolExito = true
                        llExitoso.visibility = View.VISIBLE
                        llNoExitoso.visibility = View.GONE
                        linearMerca.visibility = View.VISIBLE
                        txt2.text = "Fotos documento soporte"
                    }
                    R.id.rbNoExitoso -> {
                        boolExito = false
                        llExitoso.visibility = android.view.View.GONE
                        llNoExitoso.visibility = android.view.View.VISIBLE
                        linearMerca.visibility = android.view.View.GONE
                        txt2.text = "Foto notificación debajo de la puerta"
                    }
                    else -> {
                    }
                }
            }
        })
        btnFinalizar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (boolFirma) {
                    val updateInfo: MutableMap<String, Any?> = java.util.HashMap<String, Any?>() //Actualizar servicio
                    val updateInfo2: MutableMap<String, Any> = java.util.HashMap<String, Any>() //Actualizar historial
                    val arrayHistoryNote = java.util.ArrayList<MutableMap<String, Any>>() //Actualizar historyNote
                    val updateInfo3: MutableMap<String, Any> = java.util.HashMap<String, Any>() //Actualizar historyNote
                    updateInfo2["TareaHoraFin"] = Common.formatHora.format(java.util.Date())
                    updateInfo3["iduser"] = FirebaseAuth.getInstance().currentUser!!.uid
                    updateInfo3["tipouser"] = "Campo"
                    if (boolExito) {
                        updateInfo3["nota"] = ("Hora: " + Common.formatHora.format(java.util.Date()).toString() + "\n" +
                                "Usuario: " + Common.documentUser!!.get("nombres").toString() + " " + Common.documentUser!!.get("apellidos").toString() + "\n" +
                                "Nota: ".toString() + edtObservaciones.text.toString())
                        if ((bool1 > 0) && (bool2 > 0) && (bool3 > 0)) {
                            try {
                                updateInfo["estado"] = "Finalizado"
                                val mapEjec = Common.selectedService!!["ejecucion"] as MutableMap<String, Any>?
                                mapEjec!!["abierta"] = "0"
                                mapEjec["fechafin"] = Common.formatDate.format(java.util.Date())
                                updateInfo["ejecucion"] = mapEjec
                                if (Common.formatHora.parse(Common.formatHora.format(Date())).time <= Common.formatHora.parse(mapHistory!!["TareaRangoFin"].toString()).time) {
                                    if (!Common.selectedService!!["prsNoved"].toString().equals("1")) {
                                        updateInfo["prsNoved"] = "0"
                                    }
                                    updateInfo2["tipNov"] = "0"
                                } else {
                                    updateInfo["prsNoved"] = "1"
                                    updateInfo2["tipNov"] = "1"
                                }
                            } catch (e: java.text.ParseException) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Termina de tomar las fotos", Toast.LENGTH_SHORT).show()
                            return
                        }
                    } else {
                        if (bool1 > 0 && bool2 > 0) {
                            updateInfo3["nota"] = ("Hora: " + Common.formatHora.format(java.util.Date()).toString() + "\n".toString() +
                                    "Usuario: " + Common.documentUser!!.get("nombres").toString() + " " + Common.documentUser!!.get("apellidos").toString() + "\n" +
                                    "Motivo: " + spnRazon.selectedItem.toString() + "\n" +
                                    "Nota: " + edtObservaciones.text.toString())
                            updateInfo["estado"] = "No Exitosa"
                            updateInfo["prsNoved"] = "1"
                            val mapEjec = Common.selectedService!!["ejecucion"] as MutableMap<String, Any>?
                            mapEjec!!["abierta"] = "0"
                            mapEjec["fechafin"] = Common.formatDate.format(java.util.Date())
                            updateInfo["ejecucion"] = mapEjec
                            updateInfo2["tipNov"] = "4"
                            updateInfo2["motivoNoEx"] = spnRazon.selectedItem.toString()
                        } else {
                            Toast.makeText(requireContext(), "Termina de tomar las fotos", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    Common.boolPlay = true
                    val mDialog = ProgressDialog(requireContext())
                    mDialog.setMessage("Finalizando..")
                    mDialog.show()
                    FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(Common.recIdTarea).updateChildren(updateInfo).addOnCompleteListener(object : OnCompleteListener<Void?> {
                        override fun onComplete(task: com.google.android.gms.tasks.Task<Void?>) {
                            FirebaseDatabase.getInstance().getReference(Common.service_tbl).child(Common.recIdTarea).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val params: MutableMap<String, String> = HashMap()

                                    //try {
                                    params["workState"] = dataSnapshot.child("estado").value.toString()
                                    params["WebServise"] = dataSnapshot.child("WebServise").value.toString()
                                    params["codeWork"] = dataSnapshot.child("codetarea").value.toString()
                                    params["lat"] = dataSnapshot.child("latLng/latitude").value.toString()
                                    params["lng"] = dataSnapshot.child("latLng/longitude").value.toString()
                                    params["idClientDirect"] = dataSnapshot.child("infoLogic/idCliente").value.toString()
                                    params["nameDriver"] = dataSnapshot.child("infoUsers/0/nameDriver").value.toString()
                                    params["key"] = "ea5f5af802977407f5a0d5ac43b532d1"
                                    params["directionClientFinal"] = dataSnapshot.child("infoCliente/direction").value.toString()
                                    params["typeServ"] = Common.listTipoServ!!.get(dataSnapshot.child("typeService").value.toString()).toString()
                                    params["email"] = dataSnapshot.child("infoCliente/email").value.toString()
                                    params["sendEmail"] = dataSnapshot.child("infoCliente/enviarCorreo").value.toString()
                                    params["request"] = "finTarea"
                                    params["nameClientFinal"] = dataSnapshot.child("infoCliente/nomClient").value.toString()
                                    params["telClientfinal1"] = dataSnapshot.child("infoCliente/tel1").value.toString()
                                    params["telClientfinal2"] = dataSnapshot.child("infoCliente/tel2").value.toString()
                                    params["identClientFinal"] = dataSnapshot.child("infoCliente/identiClient").value.toString()
                                    params["pais"] = dataSnapshot.child("infoCliente/pais").value.toString()
                                    params["ciudad"] = dataSnapshot.child("infoCliente/ciudad").value.toString()
                                    params["depto"] = dataSnapshot.child("infoCliente/depto").value.toString()
                                    params["idAppDriver"] = FirebaseAuth.getInstance().currentUser!!.uid
                                    getPostData(params, requireContext())
                                    //}catch (Exception e){}
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                            database.getReference(Common.service_tbl).child(Common.recIdTarea + "/historial/" + Common.formatDate.format(java.util.Date())).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        var inStandBy: java.util.Date? = null
                                        var realTime: java.util.Date? = null
                                        val finStandBy = Common.formatHora.parse(Common.formatHora.format(java.util.Date()))
                                        try {
                                            inStandBy = Common.formatHora.parse(dataSnapshot.child("TareaHoraInStandBy").value.toString())
                                        } catch (e: java.lang.Exception) {
                                            inStandBy = Common.formatHora.parse(dataSnapshot.child("TareaHoraIn").value.toString())
                                        }
                                        try {
                                            realTime = Common.formatHora.parse(dataSnapshot.child("TareaTiempoReal").value.toString())
                                        } catch (e: java.lang.Exception) {
                                            realTime = Common.formatHora.parse("00:00")
                                        }
                                        finStandBy.minutes = finStandBy.minutes - inStandBy!!.minutes
                                        finStandBy.hours = finStandBy.hours - inStandBy.hours
                                        realTime!!.minutes = realTime.minutes + finStandBy.minutes
                                        realTime.hours = realTime.hours + finStandBy.hours
                                        val updateInfo: kotlin.collections.MutableMap<String, kotlin.Any> = java.util.HashMap<String, kotlin.Any>()
                                        updateInfo["standBy"] = 1
                                        database.getReference(Common.service_tbl).child(Common.recIdTarea).updateChildren(updateInfo)
                                        updateInfo2["TareaTiempoReal"] = Common.formatHora.format(realTime)
                                        updateInfo2["RecHoraFin"] = Common.formatHora.format(java.util.Date())
                                        updateInfo2["RecHoraIn"] = (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, kotlin.Any>?)!!.get("RecHoraIn").toString()
                                        updateInfo2["RecKmEst"] = (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, kotlin.Any>?)!!.get("RecKmEst").toString()
                                        updateInfo2["RecKmReal"] = io.paperdb.Paper.book().read<kotlin.Any>("mtRec")
                                        updateInfo2["RecTiempoEst"] = (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, kotlin.Any>?)!!.get("RecTiempoEst").toString()
                                        updateInfo2["RecTiempoReal"] = (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, kotlin.Any>?)!!.get("RecTiempoReal").toString()
                                        updateInfo2["TareaHoraFin"] = Common.formatHora.format(java.util.Date())
                                        database.getReference(Common.service_tbl).child(Common.recIdTarea + "/historial/" + Common.formatDate.format(java.util.Date())).updateChildren(updateInfo2).addOnCompleteListener(object : OnCompleteListener<java.lang.Void?> {
                                            override fun onComplete(task: com.google.android.gms.tasks.Task<java.lang.Void?>) {
                                                database.getReference(Common.service_tbl).child(Common.recIdTarea + "/historial/" + Common.formatDate.format(java.util.Date()) + "/historyNote").push().setValue(updateInfo3, object : DatabaseReference.CompletionListener {
                                                    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
                                                        recRegistrar(0, 0)
                                                        positionSelected = -1
                                                        mDialog.dismiss()
                                                        requireActivity().finish()
                                                    }
                                                })
                                            }
                                        })
                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                        }
                    })
                } else Toast.makeText(requireContext(), getString(R.string.please_complete_form), Toast.LENGTH_SHORT).show()
            }
        })
        btnCancelar.setOnClickListener { dialogStandBySingle(btnCancelar) }

        Common.typePhoto = "5"
        Common.ifSave = "normal"
        Common.mapImgNormal = HashMap<String?, Any>()
        try {
            if (mapHistory?.get("img") != null) {
                Common.mapImgNormal = mapHistory["img"] as MutableMap<String?, Any>?
            }
        } catch (e: java.lang.Exception) { }
        bool1 = 0
        bool2 = 0
        bool3 = 0
        if (Common.isConnected) {
            try {
                Picasso.get().load((Common.mapImgNormal!!.get("imgPhLugar1") as MutableMap<String?, Any>?)!!["url"].toString())
                        .placeholder(R.drawable.itcgo).into(imgPhLugar1)
            } catch (e: java.lang.Exception) {
                imgPhLugar1.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                Picasso.get().load((Common.mapImgNormal!!.get("imgPhLugar2") as MutableMap<String?, Any>?)!!["url"].toString())
                        .placeholder(R.drawable.itcgo).into(imgPhLugar2)
            } catch (e: java.lang.Exception) {
                imgPhLugar2.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                Picasso.get().load((Common.mapImgNormal!!.get("imgPhDoc1") as MutableMap<String?, Any>?)!!["url"].toString())
                        .placeholder(R.drawable.itcgo).into(imgPhDoc1)
            } catch (e: java.lang.Exception) {
                imgPhDoc1.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                Picasso.get().load((Common.mapImgNormal!!.get("imgPhDoc2") as MutableMap<String?, Any>?)!!["url"].toString())
                        .placeholder(R.drawable.itcgo).into(imgPhDoc2)
            } catch (e: java.lang.Exception) {
                imgPhDoc2.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                Picasso.get().load((Common.mapImgNormal!!.get("imgPhMer1") as MutableMap<String?, Any>?)!!["url"].toString())
                        .placeholder(R.drawable.itcgo).into(imgPhMer1)
            } catch (e: java.lang.Exception) {
                imgPhMer1.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                Picasso.get().load((Common.mapImgNormal!!.get("imgPhMer2") as MutableMap<String?, Any>?)!!["url"].toString())
                        .placeholder(R.drawable.itcgo).into(imgPhMer2)
            } catch (e: java.lang.Exception) {
                imgPhMer2.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                Picasso.get().load(Common.mapImgNormal!!.get("firma").toString()).placeholder(R.drawable.itcgo).into(btnAddFirma1)
            } catch (e: java.lang.Exception) {
                btnAddFirma1.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
        } else {
            try {
                var strNameImg = (Common.mapImgNormal!!.get("imgPhLugar1") as MutableMap<String?, kotlin.Any>?)!!["url"].toString()
                strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                strNameImg = strNameImg.replace("?alt=media", "")
                val f = java.io.File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                Picasso.get().load(f).into(imgPhLugar1)
            } catch (e: java.lang.Exception) {
                imgPhLugar1.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                var strNameImg = (Common.mapImgNormal!!.get("imgPhLugar2") as MutableMap<String?, Any>?)!!["url"].toString()
                strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                strNameImg = strNameImg.replace("?alt=media", "")
                val f = java.io.File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                Picasso.get().load(f).into(imgPhLugar2)
            } catch (e: java.lang.Exception) {
                imgPhLugar2.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                var strNameImg = (Common.mapImgNormal!!.get("imgPhDoc1") as MutableMap<String?, kotlin.Any>?)!!["url"].toString()
                strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                strNameImg = strNameImg.replace("?alt=media", "")
                val f = java.io.File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                Picasso.get().load(f).into(imgPhDoc1)
            } catch (e: java.lang.Exception) {
                imgPhDoc1.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                var strNameImg = (Common.mapImgNormal!!.get("imgPhDoc2") as MutableMap<String?, kotlin.Any>?)!!["url"].toString()
                strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                strNameImg = strNameImg.replace("?alt=media", "")
                val f = java.io.File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                Picasso.get().load(f).into(imgPhDoc2)
            } catch (e: java.lang.Exception) {
                imgPhDoc2.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                var strNameImg = (Common.mapImgNormal!!.get("imgPhMer1") as MutableMap<String?, Any>?)!!["url"].toString()
                strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                strNameImg = strNameImg.replace("?alt=media", "")
                val f = java.io.File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                Picasso.get().load(f).into(imgPhMer1)
            } catch (e: java.lang.Exception) {
                imgPhMer1.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                var strNameImg = (Common.mapImgNormal!!.get("imgPhMer2") as MutableMap<String?, Any>?)!!["url"].toString()
                strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                strNameImg = strNameImg.replace("?alt=media", "")
                val f = java.io.File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                Picasso.get().load(f).into(imgPhMer2)
            } catch (e: java.lang.Exception) {
                imgPhMer2.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            try {
                var strNameImg = Common.mapImgNormal!!.get("firma").toString()
                strNameImg = strNameImg.replace("https://firebasestorage.googleapis.com/v0/b/tcgo-27bde.appspot.com/o/images%2F", "")
                strNameImg = strNameImg.replace("?alt=media", "")
                val f = java.io.File("/storage/emulated/0/Android/data/com.app.worktic.tcgo/files/Pictures/$strNameImg")
                Picasso.get().load(f).into(btnAddFirma1)
            } catch (e: java.lang.Exception) {
                btnAddFirma1.setImageResource(R.drawable.ic_edit_black_24dp)
            }
        }
        imgPhLugar1.setOnClickListener {
            if (Tools.getMetros(mapLatLng!!["latitude"].toString().toDouble(), mapLatLng["longitude"].toString().toDouble()) <= 200) {
                if (Common.boolPhotos) {
                    Common.typePhoto = "5"
                    Common.ifSave = "normal"
                    Common.mapImgNormal = HashMap()
                    try {
                        if (mapHistory?.get("img") != null) {
                            Common.mapImgNormal = mapHistory["img"] as HashMap<String?, Any>
                        }
                    } catch (e: java.lang.Exception) {
                    }
                    Common.intTypeImg = 0
                    Common.txtImgHome = "imgPhLugar1"
                    Common.dispatchTakePictureIntent(requireParentFragment())
                } else {
                    Toast.makeText(requireContext(), "No puedes tomar más fotos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Debes estar a 200 metros de la tarea", Toast.LENGTH_SHORT).show()
            }
        }
        imgPhLugar2.setOnClickListener {
            if (Tools.getMetros(mapLatLng!!["latitude"].toString().toDouble(), mapLatLng["longitude"].toString().toDouble()) <= 200) {
                if (Common.boolPhotos) {
                    Common.typePhoto = "5"
                    Common.ifSave = "normal"
                    Common.mapImgNormal = HashMap<String?, Any>()
                    try {
                        if (mapHistory?.get("img") != null) {
                            Common.mapImgNormal = mapHistory["img"] as MutableMap<String?, Any>?
                        }
                    } catch (e: java.lang.Exception) {
                    }
                    Common.intTypeImg = 0
                    Common.txtImgHome = "imgPhLugar2"
                    Common.dispatchTakePictureIntent(requireParentFragment())
                } else {
                    Toast.makeText(requireContext(), "No puedes tomar más fotos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Debes estar a 200 metros de la tarea", Toast.LENGTH_SHORT).show()
            }
        }
        imgPhDoc1.setOnClickListener {
            if (Tools.getMetros(mapLatLng!!["latitude"].toString().toDouble(), mapLatLng["longitude"].toString().toDouble()) <= 200) {
                if (Common.boolPhotos) {
                    Common.typePhoto = "5"
                    Common.ifSave = "normal"
                    Common.mapImgNormal = HashMap()
                    try {
                        if (mapHistory?.get("img") != null) {
                            Common.mapImgNormal = mapHistory["img"] as MutableMap<String?, Any>?
                        }
                    } catch (e: java.lang.Exception) {
                    }
                    Common.intTypeImg = 0
                    Common.txtImgHome = "imgPhDoc1"
                    Common.dispatchTakePictureIntent(requireParentFragment())
                } else {
                    Toast.makeText(requireContext(), "No puedes tomar más fotos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Debes estar a 200 metros de la tarea", Toast.LENGTH_SHORT).show()
            }
        }
        imgPhDoc2.setOnClickListener {
            if (Tools.getMetros(mapLatLng!!["latitude"].toString().toDouble(), mapLatLng["longitude"].toString().toDouble()) <= 200) {
                if (Common.boolPhotos) {
                    Common.typePhoto = "5"
                    Common.ifSave = "normal"
                    Common.mapImgNormal = HashMap()
                    try {
                        if (mapHistory?.get("img") != null) {
                            Common.mapImgNormal = mapHistory["img"] as MutableMap<String?, Any>?
                        }
                    } catch (e: java.lang.Exception) {
                    }
                    Common.intTypeImg = 0
                    Common.txtImgHome = "imgPhDoc2"
                    Common.dispatchTakePictureIntent(requireParentFragment())
                } else {
                    Toast.makeText(requireContext(), "No puedes tomar más fotos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Debes estar a 200 metros de la tarea", Toast.LENGTH_SHORT).show()
            }
        }
        imgPhMer1.setOnClickListener {
            if (Tools.getMetros(mapLatLng!!["latitude"].toString().toDouble(), mapLatLng["longitude"].toString().toDouble()) <= 200) {
                if (Common.boolPhotos) {
                    Common.typePhoto = "5"
                    Common.ifSave = "normal"
                    Common.mapImgNormal = HashMap()
                    try {
                        if (mapHistory!!["img"] != null) {
                            Common.mapImgNormal = mapHistory["img"] as MutableMap<String?, Any>?
                        }
                    } catch (e: java.lang.Exception) {
                    }
                    Common.intTypeImg = 0
                    Common.txtImgHome = "imgPhMer1"
                    Common.dispatchTakePictureIntent(requireParentFragment())
                } else {
                    Toast.makeText(requireContext(), "No puedes tomar más fotos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Debes estar a 200 metros de la tarea", Toast.LENGTH_SHORT).show()
            }
        }
        imgPhMer2.setOnClickListener {
            if (Tools.getMetros(mapLatLng!!["latitude"].toString().toDouble(), mapLatLng["longitude"].toString().toDouble()) <= 200) {
                if (Common.boolPhotos) {
                    Common.typePhoto = "5"
                    Common.ifSave = "normal"
                    Common.mapImgNormal = HashMap<String?, Any>()
                    try {
                        if (mapHistory?.get("img") != null) {
                            Common.mapImgNormal = mapHistory["img"] as MutableMap<String?, Any>?
                        }
                    } catch (e: java.lang.Exception) {
                    }
                    Common.intTypeImg = 0
                    Common.txtImgHome = "imgPhMer2"
                    Common.dispatchTakePictureIntent(requireParentFragment())
                } else {
                    Toast.makeText(requireContext(), "No puedes tomar más fotos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Debes estar a 200 metros de la tarea", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getPostData(params: MutableMap<String, String>, context: android.content.Context?){
        if (!Common.isConnected){
            var  listImage: ArrayList<MutableMap<String, String>?>?
            try {
                listImage = Paper.book().read<java.util.ArrayList<MutableMap<String, String>?>>("listEmail")
                if (listImage == null)listImage = java.util.ArrayList<MutableMap<String, String>?>()
            }catch (e: java.lang.Exception){
                listImage = java.util.ArrayList<MutableMap<String, String>?>()
            }
            listImage!!.add(params)
            Paper.book().write("listEmail", listImage)
        } else {
            val queue = Volley.newRequestQueue(context)
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(Method.POST, Common.urlApi,
                    Response.Listener {
                    }, Response.ErrorListener {
            }){
                override fun getParams(): MutableMap<String, String> {
                    return params
                }
            }

            // Add the request to the RequestQueue.
            queue.add<String>(stringRequest)
        }
    }

    var  updateInfoRed: MutableMap<String, Any>? = null

    private fun recRegistrar(intEstado: Int, zIndex: Int){
        updateInfoRed = HashMap()
        val updateInfo2: MutableMap<String, Any> = HashMap()
        val updateInfo3: MutableMap<String, Any> = java.util.HashMap<String, Any>()
        val horaNow = Date()
        var  boolRecKmTiempo = true
        updateInfoRed = Common.mapRegistroUs!!["Recorrido"] as MutableMap<String, Any>?
        val  mapLatLng = arrayService.get(zIndex).get("latLng") as MutableMap<String, Any>
        riderLatDest = mapLatLng["latitude"].toString()
        riderLngDest = mapLatLng["longitude"].toString()
        if (((Common.mapRegistroUs!!["Recorrido"] as MutableMap<String?, Any>?)!!["Estado"].toString() == "0")){
            updateInfoRed!!["RecKmReal"] = io.paperdb.Paper.book().read<kotlin.Any>("mtRec")
            updateInfoRed!!["RecTiempoReal"] = "00:00"
        } else if (((Common.mapRegistroUs!!["Recorrido"] as MutableMap<String?, Any>?)!!["Estado"].toString() == "1")){
            if (!(Common.mapRegistroUs!!["Recorrido"] as MutableMap<String?, Any>?)!!["RecHoraIn"].toString().isEmpty()){
                try {
                    val horaIn = Date()
                    val recTiempo = Date()
                    horaIn.hours = Common.formatHora.parse((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get("RecHoraIn").toString()).hours
                    horaIn.minutes = Common.formatHora.parse((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get("RecHoraIn").toString()).minutes
                    recTiempo.hours = (horaNow.hours) - horaIn.hours
                    recTiempo.minutes = (horaNow.minutes) - horaIn.minutes
                    updateInfoRed!!["RecTiempoReal"] = Common.formatHora.format(recTiempo)
                    updateInfoRed!!["RecKmReal"] = Paper.book().read<Any>("mtRec").toString() + ""
                }catch (e: java.text.ParseException){
                    e.printStackTrace()
                }
            }
        } else {
            updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
            if (((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get("Estado").toString() != "5" &&
                            (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get("Estado").toString() != "2" &&
                            (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get("Estado").toString() != "3")){
                try {
                    val horaIn = java.util.Date()
                    val recTiempo = java.util.Date()
                    val tiempoReal = java.util.Date()
                    horaIn.hours = Common.formatHora.parse((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<kotlin.String?, kotlin.Any>?)!!.get("RecHoraIn2").toString()).hours
                    horaIn.minutes = Common.formatHora.parse((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<kotlin.String?, kotlin.Any>?)!!.get("RecHoraIn2").toString()).minutes
                    tiempoReal.hours = Common.formatHora.parse((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<kotlin.String?, kotlin.Any>?)!!.get("RecTiempoReal").toString()).hours
                    tiempoReal.minutes = Common.formatHora.parse((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<kotlin.String?, kotlin.Any>?)!!.get("RecTiempoReal").toString()).minutes
                    recTiempo.hours = (horaNow.hours) - horaIn.hours + tiempoReal.hours
                    recTiempo.minutes = (horaNow.minutes) - horaIn.minutes + tiempoReal.minutes
                    updateInfoRed!!["RecTiempoReal"] = Common.formatHora.format(recTiempo)
                    updateInfoRed!!["RecKmReal"] = io.paperdb.Paper.book().read<kotlin.Any>("mtRec").toString() + ""
                }catch (e: java.text.ParseException){
                    e.printStackTrace()
                }
            } else {}
        }
        updateInfoRed!!["Estado"] = intEstado.toString() + ""
        if (intEstado != 0){
            updateInfoRed!!["IdTarea"] = arrayService.get(zIndex).get("key").toString()
        } else {}
        val mapRed: kotlin.collections.MutableMap<kotlin.String, kotlin.Any?> = java.util.HashMap<kotlin.String, kotlin.Any?>()
        when(intEstado){
            0 -> {
                updateInfoRed!!["IdTarea"] = ""
                updateInfoRed!!["RecHoraFin"] = ""
                updateInfoRed!!["RecHoraIn"] = ""
                updateInfoRed!!["RecHoraIn2"] = ""
                updateInfoRed!!["RecKmEst"] = ""
                updateInfoRed!!["RecKmReal"] = ""
                updateInfoRed!!["RecTiempoEst"] = ""
                updateInfoRed!!["RecTiempoReal"] = ""
                Common.boolRecKm = false
                boolRecKmTiempo = false
                Paper.book().write("mtRec", 0.0)
                mapRed["Recorrido"] = updateInfoRed
                Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString()).update(mapRed)
            }
            1 -> {
                Common.boolRecKm = true
                updateInfo2["estado"] = "En Proceso"
                Common.dbServices!!.document(arrayService.get(zIndex).get("key").toString()).update(updateInfo2)
                if (((Common.mapRegistroUs!!["Recorrido"] as MutableMap<kotlin.String?, kotlin.Any>?)!!["Estado"].toString() == "0")) {
                    updateInfoRed!!["ubicacion"] = com.google.android.gms.maps.model.LatLng(
                            Common.mLastLocation!!.latitude,
                            Common.mLastLocation!!.longitude
                    )
                    updateInfoRed!!["RecHoraIn"] = Common.formatHora.format(horaNow)
                }
                Common.recIdTarea = arrayService.get(zIndex).get("key").toString()
            }
            2 -> {
                if (Common.recIdTarea !== arrayService.get(zIndex).get("key").toString()) {
                    if (Common.recIdTarea !== "" && Common.recIdTarea != null) {
                        updateInfo3["estado"] = "Pendiente"
                        Common.dbServices!!.document(Common.recIdTarea).update(updateInfo3)
                    } else {
                    }
                } else {
                }
                updateInfo2["estado"] = "En Proceso"
                var mapEje = arrayService.get(zIndex).get("ejecucion") as kotlin.collections.MutableMap<kotlin.String?, kotlin.Any?>
                mapEje = arrayService.get(zIndex).get("ejecucion") as kotlin.collections.MutableMap<kotlin.String?, kotlin.Any?>
                mapEje["abierta"] = "1"
                updateInfo2["ejecucion"] = mapEje
                Common.dbServices!!.document(arrayService.get(zIndex).get("key").toString()).update(updateInfo2)
                Common.recIdTarea = arrayService.get(zIndex).get("key").toString()
                Common.boolRecKm = false
                updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
                boolRecKmTiempo = false
                mapRed["Recorrido"] = updateInfoRed
                Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString()).update(mapRed)
            }
            3 -> {
                Common.recIdTarea = arrayService.get(zIndex).get("key").toString()
                val updateInfo: kotlin.collections.MutableMap<kotlin.String, kotlin.Any> = java.util.HashMap<kotlin.String, kotlin.Any>()
                updateInfo["iduser"] = FirebaseAuth.getInstance().currentUser!!.uid
                updateInfo["tipouser"] = "Campo"
                updateInfo["nota"] = ("Hora: " + Common.formatHora.format(java.util.Date()).toString() + "\n".toString() +
                        "Usuario: " + Common.documentUser!!.get("nombres").toString().toString() + " " + Common.documentUser!!.get("apellidos").toString().toString() + "\n".toString() +
                        "Nota: Se reporta tiempo muerto")
                val mapService: MutableMap<String, Any> = arrayService.get(zIndex)
                val mapHistoryDates = mapService["historial"] as MutableMap<String, Any?>?
                val mapHistory = mapHistoryDates!![Common.formatDate.format(Date())] as MutableMap<String, Any?>?
                val listHistoryNote = mapHistory!!["historyNote"] as java.util.ArrayList<Any>?

                listHistoryNote!!.add(updateInfo)
                mapHistory["historyNote"] = listHistoryNote
                mapHistoryDates[Common.formatDate.format(Date())] = mapHistory
                mapService["historial"] = mapHistoryDates
                Common.dbServices!!.document(Common.recIdTarea).update(mapService)

                updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
                Common.boolRecKm = false
                boolRecKmTiempo = false
                mapRed["Recorrido"] = updateInfoRed
                Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString()).update(mapRed)
            }
            4 -> {
                Common.boolRecKm = true
                updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)

                updateInfo2["estado"] = "En Proceso"
                if ((Common.recIdTarea !== "") && !Common.recIdTarea.isEmpty() && (Common.recIdTarea != null)) {
                    updateInfo3["estado"] = "Pendiente"
                    Common.dbServices!!.document(Common.recIdTarea).update(updateInfo3)
                }
                Common.dbServices!!.document(arrayService.get(zIndex).get("key").toString()).update(updateInfo2)
                Common.recIdTarea = arrayService.get(zIndex).get("key").toString()
            }
            5 -> {
                Common.boolRecKm = false
                updateInfoRed!!["IdTarea"] = ""
                updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
                updateInfoRed!!["RecKmEst"] = ""
                updateInfoRed!!["RecTiempoEst"] = ""
                boolRecKmTiempo = false
                mapRed["Recorrido"] = updateInfoRed
                Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString()).update(mapRed)
                updateInfo2["estado"] = "Pendiente"
                Common.dbServices!!.document(arrayService.get(zIndex).get("key").toString()).update(updateInfo2)
            }}
    }

    private fun dialogStandBySingle(btnCancelar: Button){
        val  builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("STAND BY")
        builder.setMessage("¿Porqué quieres colocar esta tarea en STAND BY?")
        val inflater = LayoutInflater.from(requireContext())
        val layout_stand_by = inflater.inflate(R.layout.stanby_layout, null)
        val  etObservation = layout_stand_by.findViewById<EditText>(R.id.etObservation)
        val  btnCancelarST = layout_stand_by.findViewById<Button>(R.id.btnCancelarSB)
        val  btnAceptarST = layout_stand_by.findViewById<Button>(R.id.btnIngresarST)
        builder.setView(layout_stand_by)
        val  show = builder.create()
        try {
            show.show()
        }catch (e: java.lang.Exception){}
        btnAceptarST.setOnClickListener {
            if (!etObservation.text.toString().isEmpty()) {
                val mDialog = ProgressDialog(requireContext())
                mDialog.setMessage("Actualizando..")
                mDialog.show()
                val updateDialog: kotlin.collections.MutableMap<kotlin.String, kotlin.Any> = java.util.HashMap<kotlin.String, kotlin.Any>()
                updateDialog["exitosa"] = 0
                database.getReference(Common.service_tbl).child(Common.recIdTarea + "/historial/" + Common.formatDate.format(Date())).updateChildren(updateDialog)
                val updateInfo: kotlin.collections.MutableMap<String, Any> = java.util.HashMap<kotlin.String, kotlin.Any>()
                updateInfo["iduser"] = FirebaseAuth.getInstance().currentUser!!.uid
                updateInfo["tipouser"] = "Campo"
                updateInfo["nota"] = ("Hora: " + Common.formatHora.format(Date()).toString() + "\n" +
                        "Usuario: " + Common.documentUser!!.get("nombres").toString() + " " + Common.documentUser!!.get("apellidos").toString() + "\n" +
                        "Nota: Tarea en STAND BY, " + etObservation.text.toString())
                database.getReference(Common.service_tbl).child(Common.recIdTarea + "/historial/" + Common.formatDate.format(java.util.Date()) + "/historyNote").updateChildren(updateInfo, DatabaseReference.CompletionListener { error, ref ->
                    val updateInfo2: MutableMap<String, Any> = java.util.HashMap<String, Any>()
                    updateInfo2["standBy"] = 1
                    database.getReference(Common.service_tbl).child(Common.recIdTarea).updateChildren(updateInfo2)
                    recRegistrar(5, 0)
                    Toast.makeText(requireContext(), "Tu tarea ha pasado a STAND BY", Toast.LENGTH_SHORT).show()
                    positionSelected = -1
                    mDialog.dismiss()
                    show.dismiss()
                })
            } else {
                Toast.makeText(requireContext(), "Texto invalido", Toast.LENGTH_SHORT).show()
                btnCancelar.callOnClick()
            }
        }
        btnCancelarST.setOnClickListener { show.dismiss() }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FinishTaskFragment()
    }
}