package com.example.tcgokotlin

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.ModulLogin.LoginActivity
import com.example.tcgokotlin.data.network.Repo
import com.example.tcgokotlin.domain.UseCase
import com.example.tcgokotlin.presentation.viewmodel.MainViewModel
import com.example.tcgokotlin.presentation.viewmodel.MainViewModelFactory
import com.example.tcgokotlin.sqliteDBHelper.LoadTaskSqlite
import com.example.tcgokotlin.utils.AnimationUtils.NotificationAlerter
import com.example.tcgokotlin.utils.Common
import com.example.tcgokotlin.utils.sharedPreferences.SesionManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_options.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class OptionsFragment : Fragment() {

    val REQUEST_IMAGE_CAPTURE = 1
    var  arrayService = ArrayList<MutableMap<String, Any>>()

    private var mediaPlayer: MediaPlayer? = null

    private val viewModelActivity by activityViewModels<MainViewModel> { MainViewModelFactory(UseCase(Repo())) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
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
                Tools.uploadImageUser(f, imgProfilePicture, requireContext(), requireActivity())
                fOut.flush()
                fOut.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun observeServicesPen1() {
        viewModelActivity.fetchArrayServicesPen1.observe(viewLifecycleOwner, { result ->
            when(result){
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val sm = SesionManager(requireContext())
                    arrayService = Tools.chargeArrayService()
                    val loadtasksql = LoadTaskSqlite()
                    loadtasksql.cargarservices(requireContext(), arrayService)
                    if (sm.getIntTasks() == 0) {
                        sm.setIntTasks(arrayService.size)
                    }
                    if (sm.getIntTasks() < arrayService.size) {
                        sm.setIntTasks(arrayService.size)
                        mediaPlayer?.start()
                        Toast.makeText(requireContext(),getString(R.string.do_you_have_new_tasks),Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(),"Ocurrio un error ${result.exception.message}",Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setListeners() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.tarea)
        mediaPlayer?.isLooping = false
        observeServicesPen1()
        tvNameUser.text = Common.documentUser?.get("nombres").toString()
        Glide.with(requireContext()).load((Common.documentUser!!["fotoConductor"] as Map<String?, Any>?)!!["url"].toString()).placeholder(R.drawable.ic_home_place_holder_profile).error(R.drawable.ic_home_place_holder_profile).into(imgProfilePicture)
        clLogout.setOnClickListener {
            showDialogLogout()
        }
        clClose.setOnClickListener {
            showDialogCloseApp()
        }
        clHelp.setOnClickListener {
            intentBrowser()
        }
        clChangePassword.setOnClickListener {
            showDialogChangePwd()
        }
        clProfile.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_options_to_editProfileFragment)
        }
        imgProfilePicture.setOnClickListener {
            Common.intTypeImg = 1
            Tools.dispatchTakePictureIntent(this)
        }
    }





    private fun intentBrowser() {
        val uri = Uri.parse("https://wa.me/573155118029")
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        requireContext().startActivity(browserIntent)
    }

    private fun showDialogLogout() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setIcon(R.drawable.ic_location_off)
        alertDialog.setTitle(getString(R.string.sign_off))
        alertDialog.setMessage(getString(R.string.you_are_sure_close_app))
        val layout_bnt: View = requireActivity().layoutInflater.inflate(R.layout.layout_btn, null)
        val btn1 = layout_bnt.findViewById<Button>(R.id.btn1)
        val btn2 = layout_bnt.findViewById<Button>(R.id.btn2)
        btn1.text = getString(R.string.cancel)
        btn2.text = getString(R.string.logout)
        alertDialog.setView(layout_bnt)
        val show = alertDialog.show()
        btn1.setOnClickListener { show.dismiss() }
        btn2.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            requireContext().startActivity(intent)
        }
    }

    private fun showDialogCloseApp() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setIcon(R.drawable.ic_location_off)
        alertDialog.setTitle(getString(R.string.logout_tcgo))
        alertDialog.setMessage(getString(R.string.you_are_sure_logout))
        val layout_bnt: View = requireActivity().layoutInflater.inflate(R.layout.layout_btn, null)
        val btn1 = layout_bnt.findViewById<Button>(R.id.btn1)
        val btn2 = layout_bnt.findViewById<Button>(R.id.btn2)
        btn1.text = getString(R.string.cancel)
        btn2.text = getString(R.string.logout)
        alertDialog.setView(layout_bnt)
        val show = alertDialog.show()
        btn1.setOnClickListener { show.dismiss() }
        btn2.setOnClickListener {
            requireActivity().finishAffinity()
        }
    }

    private fun showDialogChangePwd() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(getString(R.string.change_password_title))
        alertDialog.setMessage(getString(R.string.please_fill_all_information))
        val inflater = this.layoutInflater
        val layout_pwd = inflater.inflate(R.layout.layout_change_pwd, null)
        alertDialog.setView(layout_pwd)
        val show = alertDialog.show()
        val btnCancelar = layout_pwd.findViewById<Button>(R.id.btnCancel)
        val btnCambiar = layout_pwd.findViewById<Button>(R.id.btnChange)
        val edtPassword = layout_pwd.findViewById<View>(R.id.edtPassword) as TextView
        val edtNewPassword = layout_pwd.findViewById<View>(R.id.edtNewPassword) as TextView
        val edtRepeatPassword = layout_pwd.findViewById<View>(R.id.edtRepeatPassword) as TextView
        btnCancelar.setOnClickListener { show.dismiss() }
        btnCambiar.setOnClickListener {
            val waitingDialog = SpotsDialog.Builder().setContext(requireContext()).build()
            waitingDialog.show()
            if (validateFields(edtPassword, edtRepeatPassword, edtNewPassword)) {
                changePassword(edtPassword, edtRepeatPassword, waitingDialog, show)
            } else {
                waitingDialog.dismiss()
            }
        }
    }

    private fun validateFields(edtPassword: TextView, edtRepeatPassword: TextView, edtNewPassword: TextView): Boolean {
        var isValid = true
        if (edtNewPassword.text.toString().trim().isEmpty()) {
            edtNewPassword.error = getString(R.string.this_field_is_required)
            isValid = false
        }
        if (edtRepeatPassword.text.toString().trim().isEmpty()) {
            edtRepeatPassword.error = getString(R.string.this_field_is_required)
            isValid = false
        }
        if (isValid) {
            if (edtNewPassword.text.toString() != edtRepeatPassword.text.toString()) {
                edtNewPassword.error = getString(R.string.the_password_not_match)
                edtRepeatPassword.error = getString(R.string.the_password_not_match)
                isValid = false
            }
        }
        if (edtPassword.text.toString().trim().isEmpty()) {
            edtPassword.error = getString(R.string.this_field_is_required)
            isValid = false
        }
        return isValid
    }

    private fun changePassword(
        edtPassword: TextView,
        edtRepeatPassword: TextView,
        waitingDialog: android.app.AlertDialog,
        show: AlertDialog
    ) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        //Obtener credenciales de auth desde el usuario para re-autenticacion.
        //Ejemplo con solo el correo
        val credential = EmailAuthProvider.getCredential(email, edtPassword.text.toString())
        FirebaseAuth.getInstance().currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseAuth.getInstance().currentUser?.updatePassword(edtRepeatPassword.text.toString())?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Actualizar la informacion del Driver, columna password
                                Common.documentUser?.set("password", edtRepeatPassword.text.toString().trim())
                                Common.documentUser?.let {
                                    Common.dbDriversInformation?.document(it["key"].toString())?.update(it)?.addOnSuccessListener {
                                        show.dismiss()
                                        NotificationAlerter.createAlert(getString(R.string.the_password_was_changed), requireActivity())
                                    }?.addOnFailureListener {
                                        show.dismiss()
                                        NotificationAlerter.createAlertError(getString(R.string.the_password_was_changed_but_not_updated), requireActivity())
                                    }
                                    waitingDialog.dismiss()
                                }
                            } else {
                                NotificationAlerter.createAlertError(getString(R.string.the_password_cannot_changed), requireActivity())
                            }
                        }
                } else {
                    waitingDialog.dismiss()
                    NotificationAlerter.createAlertError(getString(R.string.password_old_incorrect), requireActivity())
                }
            }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }


    companion object { fun newInstance() = OptionsFragment() }
}