package com.example.tcgokotlin.ModulHome

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.ModulLogin.LoginActivity
import com.example.tcgokotlin.R
import com.example.tcgokotlin.Sincronizar.CargarEstados
import com.example.tcgokotlin.Sincronizar.SincronizarOnline
import com.example.tcgokotlin.Sincronizar.VerificarConex
import com.example.tcgokotlin.data.model.Chat
import com.example.tcgokotlin.data.model.Services
import com.example.tcgokotlin.data.network.Repo
import com.example.tcgokotlin.domain.UseCase
import com.example.tcgokotlin.presentation.viewmodel.MainViewModel
import com.example.tcgokotlin.presentation.viewmodel.MainViewModelFactory
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.sqliteDBHelper.LoadTaskSqlite
import com.example.tcgokotlin.utils.Common
import com.example.tcgokotlin.utils.sharedPreferences.SesionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.content_driver_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@ExperimentalCoroutinesApi
class DriverHomeFragment : Fragment() {

    val MY_PERMISSION_REQUEST_CODE = 7000
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var typeFilter: AutocompleteFilter? = null
    var locationManager: LocationManager? = null
    var mLocation: Location? = null
    val cantUpLoad: Int? = null
    val cantImg = 0
    var arrayService = ArrayList<MutableMap<String, Any>>()
    var nombreFoto: String = ""
    var path: String = ""

    //Date
    var dateLocation: Date? = null
    var dateLocationSeg: Date? = null

    val REQUEST_TAKE_PHOTO = 1
    val currentPhotoPath: String? = null
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference.child("images")

    var drivers: DatabaseReference? = null
    var firebaseStorage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    val REQUEST_IMAGE_CAPTURE = 1

