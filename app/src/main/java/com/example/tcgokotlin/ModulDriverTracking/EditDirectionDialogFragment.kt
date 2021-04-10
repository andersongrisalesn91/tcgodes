package com.example.tcgokotlin.ModulDriverTracking

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.R
import com.example.tcgokotlin.utils.AnimationUtils.NotificationAlerter
import com.example.tcgokotlin.utils.Common
import com.google.android.gms.maps.model.LatLng
import dmax.dialog.SpotsDialog
import java.util.*


class EditDirectionDialogFragment: DialogFragment() {

    private var zIndex = 0
    private var startDir = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            zIndex = it.getInt("zIndex")
            startDir = it.getString("startDir")?:""
        }
    }

    @SuppressLint("InflateParams")
    @NonNull
    lateinit var alertDialog: Dialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = activity?.layoutInflater?.inflate(R.layout.layout_edit_dir, null)

        alertDialog = Dialog(requireContext())
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        v?.let { alertDialog.setContentView(it) }

        alertDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setListeners(alertDialog)
        alertDialog.show()

        return alertDialog
    }

    private fun setListeners(v: Dialog?) {
        val arrayService = Tools.chargeArrayService()
        val imgCerrar = v?.findViewById<ImageView>(R.id.imgCerrar)
        val btnSave = v?.findViewById<Button>(R.id.BtnSave)
        val editLocation = v?.findViewById<EditText>(R.id.etCurrentubication)
        val editDir = v?.findViewById<EditText>(R.id.etMyUbication)
        val editObs = v?.findViewById<EditText>(R.id.etObservations)
        val setLocation = "${Common.mLastLocation?.latitude.toString()}, ${Common.mLastLocation?.longitude.toString()}"
        editLocation?.setText(setLocation)
        editDir?.setText(arrayService[zIndex]["direction"].toString())
        editDir?.setText(startDir)
        imgCerrar?.setOnClickListener { v.dismiss() }
        btnSave?.setOnClickListener {
            val waitingDialog = SpotsDialog.Builder().setContext(requireContext()).build()
            waitingDialog.setMessage(getString(R.string.sended_request))
            waitingDialog.show()
            val mapClient: MutableMap<String, Any> = HashMap()
            val mapClientReq: MutableMap<String, Any> = HashMap()
            val mapUserRequest: MutableMap<String, Any> = HashMap()
            mapClientReq["userID"] = Common.documentUser?.get("key").toString()
            mapClientReq["userName"] = "${Common.documentUser?.get("nombres").toString()} ${Common.documentUser?.get("apellidos").toString()}"
            mapUserRequest["userID"] = ""
            mapUserRequest["userName"] = ""
            val mapClientReq2: MutableMap<String, Any> = HashMap()
            val infoClient = arrayService[zIndex]["infoCliente"] as MutableMap<String, Any>
            mapClientReq2["campo1"] = infoClient["campo1"] ?:  ""
            mapClientReq2["idClient"] = infoClient["idClient"] ?: ""
            mapClientReq2["nomClient"] = infoClient["nomClient"].toString()
            mapClientReq2["identiClient"] = infoClient["identiClient"].toString()
            mapClient["dataClient"] = mapClientReq2
            mapClient["idTarea"] = arrayService[zIndex]["key"].toString()
            mapClient["codeTarea"] = arrayService[zIndex]["codetarea"].toString()
            mapClient["coordinates"] = LatLng(Common.mLastLocation?.latitude ?: 0.0, Common.mLastLocation?.longitude ?: 0.0)
            mapClient["date"] = Common.formatDate.format(Date())
            mapClient["dateTramit"] = ""
            mapClient["direction"] = editDir?.text.toString()
            mapClient["idClient"] = Common.documentUser?.get("idClientw").toString()
            mapClient["notes"] = editObs?.text.toString()
            mapClient["processed"] = false
            mapClient["userRequest"] = mapClientReq
            mapClient["userTramit"] = mapUserRequest
            Common.db?.collection("client_update_request")?.add(mapClient)?.addOnSuccessListener {
                imgCerrar?.callOnClick()
                waitingDialog.dismiss()
                NotificationAlerter.createAlert(getString(R.string.request_sended), requireActivity())
            }?.addOnFailureListener {
                waitingDialog.dismiss()
                NotificationAlerter.createAlertError(getString(R.string.problem_sended_request), requireActivity())
            }
        }
    }
}