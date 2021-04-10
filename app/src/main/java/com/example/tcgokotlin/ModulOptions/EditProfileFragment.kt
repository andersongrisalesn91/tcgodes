package com.example.tcgokotlin.ModulOptions

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.ModulOptions.remote.OptionsDataSource
import com.example.tcgokotlin.R
import com.example.tcgokotlin.data.model.RequestUpdateName
import com.example.tcgokotlin.utils.AnimationUtils.NotificationAlerter
import com.example.tcgokotlin.utils.Common
import com.example.tcgokotlin.utils.Common.dispatchTakePictureIntent
import com.example.tcgokotlin.utils.sharedPreferences.SesionManager
import kotlinx.android.synthetic.main.layout_update_information.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class EditProfileFragment : Fragment() {

    val REQUEST_IMAGE_CAPTURE = 1
    private val viewModelOptions by viewModels<OptionsViewModel> { OptionsViewModelFactory(OptionsRepo(OptionsDataSource(requireContext()))) }
    private lateinit var sm: SesionManager
    private var namePhoto = ""
    private var isString = false
    private lateinit var imageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_update_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        checkImages()
        sm = SesionManager(requireContext())
        generateBasicAuthentication()
        getCurrentDates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            var imageBitmap = extras?.get("data") as Bitmap?
            val f: File
            val fOut: FileOutputStream
            try {
                imageBitmap = imageBitmap?.let { Tools.resizeBitmap(it, 700) }
                val canvas = imageBitmap?.let { Canvas(it) }
                val paint = Paint()
                val mText: String = Common.formatDate.format(Date()).toString() + " " + Common.formatHora.format(Date()) + "\n"
                var i = 25
                for (line in mText.split("\n").toTypedArray()) {
                    i += 50
                }
                val paint2 = Paint()
                paint2.setARGB(125, 117, 117, 117)
                canvas?.drawRect(0f, (imageBitmap?.height!! - i).toFloat(), imageBitmap.width.toFloat(), imageBitmap.height.toFloat(), paint2)
                for (line in mText.split("\n").toTypedArray()) {
                    i -= 50
                    canvas?.drawText(line, 0, line.toCharArray().size, 25f, (imageBitmap?.height!! - i).toFloat(), paint)
                }
                f = Tools.createImageFile(requireContext())
                fOut = FileOutputStream(f)
                imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                Common.boolTypePhoto = true
                Tools.uploadImageProfile(f, imageView, requireContext(), requireActivity(), namePhoto, layoutProgressBarEditProfile, isString)
                fOut.flush()
                fOut.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun postUpdateName() {
        viewModelOptions.fetchUpdateName.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Loading -> {
                    Tools.showView(layoutProgressBarEditProfile)
                }
                is Resource.Success -> {
                    val updateName = result.data
                    if (updateName.status_code == 200) {
                        Common.documentUser?.set("apellidos", edtLastName.text.toString())
                        Common.documentUser?.set("nombres", edtName.text.toString())
                        Common.documentUser?.let {
                            Common.dbDriversInformation?.document(it["key"].toString())?.update(it)?.addOnSuccessListener {
                                NotificationAlerter.createAlert(getString(R.string.your_names_updates_successfull), requireActivity())
                            }?.addOnFailureListener {
                                NotificationAlerter.createAlertError(getString(R.string.the_lastname_was_changed_but_not_updated), requireActivity())
                            }
                        }
                    } else {
                        NotificationAlerter.createAlertError(getString(R.string.problem_charge_names), requireActivity())
                    }
                    Tools.hideView(layoutProgressBarEditProfile)
                }
                is Resource.Failure -> {
                    NotificationAlerter.createAlertError(getString(R.string.problem_charge_names), requireActivity())
                    Tools.hideView(layoutProgressBarEditProfile)
                }
            }

        }
    }

    private fun getCurrentDates() {
        viewModelOptions.fetchCurrentDates.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Loading -> {
                    Tools.showView(layoutProgressBarEditProfile)
                }
                is Resource.Success -> {
                    val currentDates = result.data
                    val dates = currentDates.data
                    val seguro = dates.vencesegur
                    dateFechaVenSeg.updateDate(seguro.year, seguro.month - 1, seguro.day)
                    val pase = dates.vencepase
                    dateFechaVenLic.updateDate(pase.year, pase.month - 1, pase.day)
                    val tecnico = dates.ventetecnico
                    dateFechaVenTec.updateDate(tecnico.year, tecnico.month - 1, tecnico.day)
                    Tools.hideView(layoutProgressBarEditProfile)
                }
                is Resource.Failure -> {
                    NotificationAlerter.createAlertError(getString(R.string.problem_charge_dates), requireActivity())
                    Tools.hideView(layoutProgressBarEditProfile)
                }
            }
        }

    }

    private fun getRequestUpdateName(){
        val sm = SesionManager(requireContext())
        val idApp = sm.getInfo()?.uid?: ""
        val name = edtName.text.toString()
        val lastName = edtLastName.text.toString()
        val requestUpdateName = RequestUpdateName(idApp,name,lastName)
        sm.setRequestUpdateName(requestUpdateName)
    }

    private fun generateBasicAuthentication() {
        val user = Common.documentUser
        val userName = user?.get("driverCC") ?: ""
        val userUid = user?.get("UserUID") ?: ""
        val password = "3f9b0a1064c7d1db34b009571dd910fe"
        val auth = "${userName}:${password}"
        val basicAuthorization = "Basic ${Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)}"
        sm.setBasicAuthorization(basicAuthorization)
        sm.setUserUid(userUid.toString())
    }

    private fun validateFields():Boolean {
        var isValid = true
        if (edtName.text?.isEmpty() == true) {
            isValid = false
            edtName.error = getString(R.string.this_field_is_required)
        }
        if (edtLastName.text?.isEmpty() == true) {
            isValid = false
            edtLastName.error = getString(R.string.this_field_is_required)
        }
        return isValid
    }

    private fun checkImages() {
        if (Common.documentUser?.get("nombres") != null) {
            edtName.setText(Common.documentUser?.get("nombres").toString())
        }
        if (Common.documentUser?.get("apellidos") != null) {
            edtLastName.setText(Common.documentUser?.get("apellidos").toString())
        }
        checkConditionsImageString(fotoresibo, "fotoresibo")
        checkConditionsImageString(fotoSeguro, "fotoSeguro")
        checkConditionsImage(fotoCedulal1, "fotoCedulal1")
        checkConditionsImage(fotoCedulal2, "fotoCedulal2")
        checkConditionsImage(fotoLicencial1, "fotoLicencial1")
        checkConditionsImage(fotoLicencial2, "fotoLicencial2")
        checkConditionsImage(fotoTecnicomec, "fotoTecnicomec")
    }

    private fun checkConditionsImageString(iv:ImageView, nameImage:String) {
        if ((Common.documentUser?.get("images") as MutableMap<String, Any>)[nameImage] != "" && (Common.documentUser?.get("images") as MutableMap<String, Any>)[nameImage] != null) {
            iv.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
        } else {
            iv.setImageResource(R.drawable.ic_camera_alt_black_24dp)
        }
    }

    private fun checkConditionsImage(iv:ImageView, nameImage:String) {
        if (((Common.documentUser?.get("images") as MutableMap<String, Any>)[nameImage] as MutableMap<String,Any>)["url"] != "" && ((Common.documentUser?.get("images") as MutableMap<String, Any>)[nameImage] as MutableMap<String,Any>)["url"] != null) {
            iv.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
        } else {
            iv.setImageResource(R.drawable.ic_camera_alt_black_24dp)
        }
    }

    private fun setListeners() {
        dateFechaVenLic.isEnabled = false
        dateFechaVenSeg.isEnabled = false
        dateFechaVenTec.isEnabled = false

        fotoCedulal1.setOnClickListener {
            Common.intTypeImg = 2
            isString = false
            namePhoto = "fotoCedulal1"
            imageView = fotoCedulal1
            dispatchTakePictureIntent(this)
        }
        fotoCedulal2.setOnClickListener {
            Common.intTypeImg = 2
            isString = false
            namePhoto = "fotoCedulal2"
            imageView = fotoCedulal2
            dispatchTakePictureIntent(this)
        }
        fotoLicencial1.setOnClickListener {
            Common.intTypeImg = 2
            isString = false
            namePhoto = "fotoLicencial1"
            imageView = fotoLicencial1
            dispatchTakePictureIntent(this)
        }
        fotoLicencial2.setOnClickListener {
            Common.intTypeImg = 2
            isString = false
            namePhoto = "fotoLicencial2"
            imageView = fotoLicencial2
            dispatchTakePictureIntent(this)
        }
        fotoSeguro.setOnClickListener {
            Common.intTypeImg = 2
            isString = true
            namePhoto = "fotoSeguro"
            imageView = fotoSeguro
            dispatchTakePictureIntent(this)
        }
        fotoTecnicomec.setOnClickListener {
            Common.intTypeImg = 2
            isString = false
            namePhoto = "fotoTecnicomec"
            imageView = fotoTecnicomec
            dispatchTakePictureIntent(this)
        }
        fotoresibo.setOnClickListener {
            Common.intTypeImg = 2
            isString = true
            namePhoto = "fotoresibo"
            imageView = fotoresibo
            dispatchTakePictureIntent(this)
        }

        btnActualizar.setOnClickListener {
            if (validateFields()) {
                getRequestUpdateName()
                postUpdateName()
            }
        }
    }

    companion object {
        fun newInstance() = EditProfileFragment()
    }
}