    //Sistema de presencia
    val dataServiceDriver = FirebaseDatabase.getInstance().getReference(Common.service_tbl)
    val dataDriver = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)

    private var mediaPlayer: MediaPlayer? = null
    var mediaSMS: MediaPlayer? = null

    var arrayChat: ArrayList<Chat?> = ArrayList()
    var arrayServiceNow: ArrayList<Services?> = ArrayList()
    var arrayServiceWeek: ArrayList<Services?>? = ArrayList()
    var arrayServiceProg: ArrayList<Services?>? = ArrayList()

    var show: AlertDialog? = null
    var showJornada: AlertDialog? = null
    var alertDialog: AlertDialog.Builder? = null
    var blueImage: ImageView? = null

    var myDate: Date? = null
    var dateInJor: Date? = null
    var dateInBre: Date? = null
    var dateInAlm: Date? = null
    var dateFinJor: Date? = null
    var dateFinBre: Date? = null
    var dateFinAlm: Date? = null
    val countService = 0
    var countService2: Int = 0
    var cantPhoto1 = 0
    var cantPhoto2 = 0
    var ifImagePerfil = false
    var boolLocation = true
    var mReturn = false
    var boolBtnJornada = true

    var imageUri: Uri? = null
    var nameImage: String? = null

    private val viewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(UseCase(Repo()))).get(
            MainViewModel::class.java
        )
    }
    private val viewModelActivity by activityViewModels<MainViewModel> {
        MainViewModelFactory(
            UseCase(
                Repo()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_home, container, false)

    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                f = Common.createImageFile()
                fOut = FileOutputStream(f)
                imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                Common.boolTypePhoto = true
                Common.saveOfflineImg()
                fOut.flush()
                fOut.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fg = FuncionesGenerales()
        val so = SincronizarOnline()

        val campof = fg.parametro(requireContext(), "CAMPOFENC")
        val latitud_ult = fg?.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg?.parametro(requireContext(), "ULT_LON")
        var latact = latitud_ult
        var lonact = longitud_ult
        val queryFoto =
            "UPDATE '101_REGISTRO' set " + campof + "nfoto='" + nombreFoto + "' , " + campof + "hora='" + fg.fechaActual(
                3
            ) + "' , " + campof + "lat='" + latact + "' , " + campof + "lon='" + lonact + "' " +
                    "where useruid='" + fg.useruid(requireContext()) +
                    "' and fecha='" + fg.fechaActual(1) + "';"
        fg.ejecDB(requireContext(), queryFoto)
        try {

            so.cargarFotoUnica(
                requireContext(),
                nombreFoto,
                "update '101_REGISTRO' set " + campof + "sinc='1' " +
                        "where useruid='" + fg.useruid(requireContext()) + "' and fecha='" + fg.fechaActual(
                    1
                ) + "';"
            )
        } catch (e: Exception) {
            Log.i("Fallo-Sinc " + campof + "Foto: ", e.toString())
        } finally {
            try {
                so.sincronizarRegistro101(requireContext())
            } finally {
                findNavController().navigate(R.id.navigation_home)
            }
        }

    }


    private fun observers() {
        observeServicesPen1()
    }


    private fun observeServicesPen1() {
        viewModelActivity.fetchArrayServicesPen1.observe(viewLifecycleOwner, { result ->
            when (result) {
                is Resource.Loading -> {
                }
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
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.do_you_have_new_tasks),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(
                        requireContext(),
                        "Ocurrio un error ${result.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var fg = FuncionesGenerales()
        fg.act_param(requireContext(), "USERUID", Common.documentUser?.get("UserUID").toString())
        fg.act_param(requireContext(), "IDCLIENTW", Common.documentUser?.get("idClientw").toString())
        fg.ejecDB(requireContext(), "INSERT OR IGNORE INTO '101_REGISTRO' (useruid,fecha,idclientw) VALUES ('" + Common.documentUser?.get("UserUID").toString() + "','" + fg.fechaActual(1) + "','" + Common.documentUser?.get("idClientw").toString() +"');")
        val vc = VerificarConex()
        val net = vc.revisarconexión(requireContext())
        if (net) {
            val ce = CargarEstados()
            ce.reload_infogen(requireContext())
        }
        val bk = Backups()
        bk.backupdDatabase(requireContext());
        Common.mContext = requireContext()
        Common.mLayoutInflater = this.layoutInflater
        Common.mDialog2 = ProgressDialog(requireContext())
        Common.waitingDialog = ProgressDialog(requireContext())
        Common.mDialog2?.setMessage(getString(R.string.charge_data))
        Common.mDialog2?.setOnDismissListener {
            when (Common.intReg) {
                0 -> Common.setRegistro()
                1 -> Common.setRegFailed()
                2 -> Common.setRegistroUs(Common.idDayReg)
                3 -> {
                    Common.setServices()
                    setFormJornada()
                }
                4 -> Common.saveImgHome()
                5 -> setFormJornada()
            }
        }
        val sm = SesionManager(requireContext())
        Common.UID = sm.getInfo()?.uid ?: ""
        Common.EMAIL = sm.getInfo()?.email ?: ""
        Common.setDbSettings()
        Common.setDriversInformation("email", Common.EMAIL)
        Common.dbDriversInformation = Common.db?.collection("DriversInformation")
        try {
            Common.listenerRegDriversInformation?.remove()
        } catch (e: Exception) {
        }
        Common.listenerRegDriversInformation = Common.dbDriversInformation?.whereEqualTo(
            "USERUID",
            Common.UID
        )
            ?.addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                for (change in querySnapshot!!.documentChanges) {
                    Common.documentUser = change.document.data
                    Common.documentUser?.put("key", querySnapshot.documents[0].id)
                }
            }
        Common.setRegistro()
        Paper.init(requireContext())
        mediaSMS = MediaPlayer.create(requireContext(), R.raw.sms)
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.tarea)
        mediaSMS?.isLooping = false
        mediaPlayer?.isLooping = false
        try {
            if (Common.documentUser?.get("estadoDriver").toString()
                    .equals("DISPONIBLE CON TAREAS") || Common.documentUser?.get(
                    "estadoDriver"
                ).toString().equals("DISPONIBLE SIN TAREAS") ||
                Common.documentUser?.get("estadoDriver").toString().equals("INACTIVO")
            ) {
            } else {
                linLayContent.visibility = View.GONE
                linLayNotPerm.visibility = View.VISIBLE
                txtInfo.text =
                    Common.documentUser?.get("estadoDriver").toString() + " " + txtInfo.text
            }
        } catch (e: Exception) {
            requireContext().startActivity(Intent(context, LoginActivity::class.java))
        }
        estadoDriver.setText(Common.documentUser?.get("estadoDriver").toString())
        setListeners()
        //Inicializar Firebase Storage
        initFirebaseStorage()
        //Inicializar Vista

        //Places API
        typeFilter = AutocompleteFilter.Builder()
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
            .setTypeFilter(3)
            .build()
        updateFirebaseToken()
        estadoDriver.setText(Common.documentUser?.get("estadoDriver").toString())


        //consulta api tcgo
        val params: MutableMap<String, String> = HashMap()
        params["idclient"] = Common.documentUser?.get("idClientw").toString()
        try {
            if (Paper.book().read<Any>("fecha").toString() == Common.formatDate.format(Date())) {
                Common.listTipoServ = Paper.book().read("listTipoServ")
                Common.listNoEx = Paper.book().read("listNoEx")
                Common.listSelector = Paper.book().read("listSelector")
                Common.listSelectorElement = Paper.book().read("listSelectorElement")
                textImg1.text = (Paper.book()
                    .read<Any>("DataClient") as JSONArray).getJSONObject(0)["nameSop1"].toString()
                textImg2.text = (Paper.book()
                    .read<Any>("DataClient") as JSONArray).getJSONObject(0)["nameSop2"].toString()
                Common.hashCode =
                    (Paper.book().read<Any>("DataKeyClient") as JSONArray).getJSONObject(
                        0
                    )["hash_code"].toString()
                Common.boolSMS = true
            } else {
                Paper.book().write("fecha", Common.formatDate.format(Date()))
                getPostData(params, requireContext(), "getData")
            }
        } catch (e: Exception) {
            Paper.book().write("fecha", Common.formatDate.format(Date()))
            getPostData(params, requireContext(), "getData")
        }
        checkLocationAndInternet()
        observers()
    }

    private fun checkLocationAndInternet() {
        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }
        if (!gps_enabled && !network_enabled) {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setIcon(R.drawable.ic_location_off)
            alertDialog.setTitle(getString(R.string.location_inactive))
            alertDialog.setMessage(getString(R.string.please_verify_your_location))
            val layout_bnt: View = getLayoutInflater().inflate(R.layout.layout_btn, null)
            val btn1 = layout_bnt.findViewById<Button>(R.id.btn1)
            val btn2 = layout_bnt.findViewById<Button>(R.id.btn2)
            btn2.text = getString(R.string.active_location)
            btn1.visibility = View.GONE
            alertDialog.setView(layout_bnt)
            val show = alertDialog.show()
            btn2.setOnClickListener {
                requireActivity().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                mReturn = false
                show.dismiss()
            }
            try {
                Common.mLastLocation = mLocation
            } catch (e: Exception) {
            }
        }
    }

    private fun setListeners() {
        val myDateReference = FirebaseDatabase.getInstance().getReference(Common.user_register_tb1)
        myDate = Date()
        val currentDate: String = Common.formatDate.format(myDate)
        btnBreack.isEnabled = false
        btnAlmuerzo.isEnabled = false
        btnPermiso.isEnabled = false
        spnPermiso.isEnabled = false
        Common.ifJornada = 0
        Common.ifBreack = 0
        Common.ifAlmuerzo = 0
        Common.ifPermiso = 0
        cantPhoto1 = 0
        cantPhoto2 = 0

        var fg = FuncionesGenerales()
        imgPhkm.setOnClickListener {
            tomarFoto("km1")
        }
        imgPhkm2.setOnClickListener {
            tomarFoto("km2")
        }
        imgPhVh.setOnClickListener {
            tomarFoto("vh1")
        }
        imgPhVh2.setOnClickListener {
            tomarFoto("vh2")
        }

        btnPermiso.setOnClickListener {
            val currentHora: String = Common.dateFormatHora.format(Date())
            val initmp = fg.getQ1(requireContext(),"Select inciopermiso from '101_REGISTRO'  where useruid='" + fg.parametro(requireContext(),
                "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                "FECHA_ACT") + "';")
            val fintmp = fg.getQ1(requireContext(),"Select finpermiso from '101_REGISTRO'  where useruid='" + fg.parametro(requireContext(),
                "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                "FECHA_ACT") + "';")
            if (initmp=="0" || initmp==""){
                fg.ejecDB(requireContext(),
                    "UPDATE '101_REGISTRO' set iniciopermiso='" + currentHora
                            + "' where useruid='" + fg.parametro(requireContext(),
                        "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                        "FECHA_ACT") + "';")
                val sincO = SincronizarOnline()
                sincO.sincronizarRegistro101(requireContext())
                btnPermiso.setText("FINALIZAR PERMISO")
            }else if (fintmp=="0" || fintmp==""){
                val inflaterpop =
                    requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                popupConfirmar(inflaterpop,"4")
            }else{
                Toast.makeText(requireContext(),
                    "Ya inicio y finalizo permiso",
                    Toast.LENGTH_LONG).show()
            }
        }
        btnJornada.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                val updateRegister: Map<String, Any> = HashMap()
                val updateDriver: MutableMap<String, Any> = HashMap()
                if (cantPhoto1 >= 1 && cantPhoto2 >= 1) {
                    if (Common.ifJornada == 0) {
                        val currentHora: String = Common.dateFormatHora.format(Date())

                        if (Common.arrayServiceClose != null) {
                            Common.arrayServiceClose = closeTask(Common.arrayServiceClose, 2)
                        }
                        if (Common.arrayServiceClose1 != null) {
                            Common.arrayServiceClose1 = closeTask(Common.arrayServiceClose1, 2)
                        }
                        val mapTiempos =
                            Common.mapRegistroUs?.get("Tiempos") as MutableMap<String, Any?>
                        mapTiempos["InicioJornada"] = currentHora
                        val latitud_ult = fg?.parametro(requireContext(), "ULT_LAT")
                        val longitud_ult = fg?.parametro(requireContext(), "ULT_LON")
                        var latact = latitud_ult
                        var lonact = longitud_ult
                        fg.ejecDB(
                            requireContext(),
                            "UPDATE '101_REGISTRO' set gpslat='" + latact + "' , gpslon='" + lonact + "' , iniciojornada='" + currentHora
                                    + "' where useruid='" + fg.parametro(
                                requireContext(),
                                "USERUID"
                            ) + "' and fecha='" + fg.fechaActual(
                                1
                            ) + "';"
                        )
                        try {
                            val sincO = SincronizarOnline()
                            sincO.sincronizarRegistro101(requireContext())
                        } catch (e: Exception) {
                            Log.i("No cargo registros", "101_REGISTRO: " + e.message.toString())
                        }
                        mapTiempos["FinJornada"] = null
                        val location: MutableMap<String, Any> = HashMap()
                        location["hora"] = Common.formatHora.format(Date())
                        location["latLng"] = LatLng(
                            Common.mLastLocation?.latitude ?: 0.0,
                            Common.mLastLocation?.longitude ?: 0.0
                        )
                        /*var lat = Common.mLastLocation?.latitude ?: 0.0
                        var lon = Common.mLastLocation?.longitude ?: 0.0
                        fg.ejecDB(requireContext(),"UPDATE '101_REGISTRO' set gpslat='" + lat + "' , gpslon='" +  lon
                                + "' where useruid='" + fg.parametro(requireContext(),"USERUID") + "' and fecha='" + fg.parametro(requireContext(),"FECHA_ACT") + "';")*/
                        Common.mapRegistroUs?.put("Tiempos", mapTiempos)
                        Common.mapRegistroUs?.let {
                            Common.dbRegistroUs?.document(
                                Common.mapRegistroUs?.get(
                                    "key"
                                ).toString()
                            )?.update(it)
                        }
                        setFormJornada()
                        dataServiceDriver.orderByChild("idDriver")
                            .equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.value != null) {
                                        var countService = 0
                                        var countService2 = 0
                                        var boolFinJornada = false
                                        for (snapshot in dataSnapshot.children) {
                                            countService++
                                        }
                                        for (snapshot in dataSnapshot.children) {
                                            countService2++
                                            if (Common.formatDate.format(Date()).equals(
                                                    snapshot.child(
                                                        "dateServ"
                                                    ).value.toString()
                                                )
                                            ) if (snapshot.child("estado").value.toString() == "Pendiente" || snapshot.child(
                                                    "estado"
                                                ).value.toString() == "En Proceso"
                                            ) {
                                                boolFinJornada = true
                                            }
                                            if (countService == countService2) {
                                                if (boolFinJornada) {
                                                    updateDriver["estadoDriver"] =
                                                        getString(R.string.available_with_tasks)
                                                    estadoDriver.text =
                                                        getString(R.string.available_with_tasks)
                                                } else {
                                                    updateDriver["estadoDriver"] =
                                                        getString(R.string.available_without_tasks)
                                                    estadoDriver.setText(getString(R.string.available_without_tasks))
                                                }
                                                dataDriver.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                                    .updateChildren(
                                                        updateDriver
                                                    )
                                            }
                                        }
                                    } else {
                                        updateDriver["estadoDriver"] =
                                            getString(R.string.available_without_tasks)
                                        estadoDriver.text =
                                            getString(R.string.available_without_tasks)
                                        dataDriver.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                            .updateChildren(
                                                updateDriver
                                            )
                                    }
                                    myDateReference.child(currentDate)
                                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .updateChildren(
                                            updateRegister
                                        )
                                    myDateReference.child(currentDate)
                                        .child(FirebaseAuth.getInstance().currentUser!!.uid + "/locatios")
                                        .push().setValue(
                                            location
                                        )
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                    }
                    if (Common.ifJornada == 1) {
                        dateFinJor = Date()
                        dateFinJor?.hours = dateFinJor?.getHours()?.minus(3)!!
                        if (dateInJor?.getTime()!! <= dateFinJor?.getTime()!!) {
                            dateFinJor?.setHours(dateFinJor?.getHours()!! + 3)
                            if (Common.ifBreack != 1) {
                                if (Common.ifAlmuerzo == 1) {
                                    if (Common.ifPermiso != 1) {
                                        val alertDialogJornada =
                                            AlertDialog.Builder(requireContext())
                                        alertDialogJornada.setIcon(R.drawable.ic_info)
                                        alertDialogJornada.setTitle(getString(R.string.finis_working_day))
                                        val layout_bnt: View = layoutInflater.inflate(
                                            R.layout.layout_btn,
                                            null
                                        )
                                        val btn1 = layout_bnt.findViewById<Button>(R.id.btn1)
                                        val btn2 = layout_bnt.findViewById<Button>(R.id.btn2)
                                        btn1.text = getString(R.string.finish)
                                        btn2.text = getString(R.string.aceppt)
                                        val view1 = alertDialogJornada.setView(layout_bnt)
                                        var strDialog = getString(R.string.do_you_have)
                                        var intCantServ = 0
                                        var intCantServ1 = 0
                                        try {
                                            if (Common.arrayServicePro?.size != 0) {
                                                intCantServ += Common.arrayServicePro!!.size
                                                strDialog += if (Common.arrayServicePro?.size!! > 1) Common.arrayServicePro?.size.toString() + getString(
                                                    R.string.tasks_opens
                                                ) else getString(R.string.single_task_open)
                                                strDialog += getString(R.string.in_process)
                                            }
                                        } catch (e: Exception) {
                                        }
                                        try {
                                            if (Common.arrayServicePen?.size != 0) {
                                                intCantServ += Common.arrayServicePen!!.size
                                                strDialog += if (Common.arrayServicePen!!.size > 1) Common.arrayServicePen!!.size.toString() + "Tareas Pendientes Abiertas.\n" else "1 Tarea Pendiente Abierta.\n"
                                            }
                                        } catch (e: Exception) {
                                        }
                                        if (intCantServ > 0) {
                                            strDialog += "Estas tareas se reagendarán para mañana.\n\n"
                                        }
                                        try {
                                            if (Common.arrayServicePro1?.size != 0) {
                                                intCantServ1 += Common.arrayServicePro1!!.size
                                                strDialog += if (Common.arrayServicePro1?.size!! > 1) Common.arrayServicePro1?.size.toString() + getString(
                                                    R.string.tasks
                                                ) else getString(R.string.one_task)
                                                strDialog += getString(R.string.in_process)
                                            }
                                        } catch (e: Exception) {
                                        }
                                        try {
                                            if (Common.arrayServicePen1?.size != 0) {
                                                intCantServ1 += Common.arrayServicePen1!!.size
                                                strDialog += if (Common.arrayServicePen1!!.size > 1) Common.arrayServicePen1!!.size.toString() + getString(
                                                    R.string.tasks_pending
                                                ) else getString(R.string.one_task_pending)
                                            }
                                        } catch (e: Exception) {
                                        }
                                        if (intCantServ1 > 0) {
                                            strDialog += getString(R.string.those_tasks_closed_incomplet)
                                        }
                                        strDialog += getString(R.string.are_you_sure_continue)
                                        if (boolBtnJornada) {
                                            if (intCantServ != 0 || intCantServ1 != 0) {
                                                alertDialogJornada.setMessage(strDialog)
                                                showJornada = alertDialogJornada.show()
                                                btn2.setOnClickListener {
                                                    val mDialog = ProgressDialog(requireContext())
                                                    mDialog.setMessage(getString(R.string.closed_tasks))
                                                    mDialog.show()
                                                    if (Common.arrayServicePro != null) {
                                                        Common.arrayServicePro = closeTask(
                                                            Common.arrayServicePro,
                                                            1
                                                        )
                                                    }
                                                    if (Common.arrayServicePen != null) {
                                                        Common.arrayServicePen = closeTask(
                                                            Common.arrayServicePen,
                                                            1
                                                        )
                                                    }
                                                    if (Common.arrayServicePro1 != null) {
                                                        Common.arrayServicePro1 = closeTask(
                                                            Common.arrayServicePro1,
                                                            0
                                                        )
                                                    }
                                                    if (Common.arrayServicePen1 != null) {
                                                        Common.arrayServicePen1 = closeTask(
                                                            Common.arrayServicePen1,
                                                            0
                                                        )
                                                    }
                                                    showJornada?.dismiss()
                                                    mDialog.dismiss()
                                                    boolBtnJornada = false
                                                    btnJornada!!.callOnClick()
                                                }
                                            } else {
                                                boolBtnJornada = false
                                                btnJornada!!.callOnClick()
                                            }
                                        } else {
                                            boolBtnJornada = true
                                            alertDialogJornada.setMessage(getString(R.string.are_you_sure_finis_work))
                                            showJornada = alertDialogJornada.show()
                                            btn2.setOnClickListener {
                                                val mapTiempos =
                                                    Common.mapRegistroUs?.get("Tiempos") as MutableMap<String, Any>
                                                val mapUpdate: MutableMap<String, Any> = HashMap()
                                                val mapDriver: MutableMap<String, Any> = HashMap()

                                                mapTiempos["FinJornada"] =
                                                    Common.dateFormatHora.format(Date())
                                                fg.ejecDB(requireContext(),
                                                    "UPDATE '101_REGISTRO' set finjornada='" + Common.dateFormatHora.format(
                                                        Date()).toString()
                                                            + "' where useruid='" + fg.parametro(
                                                        requireContext(),
                                                        "USERUID") + "' and fecha='" + fg.parametro(
                                                        requireContext(),
                                                        "FECHA_ACT") + "';")
                                                val sincO = SincronizarOnline()
                                                sincO.sincronizarRegistro101(requireContext())

                                                mapUpdate["Tiempos"] = mapTiempos
                                                mapDriver["estadoDriver"] = "INACTIVO"
                                                Common.dbRegistroUs?.document(Common.mapRegistroUs!!["key"].toString())
                                                    ?.update(
                                                        mapUpdate
                                                    )
                                                Common.dbDriversInformation?.document(
                                                    Common.documentUser?.get(
                                                        "key"
                                                    ).toString()
                                                )?.update(mapDriver)
                                                showJornada?.dismiss()
                                            }
                                        }
                                        btn1.setOnClickListener { showJornada!!.dismiss() }
                                    } else Toast.makeText(
                                        requireContext(),
                                        getString(R.string.finish_permissions_for_continue),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else Toast.makeText(
                                    requireContext(),
                                    getString(R.string.finish_lunch_for_continue),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else Toast.makeText(
                                requireContext(),
                                getString(R.string.finish_break_for_continue),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.can_not_finish_three_hours),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    myDateReference.child(currentDate)
                        .child(FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(
                            updateRegister
                        )
                } else Toast.makeText(
                    requireContext(),
                    getString(R.string.take_pictures_continue),
                    Toast.LENGTH_SHORT
                ).show()
            }

            private fun closeTask(
                arrayTask: java.util.ArrayList<MutableMap<String, Any>?>?,
                intCase: Int,
            ): java.util.ArrayList<MutableMap<String, Any>?>? {
                var cont = 0
                for (mapService in arrayTask!!) {
                    var mapHistorial: MutableMap<String?, Any?>? = HashMap()
                    var mapListHistorial: MutableMap<String?, Any?>? = HashMap()
                    var mapHistoryNote: java.util.ArrayList<Map<String?, Any?>?>? = ArrayList()
                    if (mapService!!["historial"] != null) {
                        mapListHistorial = mapService["historial"] as MutableMap<String?, Any?>?
                        mapHistorial =
                            mapListHistorial!![Common.formatDate.format(Date())] as MutableMap<String?, Any?>?
                        mapHistoryNote =
                            mapHistorial!!["historyNote"] as java.util.ArrayList<Map<String?, Any?>?>?
                    }
                    val mapNote: MutableMap<String?, Any?> = java.util.HashMap()
                    when (intCase) {
                        0 -> {
                            mapService["estado"] = "No exitosa"
                            mapService["prsNoved"] = "1" //Con novedad
                            mapHistorial!!["tipNov"] = "5"
                            mapNote["iduser"] = FirebaseAuth.getInstance().currentUser!!.uid
                            mapNote["tipouser"] = "Sistema, Usuario de Campo"
                            mapNote["nota"] =
                                """Hora: ${Common.formatHora.format(Date())}Usuario: ${Common.documentUser!!["nombres"].toString()} ${Common.documentUser!!["apellidos"].toString()}Nota: Tarea no ejecutada por cierre de Jorada."""
                            mapHistoryNote?.add(mapNote)
                            mapHistorial["historyNote"] = mapHistoryNote
                            mapListHistorial!![Common.formatDate.format(Date())] = mapHistorial
                            mapService["historial"] = mapListHistorial
                        }
                        1 -> {
                            val mapEjecution = mapService["ejecucion"] as MutableMap<String, Any>?
                            val dateNow = Date()
                            dateNow.hours = dateNow.hours + 24
                            mapEjecution!!["fechain"] = Common.formatDate.format(dateNow)
                            mapService["ejecucion"] = mapEjecution
                            mapHistorial!!["motivoNoEx"] = "0"
                            mapNote["iduser"] = FirebaseAuth.getInstance().currentUser!!.uid
                            mapNote["tipouser"] = "Sistema, Usuario de Campo"
                            mapNote["nota"] =
                                """Hora: ${Common.formatHora.format(Date())}Usuario: ${Common.documentUser!!["nombres"].toString()} ${Common.documentUser!!["apellidos"].toString()}Nota: Se genera registro en cierre de jornada para tarea abierta."""
                            mapHistoryNote!!.add(mapNote)
                            mapHistorial["historyNote"] = mapHistoryNote
                            mapListHistorial!![Common.formatDate.format(Date())] = mapHistorial
                            mapService["historial"] = mapListHistorial
                        }
                        2 -> {
                            mapService["estado"] = "No exitosa"
                            mapService["prsNoved"] = "1" //Con novedad
                            mapHistorial!!["tipNov"] = "5"
                            mapNote["iduser"] = FirebaseAuth.getInstance().currentUser!!.uid
                            mapNote["tipouser"] = "Sistema, Usuario de Campo"
                            mapNote["nota"] =
                                """Hora: ${Common.formatHora.format(Date())}Usuario: ${Common.documentUser!!["nombres"].toString()} ${Common.documentUser!!["apellidos"].toString()}Nota: Tarea Se cierra al inicio de jornada."""
                            mapHistoryNote!!.add(mapNote)
                            mapHistorial["historyNote"] = mapHistoryNote
                            mapListHistorial!![Common.formatDate.format(Date())] = mapHistorial
                            mapService["historial"] = mapListHistorial
                        }
                    }
                    Common.dbServices!!.document(mapService["key"].toString()).update(mapService)
                    arrayTask[cont] = mapService
                    cont++
                }
                return arrayTask
            }
        })
        btnBreack.setOnClickListener {
            val currentHora: String = Common.dateFormatHora.format(Date())
            val initmp = fg.getQ1(requireContext(),"Select inciobreak from '101_REGISTRO'  where useruid='" + fg.parametro(requireContext(),
                "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                "FECHA_ACT") + "';")
            val fintmp = fg.getQ1(requireContext(),"Select finbreak from '101_REGISTRO'  where useruid='" + fg.parametro(requireContext(),
                "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                "FECHA_ACT") + "';")
            if (initmp=="0" || initmp==""){
                fg.ejecDB(requireContext(),
                    "UPDATE '101_REGISTRO' set iniciobreak='" + currentHora
                            + "' where useruid='" + fg.parametro(requireContext(),
                        "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                        "FECHA_ACT") + "';")
                val sincO = SincronizarOnline()
                sincO.sincronizarRegistro101(requireContext())
                btnBreack.setText("FINALIZAR BREAK")
            }else if (fintmp=="0" || fintmp==""){
                val inflaterpop =
                    requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                popupConfirmar(inflaterpop,"2")
            }else{
                Toast.makeText(requireContext(),
                    "Ya inicio y finalizo break",
                    Toast.LENGTH_LONG).show()
            }
        }
        btnAlmuerzo.setOnClickListener {
            val currentHora: String = Common.dateFormatHora.format(Date())
            val inialm = fg.getQ1(requireContext(),"Select incioalmuerzo  from '101_REGISTRO'  where useruid='" + fg.parametro(requireContext(),
                "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                "FECHA_ACT") + "';")
            val finalm = fg.getQ1(requireContext(),"Select finalmuerzo  from '101_REGISTRO'  where useruid='" + fg.parametro(requireContext(),
                "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                "FECHA_ACT") + "';")
            if (inialm=="0" || inialm==""){
                fg.ejecDB(requireContext(),
                    "UPDATE '101_REGISTRO' set inicioalmuerzo='" + currentHora
                            + "' where useruid='" + fg.parametro(requireContext(),
                        "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                        "FECHA_ACT") + "';")
                val sincO = SincronizarOnline()
                sincO.sincronizarRegistro101(requireContext())
                btnAlmuerzo.setText("FINALIZAR ALMUERZO")
            }else if (finalm=="0" || finalm==""){
                val inflaterpop =
                    requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                popupConfirmar(inflaterpop,"3")
            }else{
                Toast.makeText(requireContext(),
                    "Ya inicio y finalizo almuerzo",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun popupConfirmar(inflater: LayoutInflater, tipopop:String) {
        val fg = FuncionesGenerales()
        val lts = LoadTaskSqlite()
        val sinco = SincronizarOnline()
        sinco.sincronizarTablasVF(requireContext())
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        val popUp = inflater.inflate(R.layout.confirmar_layout, null)
        val titulo = popUp.findViewById<View>(R.id.tvTitulo) as TextView
        val valtitulo = if (tipopop=="1"){
            "Esta seguro que desea finalizar jornada, aun tiene tareas pendientes por ejecutar:"
        }else if (tipopop=="2"){
            "Esta seguro que desea finalizar Break:"
        }else if (tipopop=="3"){
            "Esta seguro que desea finalizar Almuerzo:"
        }else if (tipopop=="4"){
            "Esta seguro que desea finalizar Permiso:"
        }else {
            "Esta seguro que desea continuar"
        }
        titulo.text = valtitulo
        val cancel = popUp.findViewById<View>(R.id.btnCancelarSB) as TextView
        val ok = popUp.findViewById<View>(R.id.btnIngresarST) as TextView
        val popupWindow = PopupWindow(popUp, LinearLayout.LayoutParams.MATCH_PARENT, 500, true)
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        var latact = latitud_ult
        var lonact = longitud_ult
        cancel.setOnClickListener { popupWindow.dismiss() }
        ok.setOnClickListener {
            if (tipopop=="1") {
                fg.ejecDB(requireContext(),
                    "UPDATE '101_REGISTRO' set finjornada='" + fg.fechaActual(4)
                            + "' where useruid='" + fg.parametro(requireContext(),
                        "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                        "FECHA_ACT") + "';")
                val sincO = SincronizarOnline()
                sincO.sincronizarRegistro101(requireContext())
            } else if(tipopop=="2") {
                fg.ejecDB(requireContext(),
                    "UPDATE '101_REGISTRO' set finbreak='" + fg.fechaActual(4)
                            + "' where useruid='" + fg.parametro(requireContext(),
                        "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                        "FECHA_ACT") + "';")
                val sincO = SincronizarOnline()
                sincO.sincronizarRegistro101(requireContext())
            }else if(tipopop=="3") {
                fg.ejecDB(requireContext(),
                    "UPDATE '101_REGISTRO' set finalmuerzo='" + fg.fechaActual(4)
                            + "' where useruid='" + fg.parametro(requireContext(),
                        "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                        "FECHA_ACT") + "';")
                val sincO = SincronizarOnline()
                sincO.sincronizarRegistro101(requireContext())
            }else if(tipopop=="4") {
                fg.ejecDB(requireContext(),
                    "UPDATE '101_REGISTRO' set finpermiso='" + fg.fechaActual(4)
                            + "' where useruid='" + fg.parametro(requireContext(),
                        "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
                        "FECHA_ACT") + "';")
                val sincO = SincronizarOnline()
                sincO.sincronizarRegistro101(requireContext())
            }
        }
        popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
        popupWindow.showAtLocation(popUp, Gravity.CENTER, 0, 0)
    }

    @Throws(Exception::class)
    fun tomarFoto(campo: String) {
        val fg = FuncionesGenerales()
        fg.act_param(requireContext(), "CAMPOFENC", campo)
        val two: Thread = object : Thread() {
            override fun run() {
                val carpetaIMG = File(
                    Environment.getExternalStorageDirectory().toString() + "/TCGO_FILES/",
                    "IMG"
                )
                val builder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())
                val camara = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (!carpetaIMG.exists()) {
                    carpetaIMG.mkdirs()
                }

                nombreFoto =
                    "TCGO_" + fg.useruid(requireContext()) + "_" + fg.fechaActual(2) + "_" + campo + ".jpg"
                path = Environment.getExternalStorageDirectory()
                    .toString() + "/TCGO_FILES/IMG/" + nombreFoto
                val imagen = File(carpetaIMG, nombreFoto)
                val uri = Uri.fromFile(imagen)
                camara.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(camara, DriverHomeFragment.TAKE_PICTURE)
            }
        }
        two.start()
        two.join()
    }

    fun upLoadImage() {
        val waitingDialog = SpotsDialog.Builder().setContext(requireContext()).build()
        waitingDialog.show()
        val arrayStrImgPath = Paper.book().read<java.util.ArrayList<String>>("listImage")
        val arrayMapEmail =
            Paper.book().read<java.util.ArrayList<MutableMap<String, String>>>("listEmail")
        var cantUpEmail = 0
        try {
            val cantEmail = arrayMapEmail.size
            if (cantEmail != 0) {
                waitingDialog.setMessage("Cargando: ${(cantUpEmail + 1)} de $cantEmail")
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_images_upload),
                    Toast.LENGTH_SHORT
                ).show()
                waitingDialog.dismiss()
            }
            for (params: MutableMap<String, String> in arrayMapEmail) {
                val queue = Volley.newRequestQueue(requireContext())
                // Request a string response from the provided URL.
                val stringRequest: StringRequest = object : StringRequest(Method.POST,
                    Common.urlApi,
                    Response.Listener {
                        android.util.Log.e("tcgoRute", "3")
                    },
                    Response.ErrorListener {
                    }) {
                    override fun getParams(): MutableMap<String, String> {
                        return params
                    }
                }

                // Add the request to the RequestQueue.
                queue.add(stringRequest)
                cantUpEmail++
                waitingDialog.setMessage("Cargando: " + (cantUpEmail + 1) + " de " + cantEmail)
                arrayMapEmail.remove(params)
                Paper.book().write("listEmail", arrayStrImgPath)
                if (cantEmail == cantUpEmail) {
                    waitingDialog.dismiss()
                }
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.not_images_upload),
                Toast.LENGTH_SHORT
            ).show()
            waitingDialog.dismiss()
        }
        var cantUpLoad = 0
        try {
            val cantImg = arrayStrImgPath.size
            if (cantImg != 0) {
                waitingDialog.setMessage("Cargando: ${(cantUpLoad + 1)} de $cantImg")
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_images_upload),
                    Toast.LENGTH_SHORT
                ).show()
                waitingDialog.dismiss()
            }
            for (
            strImgPath: String in arrayStrImgPath) {
                Log.e("tcgoUpLoad", strImgPath)
                val file = File(strImgPath)
                var bitmap = BitmapFactory.decodeFile(strImgPath)
                bitmap = Tools.resizeBitmap(bitmap, 700)
                val baos = java.io.ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val mountainsRef = storageRef.child(file.name)
                val uploadTask = mountainsRef.putBytes(data)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    cantUpLoad++
                    waitingDialog.setMessage("Cargando: " + (cantUpLoad + 1) + " de " + cantImg)
                    arrayStrImgPath.remove(strImgPath)
                    Paper.book().write("listImage", arrayStrImgPath)
                    Log.e("tcgoUpLoad", "UpLoadImage" + taskSnapshot.uploadSessionUri)
                    if (cantImg == cantUpLoad) {
                        waitingDialog.dismiss()
                    }
                }.addOnFailureListener { Log.e("tcgoUpLoad", "No UpLoadImage") }
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.not_images_upload),
                Toast.LENGTH_SHORT
            ).show()
            waitingDialog.dismiss()
        }
    }

    private fun initFirebaseStorage() {
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage!!.reference
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            requireActivity()
        )
    }

    fun setFormJornada() {
        val fg = FuncionesGenerales()
        if (Common.mapImgHome == null) {
            Common.mapImgHome = HashMap()
        }
        if (Common.mapRegistroUs?.get("img") != null) {
            Common.mapImgHome = Common.mapRegistroUs?.get("img") as MutableMap<String, Any>?
        }
        if (fg.fotoencabezado(requireContext(), "vh1") != "0") {
            imgPhVh.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
            cantPhoto1++
        } else {
            imgPhVh.setImageResource(R.drawable.ic_camera_alt_black_24dp)
        }
        if (fg.fotoencabezado(requireContext(), "vh2") != "0") {
            imgPhVh2.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
            cantPhoto1++
        } else {
            imgPhVh2.setImageResource(R.drawable.ic_camera_alt_black_24dp)
        }
        if (fg.fotoencabezado(requireContext(), "km1") != "0") {
            imgPhkm!!.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
            cantPhoto2++
        } else {
            imgPhkm.setImageResource(R.drawable.ic_camera_alt_black_24dp)
        }
        if (fg.fotoencabezado(requireContext(), "km2") != "0") {
            imgPhkm2.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
            cantPhoto2++
        } else {
            imgPhkm2.setImageResource(R.drawable.ic_camera_alt_black_24dp)
        }

        val inialm = fg.getQ1(requireContext(),"Select incioalmuerzo  from '101_REGISTRO'  where useruid='" + fg.parametro(requireContext(),
            "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
            "FECHA_ACT") + "';")
        val finalm = fg.getQ1(requireContext(),"Select finalmuerzo  from '101_REGISTRO'  where useruid='" + fg.parametro(requireContext(),
            "USERUID") + "' and fecha='" + fg.parametro(requireContext(),
            "FECHA_ACT") + "';")























        val mapTiempos = Common.mapRegistroUs?.get("Tiempos") as Map<String?, Any>
        if (mapTiempos["InicioJornada"] != null) {
            btnJornada?.isEnabled = true
            txtInicioJornada.text = mapTiempos["InicioJornada"].toString()
            btnJornada?.text = getString(R.string.finish_work_day)
            Common.ifJornada = 1
            btnBreack.isEnabled = true
            btnAlmuerzo.isEnabled = true
            btnPermiso.isEnabled = true
            spnPermiso.isEnabled = true
            dateInJor = Date()
            dateInJor?.setHours(
                Common.dateFormatHora.parse(mapTiempos["InicioJornada"].toString()).getHours()
            )
            dateInJor?.setMinutes(
                Common.dateFormatHora.parse(mapTiempos["InicioJornada"].toString()).getMinutes()
            )
        } else {
            txtInicioJornada.text = "--:--   --"
            btnJornada!!.text = getString(R.string.init_work_day)
            btnBreack.isEnabled = false
            btnAlmuerzo.isEnabled = false
            btnPermiso.isEnabled = false
            spnPermiso.isEnabled = false
        }
        if (mapTiempos["FinJornada"] != null) {
            txtFinJornada.text = mapTiempos["FinJornada"].toString()
            btnJornada!!.isEnabled = false
            Common.ifJornada = 2
            btnBreack.isEnabled = false
            btnAlmuerzo.isEnabled = false
            btnPermiso.isEnabled = false
            spnPermiso.isEnabled = false
        } else {
            txtFinJornada.text = "--:--   --"

        }
        if (mapTiempos["InicioBreack"] != null) {
            txtInicioBreack.text = mapTiempos["InicioBreack"].toString()
            btnBreack.text = getString(R.string.finish_break)
            Common.ifBreack = 1
            dateInBre = Date()
            dateInBre!!.setHours(
                Common.dateFormatHora.parse(mapTiempos["InicioBreack"].toString()).getHours()
            )
            dateInBre!!.setMinutes(
                Common.dateFormatHora.parse(mapTiempos["InicioBreack"].toString()).getMinutes()
            )
        } else {
            txtInicioBreack.text = "--:--   --"
            btnBreack.text = getString(R.string.init_break)
        }
        if (mapTiempos["FinBreack"] != null) {
            txtFinBreack.text = mapTiempos["FinBreack"].toString()
            btnBreack.isEnabled = false
            Common.ifBreack = 2
        } else {
            txtFinBreack.text = "--:--   --"
        }
        if (mapTiempos["InicioAlmuerzo"] != null) {
            txtInicioAlmuerzo.text = mapTiempos["InicioAlmuerzo"].toString()
            btnAlmuerzo.setText(getString(R.string.finish_lunch))
            Common.ifAlmuerzo = 1
            dateInAlm = Date()
            dateInAlm!!.setHours(
                Common.dateFormatHora.parse(mapTiempos["InicioAlmuerzo"].toString()).getHours()
            )
            dateInAlm!!.setMinutes(
                Common.dateFormatHora.parse(mapTiempos["InicioAlmuerzo"].toString())
                    .getMinutes()
            )
        } else {
            txtInicioAlmuerzo.setText("--:--   --")
            btnAlmuerzo.text = getString(R.string.init_lunch)
        }
        if (mapTiempos["FinAlmuerzo"] != null) {
            txtFinAlmuerzo.text = mapTiempos["FinAlmuerzo"].toString()
            btnAlmuerzo.isEnabled = false
            Common.ifAlmuerzo = 2
        } else {
            txtFinAlmuerzo.text = "--:--   --"
        }
        if (mapTiempos["InicioPermiso"] != null) {
            txtInicioPermiso.text = mapTiempos["InicioPermiso"].toString()
            btnPermiso.text = getString(R.string.finish_permission)
            Common.ifPermiso = 1
            spnPermiso.isEnabled = false
        } else {
            txtInicioPermiso.text = "--:--   --"
            btnPermiso.text = getString(R.string.init_permission)
        }
        if (mapTiempos["FinPermiso"] != null) {
            txtFinPermiso.text = mapTiempos["FinPermiso"].toString()
            btnPermiso.isEnabled = false
            spnPermiso.isEnabled = false
            Common.ifPermiso = 2
        } else {
            txtFinPermiso.text = "--:--   --"
        }
    }

    var urlget = Common.urlApi.toString()
    fun getPostData(params: MutableMap<String, String>, context: Context?, request: String) {

        var fg = FuncionesGenerales()
        var maplist: MutableMap<String?, Any?>? = null
        var maplistnoex: MutableMap<String?, Any?>? = null
        var maplistselector: MutableMap<String?, Any?>? = null
        var maplistselecelement: MutableMap<String?, Any?>? = null

        val urlget =
            Common.urlApi.toString() + "&key=" + Common.keyReq.toString() + "&idclient=" + params["idclient"].toString()
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        var stringRequest: StringRequest = object : StringRequest(
            Method.GET, urlget,
            Response.Listener { response ->
                // Display the first 500 characters of the response string.
                when (request) {
                    "getData" -> {
                        try {
                            fg.ejecDB(requireContext(), "Delete from '104_TIPO_SERVICIO'")
                            fg.ejecDB(requireContext(), "Delete from '105_NO_EXITOSO'")
                            fg.ejecDB(requireContext(), "Delete from '106_SELECTOR'")
                            fg.ejecDB(requireContext(), "Delete from '107_SEL_ELEMENTO'")
                            val data = JSONObject(response)
                            val obj3 = data["DataClient"] as JSONArray
                            val obj4 = data["DataKeyClient"] as JSONArray
                            try {
                                val obj2 = data["DataTipoSer"] as JSONArray
                                Common.listTipoServ = HashMap()
                                var i = 0
                                while (i < obj2.length()) {
                                    (Common.listTipoServ as HashMap<String, String>).put(
                                        obj2.getJSONObject(
                                            i
                                        )["id_tip_ser"].toString(),
                                        obj2.getJSONObject(i)["tipo_serv"].toString()
                                    )
                                    i++
                                }
                                Paper.book().write<Any>("listTipoServ", Common.listTipoServ)
                            } catch (e: Exception) {
                            } finally {
                                maplist = Common.listTipoServ as MutableMap<String?, Any?>?
                                maplist?.forEach { tiposerv ->
                                    val sql =
                                        "INSERT OR IGNORE INTO '104_TIPO_SERVICIO' VALUES ('" + tiposerv.key as String? + "' , '" + tiposerv.value as String? + "');"
                                    fg.ejecDB(requireContext(), sql)
                                }
                            }
                            try {
                                val obj1 = data["DataTipNoEx"] as JSONArray
                                Common.listNoEx = HashMap()
                                var i = 0
                                while (i < obj1.length()) {
                                    (Common.listNoEx as HashMap<Int, String>).put(
                                        Integer.valueOf(
                                            obj1.getJSONObject(
                                                i
                                            )["id_no_exit"].toString()
                                        ), obj1.getJSONObject(i)["tip_no_exit"].toString()
                                    )
                                    i++
                                }
                                Paper.book().write<Any>("listNoEx", Common.listNoEx)
                            } catch (e: Exception) {
                            } finally {
                                var cont: Int = 1
                                maplistnoex = Common.listNoEx as MutableMap<String?, Any?>?
                                maplistnoex?.forEach { noexit ->
                                    val sele = noexit.toString().substringBefore("=")
                                    val sql =
                                        "INSERT OR IGNORE INTO '105_NO_EXITOSO' VALUES ('" + sele + "' , '" + noexit.value + "');"
                                    fg.ejecDB(requireContext(), sql)
                                    cont++
                                }
                            }
                            try {
                                val obj5 = data["DataSelector"] as JSONArray
                                Common.listSelector = HashMap()
                                Common.listSelectorElement = HashMap()
                                var i = 0
                                while (i < obj5.length()) {
                                    val dataElement =
                                        JSONArray(obj5.getJSONObject(i)["items"].toString())
                                    val listElement = java.util.ArrayList<String>()
                                    var j = 0
                                    while (j < dataElement.length()) {
                                        listElement.add(dataElement.getJSONObject(j)["nombre_element"].toString())
                                        j++
                                    }
                                    val listElementSelector: MutableMap<String?, ArrayList<String>> =
                                        HashMap()
                                    listElementSelector[obj5.getJSONObject(i)["selector"].toString()] =
                                        listElement
                                    (Common.listSelector as HashMap<Int, String>).put(
                                        Integer.valueOf(
                                            obj5.getJSONObject(
                                                i
                                            )["id_selector"].toString()
                                        ), obj5.getJSONObject(i)["selector"].toString()
                                    )
                                    (Common.listSelectorElement as HashMap<String, java.util.ArrayList<String>>).put(
                                        obj5.getJSONObject(
                                            i
                                        )["selector"].toString(), listElement
                                    )
                                    i++
                                }
                                Paper.book().write<Any>("listSelector", Common.listSelector)
                                Paper.book().write<Any>(
                                    "listSelectorElement",
                                    Common.listSelectorElement
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                maplistselector =
                                    Common.listSelector as MutableMap<String?, Any?>?
                                maplistselector?.forEach { selector ->
                                    val sele = selector.toString().substringBefore("=")
                                    val sql =
                                        "INSERT OR IGNORE INTO '106_SELECTOR' VALUES ('" + sele + "' , '" + selector.value as String? + "');"
                                    fg.ejecDB(requireContext(), sql)

                                }
                                maplistselecelement =
                                    Common.listSelectorElement as MutableMap<String?, Any?>?
                                maplistselecelement?.forEach { selectelement ->
                                    val arrayselele =
                                        selectelement.value as java.util.ArrayList<*>
                                    var cont: Int = 1
                                    for (arrayselelem in arrayselele) {
                                        val sqlsel =
                                            "SELECT idselector from '106_SELECTOR' where selector='" + selectelement.key + "'"
                                        val idselector = fg.getQ1(requireContext(), sqlsel)
                                        val sql =
                                            "INSERT OR IGNORE INTO '107_SEL_ELEMENTO' VALUES ('" + idselector.toString() + "' , '" + cont.toString() + "' , '" + arrayselelem.toString() + "');"
                                        fg.ejecDB(requireContext(), sql)
                                        cont++
                                    }

                                }
                            }
                            Paper.book().write("DataClient", obj3)
                            Paper.book().write("DataKeyClient", obj4)
                            textImg1!!.text = (Paper.book()
                                .read<Any>("DataClient") as JSONArray).getJSONObject(
                                0
                            )["nameSop1"].toString()
                            textImg2.text = (Paper.book()
                                .read<Any>("DataClient") as JSONArray).getJSONObject(
                                0
                            )["nameSop2"].toString()
                            Common.hashCode = (Paper.book()
                                .read<Any>("DataKeyClient") as JSONArray).getJSONObject(
                                0
                            )["hash_code"].toString()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                //Toast.makeText(requireContext(), getString(R.string.problem_conection), Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String> {
                params["request"] = request
                params["key"] = Common.keyReq
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun generateBasicAuthentication(): String {
        val user = Common.documentUser
        val userName = user?.get("driverCC") ?: ""
        val userUid = user?.get("UserUID") ?: ""
        val password = "3f9b0a1064c7d1db34b009571dd910fe"
        val auth = "${userName}:${password}"
        val basicAuthorization =
            "Basic ${Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)}"
        return basicAuthorization
    }

    private fun updateFirebaseToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(requireActivity()) { instanceIdResult ->
            val mToken = instanceIdResult.token
            val db = FirebaseDatabase.getInstance()
            val tokens = db.getReference(Common.token_tbl)
            tokens.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(mToken)
            Log.e("Token", mToken)
        }
    }

    companion object {
        private const val TAKE_PICTURE = 1
        const val MY_DEFAULT_TIMEOUT = 60000

        @JvmStatic
        fun newInstance() = DriverHomeFragment()
    }
}

